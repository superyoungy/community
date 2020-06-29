package com.yc.community.service;

import com.yc.community.dao.elasticsearch.DiscussPostRepository;
import com.yc.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ElasticsearchService {
    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    public void saveDiscussPost(DiscussPost post) {
        discussPostRepository.save(post);
    }

    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }

    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        return elasticsearchTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                SearchHits hits = response.getHits();
                if (hits.getTotalHits() <= 0) {
                    return null;
                }

                List<DiscussPost> list = new ArrayList<>();
                for (SearchHit hit : hits) {
                    Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                    Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                    DiscussPost post = new DiscussPost();
                    post.setId(Integer.parseInt(sourceAsMap.get("id").toString()));
                    post.setUserId(Integer.parseInt(sourceAsMap.get("userId").toString()));
                    post.setStatus(Integer.parseInt(sourceAsMap.get("status").toString()));
                    if (highlightFields.get("title") != null) {
                        post.setTitle(highlightFields.get("title").getFragments()[0].toString());
                    } else {
                        post.setTitle(sourceAsMap.get("title").toString());
                    }
                    if (highlightFields.get("content") != null) {
                        post.setContent(highlightFields.get("content").getFragments()[0].toString());
                    } else {
                        post.setContent(sourceAsMap.get("content").toString());
                    }
                    post.setType(Integer.parseInt(sourceAsMap.get("type").toString()));
                    post.setScore(Double.parseDouble(sourceAsMap.get("score").toString()));
                    post.setCreateTime(new Date(Long.parseLong(sourceAsMap.get("createTime").toString())));
                    post.setCommentCount(Integer.parseInt(sourceAsMap.get("commentCount").toString()));
                    list.add(post);
                }
                return new AggregatedPageImpl(list, pageable,
                        hits.getTotalHits(), response.getAggregations(), response.getScrollId(), hits.getMaxScore());
            }
        });
    }
}
