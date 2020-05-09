package com.yc.community.controller;

import com.yc.community.entity.DiscussPost;
import com.yc.community.entity.Page;
import com.yc.community.entity.User;
import com.yc.community.service.DiscussPostService;
import com.yc.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    DiscussPostService discussPostService;
    @Autowired
    UserService userService;

    @RequestMapping("/index")
    public String getIndexPage(Model model, Page page) {
        page.setRows(discussPostService.selectDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> discussPosts = discussPostService.selectDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map> list=new ArrayList<>();
        for (DiscussPost discussPost:discussPosts) {
            Map<String,Object> map = new HashMap<>();
            map.put("post",discussPost);
            User user = userService.findUserById(discussPost.getUserId());
            map.put("user",user);
            list.add(map);
        }
        model.addAttribute("discussPosts",list);
        return  "/index";
    }
}
