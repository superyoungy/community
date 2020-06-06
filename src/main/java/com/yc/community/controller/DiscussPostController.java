package com.yc.community.controller;

import com.yc.community.entity.Comment;
import com.yc.community.entity.DiscussPost;
import com.yc.community.entity.Page;
import com.yc.community.entity.User;
import com.yc.community.service.CommentService;
import com.yc.community.service.DiscussPostService;
import com.yc.community.service.UserService;
import com.yc.community.util.CommunityConstant;
import com.yc.community.util.CommunityUtil;
import com.yc.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录喔!");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setUserId(user.getId());
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        return CommunityUtil.getJSONString(0, "发布成功!");
    }

    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable int discussPostId, Model model, Page page) {
        //帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        //作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        //评论分页信息
        page.setPath("/discuss/detail/" + discussPostId);
        page.setLimit(5);
        page.setRows(post.getCommentCount());

        //评论：给帖子的评论
        //回复：给评论的评论
        //评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        //评论Vo列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        for (Comment comment : commentList) {
            //评论Vo
            Map<String, Object> commentVo = new HashMap<>();
            //评论
            commentVo.put("comment", comment);
            //作者
            commentVo.put("user", userService.findUserById(comment.getUserId()));

            //回复列表
            List<Comment> replyList = commentService.findCommentsByEntity(
                    ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
            //回复Vo列表
            List<Map<String, Object>> replyVoList = new ArrayList<>();
            for (Comment reply : replyList) {
                Map<String, Object> replyVo = new HashMap<>();
                //回复
                replyVo.put("reply", reply);
                //作者
                replyVo.put("user", userService.findUserById(reply.getUserId()));
                //回复目标
                User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                replyVo.put("target", target);

                replyVoList.add(replyVo);
            }
            commentVo.put("replys", replyVoList);

            //回复数量
            int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
            commentVo.put("replyCount", replyCount);

            commentVoList.add(commentVo);
        }

        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }
}
