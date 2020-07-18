package com.yc.community.controller;

import com.yc.community.entity.Event;
import com.yc.community.entity.Page;
import com.yc.community.entity.User;
import com.yc.community.event.EventProducer;
import com.yc.community.service.FollowService;
import com.yc.community.service.UserService;
import com.yc.community.util.CommunityConstant;
import com.yc.community.util.CommunityUtil;
import com.yc.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {
    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping("/follow")
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        if (user != null) {
            followService.follow(user.getId(), entityType, entityId);
            //系统发送通知
            Event event = new Event()
                    .setTopic(TOPIC_FOLLOW)
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityUserId(entityId);
            eventProducer.fireEvent(event);
            return CommunityUtil.getJSONString(0, "关注成功！");
        } else {
            return CommunityUtil.getJSONString(1, "你还没有登录哦！");
        }
    }

    @RequestMapping("/unfollow")
    @ResponseBody
    public String unFollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        if (user != null) {
            followService.unfollow(user.getId(), entityType, entityId);
            return CommunityUtil.getJSONString(0, "取消关注成功！");
        } else {
            return CommunityUtil.getJSONString(1, "你还没有登录哦！");
        }
    }

    @GetMapping("/followee/{userId}")
    public String getFolloweePage(@PathVariable int userId, Page page, Model model) {
        page.setPath("/followee/" + userId);
        page.setLimit(5);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        User user = userService.findUserById(userId);
        model.addAttribute("user", user);

        List<Map<String, Object>> followees = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        for (Map<String, Object> followee : followees) {
            User u = (User) followee.get("user");
            int followStatus = followService.findFollowStatus(ENTITY_TYPE_USER, u.getId());
            followee.put("followStatus", followStatus);
        }
        model.addAttribute("followees", followees);

        return "site/followee";
    }

    @GetMapping("/follower/{userId}")
    public String getFollowerPage(@PathVariable int userId, Page page, Model model) {
        page.setPath("/follower/" + userId);
        page.setLimit(5);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        User user = userService.findUserById(userId);
        model.addAttribute("user", user);

        List<Map<String, Object>> followers = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        for (Map<String, Object> follower : followers) {
            User u = (User) follower.get("user");
            int followStatus = followService.findFollowStatus(ENTITY_TYPE_USER, u.getId());
            follower.put("followStatus", followStatus);
        }
        model.addAttribute("followers", followers);

        return "site/follower";
    }
}
