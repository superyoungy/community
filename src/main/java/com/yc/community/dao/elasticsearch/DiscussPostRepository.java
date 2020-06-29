package com.yc.community.dao.elasticsearch;

import com.yc.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

@Component
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {
}
