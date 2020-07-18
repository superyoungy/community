package com.yc.community.controller;

import com.yc.community.entity.DiscussPost;
import com.yc.community.entity.Page;
import com.yc.community.entity.User;
import com.yc.community.service.DiscussPostService;
import com.yc.community.service.LikeService;
import com.yc.community.service.UserService;
import com.yc.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @GetMapping("/")
    public String getHomePage() {
        return "forward:/index";
    }

    @GetMapping("/index")
    public String getIndexPage(Model model, Page page,
                               @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> discussPostList = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(), orderMode);
        List<Map> discussPosts=new ArrayList<>();
        for (DiscussPost discussPost:discussPostList) {
            Map<String,Object> map = new HashMap<>();
            map.put("post",discussPost);
            User user = userService.findUserById(discussPost.getUserId());
            map.put("user",user);

            long likeCountEntity = likeService.getLikeCountEntity(ENTITY_TYPE_POST, discussPost.getId());
            map.put("likeCountEntity", likeCountEntity);

            discussPosts.add(map);
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("orderMode", orderMode);
        return  "index";
    }

    @GetMapping("/error")
    public String getErrorPage() {
        return "error/500";
    }

    @GetMapping("/denied")
    public String getDeniedPage() {
        return "error/404";
    }
}
