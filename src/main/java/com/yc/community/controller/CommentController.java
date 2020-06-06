package com.yc.community.controller;

import com.yc.community.entity.Comment;
import com.yc.community.service.CommentService;
import com.yc.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    //添加评论
    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable String discussPostId, Comment comment, HttpServletResponse response) {
        if (hostHolder.getUser() == null) {
            return "/site/login";
        }
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        comment.setUserId(hostHolder.getUser().getId());

        commentService.addComment(comment);

        return "redirect:/discuss/detail/" + discussPostId;
    }

}
