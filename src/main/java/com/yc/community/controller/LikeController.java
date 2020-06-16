package com.yc.community.controller;

import com.yc.community.entity.User;
import com.yc.community.service.LikeService;
import com.yc.community.util.CommunityUtil;
import com.yc.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @PostMapping("/like")
    @ResponseBody
    public String like(int entityUserId, int entityType, int entityId) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(1, "您还没有登录哦！");
        }

        likeService.like(user.getId(), entityType, entityId, entityUserId);

        Map<String, Object> map = new HashMap<>();
        long likeCountEntity = likeService.getLikeCountEntity(entityType, entityId);
        map.put("likeCountEntity", likeCountEntity);
        int likeEntityStatus = likeService.getLikeEntityStatus(user.getId(), entityType, entityId);
        map.put("likeEntityStatus", likeEntityStatus);

        return CommunityUtil.getJSONString(0, null, map);
    }

}
