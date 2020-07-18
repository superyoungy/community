package com.yc.community.controller;

import com.yc.community.entity.*;
import com.yc.community.event.EventProducer;
import com.yc.community.service.*;
import com.yc.community.util.CommunityConstant;
import com.yc.community.util.CommunityUtil;
import com.yc.community.util.HostHolder;
import com.yc.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

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

//        elasticsearchService.saveDiscussPost(discussPost);
        //触发事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, discussPost.getId());

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

        //点赞数
        model.addAttribute("likeCountEntity", likeService.getLikeCountEntity(ENTITY_TYPE_POST, post.getId()));
        //点赞状态
        if (hostHolder.getUser() == null) {
            model.addAttribute("likeEntityStatus", 0);
        } else {
            model.addAttribute("likeEntityStatus", likeService.getLikeEntityStatus(
                    hostHolder.getUser().getId(), ENTITY_TYPE_POST, post.getId()));
        }

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
            //点赞数
            commentVo.put("likeCountEntity", likeService.getLikeCountEntity(ENTITY_TYPE_COMMENT, comment.getId()));
            //点赞状态
            if (hostHolder.getUser() == null) {
                commentVo.put("likeEntityStatus", 0);
            } else {
                commentVo.put("likeEntityStatus", likeService.getLikeEntityStatus(
                        hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId()));
            }

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
                //点赞数
                replyVo.put("likeCountEntity", likeService.getLikeCountEntity(ENTITY_TYPE_COMMENT, reply.getId()));
                //点赞状态
                if (hostHolder.getUser() == null) {
                    replyVo.put("likeEntityStatus", 0);
                } else {
                    replyVo.put("likeEntityStatus", likeService.getLikeEntityStatus(
                            hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId()));
                }

                replyVoList.add(replyVo);
            }
            commentVo.put("replys", replyVoList);

            //回复数量
            int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
            commentVo.put("replyCount", replyCount);

            commentVoList.add(commentVo);
        }

        model.addAttribute("comments", commentVoList);

        return "site/discuss-detail";
    }

    @PostMapping("/remove")
    @ResponseBody
    public String removePost(int id) {
        discussPostService.removeDiscussPost(id);

        Event event = new Event()
                .setTopic(TOPiC_REMOVE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }

    @PostMapping("/top")
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updateType(id, 1);

        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }

    @PostMapping("/wonderful")
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateStatus(id, 1);

        //将需要更新分数的帖子id保存到redis
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, id);

        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }
}
