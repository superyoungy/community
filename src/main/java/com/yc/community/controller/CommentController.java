package com.yc.community.controller;

import com.yc.community.entity.Comment;
import com.yc.community.entity.DiscussPost;
import com.yc.community.entity.Event;
import com.yc.community.event.EventProducer;
import com.yc.community.service.CommentService;
import com.yc.community.service.DiscussPostService;
import com.yc.community.util.CommunityConstant;
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
public class CommentController implements CommunityConstant {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private EventProducer eventProducer;

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

        //系统发送通知
        Event event = new Event();
        if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment commentById = commentService.findCommentById(comment.getEntityId());

            if (commentById.getUserId() == hostHolder.getUser().getId()) {
                return "redirect:/discuss/detail/" + discussPostId;
            }

            event.setEntityUserId(commentById.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost discussPostById = discussPostService.findDiscussPostById(comment.getEntityId());

            if (discussPostById.getUserId() == hostHolder.getUser().getId()) {
                return "redirect:/discuss/detail/" + discussPostId;
            }

            event.setEntityUserId(discussPostById.getUserId());
        }
        event
            .setTopic(TOPIC_COMMENT)
            .setUserId(hostHolder.getUser().getId())
            .setEntityTYpe(comment.getEntityType())
            .setEntityId(comment.getEntityId())
            .addData("postId", discussPostId);
        eventProducer.fireEvent(event);

        return "redirect:/discuss/detail/" + discussPostId;
    }

}
