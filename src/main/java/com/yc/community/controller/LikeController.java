package com.yc.community.controller;

import com.yc.community.entity.Event;
import com.yc.community.entity.User;
import com.yc.community.event.EventProducer;
import com.yc.community.service.LikeService;
import com.yc.community.util.CommunityConstant;
import com.yc.community.util.CommunityUtil;
import com.yc.community.util.HostHolder;
import com.yc.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/like")
    @ResponseBody
    public String like(int entityUserId, int entityType, int entityId, int postId) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(1, "您还没有登录哦！");
        }

        likeService.like(user.getId(), entityType, entityId, entityUserId);

        //判断点赞的对象是否是帖子
        //如果是帖子，将该需要更新分数的帖子id保存到redis
        if (ENTITY_TYPE_POST == entityType) {
            String postScoreKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postScoreKey, entityId);
        }

        Map<String, Object> map = new HashMap<>();
        long likeCountEntity = likeService.getLikeCountEntity(entityType, entityId);
        map.put("likeCountEntity", likeCountEntity);
        int likeEntityStatus = likeService.getLikeEntityStatus(user.getId(), entityType, entityId);
        map.put("likeEntityStatus", likeEntityStatus);

        //系统发送通知
        if (entityUserId == user.getId()) {
            return CommunityUtil.getJSONString(0, null, map);
        }
        if (likeEntityStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .addData("postId", postId);
            eventProducer.fireEvent(event);
        }

        return CommunityUtil.getJSONString(0, null, map);
    }

}
