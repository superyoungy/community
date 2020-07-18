package com.yc.community.controller;

import com.yc.community.entity.DiscussPost;
import com.yc.community.entity.Page;
import com.yc.community.service.ElasticsearchService;
import com.yc.community.service.LikeService;
import com.yc.community.service.UserService;
import com.yc.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @GetMapping("/search")
    public String search(String keyword, Page page, Model model) {
        org.springframework.data.domain.Page<DiscussPost> discussPosts = elasticsearchService.searchDiscussPost(
                keyword, page.getCurrent() - 1, page.getLimit());
        if (discussPosts == null) {
            model.addAttribute("keyword", keyword);
            return "site/search";
        }

        page.setPath("/search?keyword=" + keyword);
        page.setRows((int) discussPosts.getTotalElements());

        List<Map<String, Object>> discussPostsVo = new ArrayList<>();
        for (DiscussPost post : discussPosts) {
            Map<String, Object> map = new HashMap<>();
            map.put("post", post);
            map.put("user", userService.findUserById(post.getUserId()));
            map.put("likeCount", likeService.getLikeCountEntity(ENTITY_TYPE_POST, post.getId()));
            discussPostsVo.add(map);
        }
        model.addAttribute("discussPostsVo", discussPostsVo);
        model.addAttribute("keyword", keyword);

        return "site/search";
    }
}
