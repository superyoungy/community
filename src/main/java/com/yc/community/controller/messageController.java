package com.yc.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.yc.community.annotation.LoginRequired;
import com.yc.community.entity.Message;
import com.yc.community.entity.Page;
import com.yc.community.entity.User;
import com.yc.community.service.MessageService;
import com.yc.community.service.UserService;
import com.yc.community.util.CommunityConstant;
import com.yc.community.util.CommunityUtil;
import com.yc.community.util.HostHolder;
import com.yc.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class messageController implements CommunityConstant {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    //私信列表
    @LoginRequired
    @GetMapping("/letter/list")
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();

        //分页信息
        page.setPath("/letter/list");
        page.setLimit(5);
        int userId = user.getId();
        page.setRows(messageService.findConversationCount(userId));

        //会话列表
        List<Message> conversationList = messageService.findConversations(
                userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);

                String conversationId = message.getConversationId();
                map.put("letterCount", messageService.findLetterCount(conversationId));
                map.put("letterUnreadCount", messageService.findLetterUnreadCount(userId, conversationId));

                int targetId = message.getFromId() == userId ? message.getToId() : message.getFromId();
                User target = userService.findUserById(targetId);
                map.put("target", target);

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        //未读私信总数
        int letterUnreadCount = messageService.findLetterUnreadCount(userId, null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        //未读通知总数
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/letter";
    }

    //会话详情
    @LoginRequired
    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable String conversationId, Page page, Model model) {
        //分页信息
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        page.setLimit(5);

        //私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        for (Message message : letterList) {
            if (message.getToId() == hostHolder.getUser().getId() && message.getStatus() == 0) {
                ids.add(message.getId());
            }

            Map<String, Object> map = new HashMap<>();
            map.put("letter", message);
            map.put("fromUser", userService.findUserById(message.getFromId()));

            letters.add(map);
        }
        model.addAttribute("letters", letters);

        model.addAttribute("target", getTarget(conversationId));

        if (!ids.isEmpty()) {
            messageService.readMessage(ids, 1);
        }

        return "/site/letter-detail";
    }

    public User getTarget(String conversationId) {
        String[] id = conversationId.split("_");
        int fromId = Integer.parseInt(id[0]);
        int toId = Integer.parseInt(id[1]);
        int userId = hostHolder.getUser().getId();

        int targetId = userId == fromId ? toId : fromId;
        return userService.findUserById(targetId);
    }

    //发送私信
    @LoginRequired
    @PostMapping("/letter/send")
    @ResponseBody
    public String addLetter(String content, String targetName) {
        if (userService.findUserByName(targetName) == null) {
            return CommunityUtil.getJSONString(1, "用户不存在！");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(userService.findUserByName(targetName).getId());
        String conversationId;
        if (message.getFromId() < message.getToId()) {
            conversationId = message.getFromId() + "_" + message.getToId();
        } else {
            conversationId = message.getToId() + "_" + message.getFromId();
        }
        message.setConversationId(conversationId);
        content = HtmlUtils.htmlEscape(content);
        content = sensitiveFilter.doFilter(content);
        message.setContent(content);
        message.setCreateTime(new Date());

        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

    //系统通知列表
    @LoginRequired
    @GetMapping("/notice/list")
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();
        //查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        Map<String, Object> messageVo = new HashMap<>();

        if (message != null) {
            messageVo.put("message", message);

            Map<String, Object> data = JSONObject.parseObject(message.getContent(), HashMap.class);
            messageVo.put("user", userService.findUserById((int) data.get("userId")));
            messageVo.put("postId", data.get("postId"));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));

            messageVo.put("createTime", message.getCreateTime());
            messageVo.put("unreadCount", messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT));
            messageVo.put("count", messageService.findNoticeCount(user.getId(), TOPIC_COMMENT));
        }
        model.addAttribute("commentNoticeVo", messageVo);


        //查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        messageVo = new HashMap<>();
        if (message != null) {
            messageVo.put("message", message);

            Map<String, Object> data = JSONObject.parseObject(message.getContent(), HashMap.class);
            messageVo.put("user", userService.findUserById((int) data.get("userId")));
            messageVo.put("postId", data.get("postId"));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));

            messageVo.put("createTime", message.getCreateTime());
            messageVo.put("unreadCount", messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE));
            messageVo.put("count", messageService.findNoticeCount(user.getId(), TOPIC_LIKE));
        }
        model.addAttribute("likeNoticeVo", messageVo);

        // 查询关注类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        messageVo = new HashMap<>();
        if (message != null) {
            messageVo.put("message", message);

            Map<String, Object> data = JSONObject.parseObject(message.getContent(), HashMap.class);
            messageVo.put("user", userService.findUserById((int) data.get("userId")));
            messageVo.put("postId", data.get("postId"));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));

            messageVo.put("createTime", message.getCreateTime());
            messageVo.put("unreadCount", messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW));
            messageVo.put("count", messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW));
        }
        model.addAttribute("followNoticeVo", messageVo);

        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }

    @LoginRequired
    @GetMapping("/notice/detail/{topic}")
    public String getNoticeDetail(Page page, Model model, @PathVariable String topic) {
        User user = hostHolder.getUser();
        //分页
        int noticeCount = messageService.findNoticeCount(user.getId(), topic);
        page.setPath("/notice/detail/" + topic);
        page.setRows(noticeCount);
        page.setLimit(5);

        List<Message> messages = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());

        //设为已读
        List<Integer> ids = new ArrayList<>();
        for (Message message : messages) {
            ids.add(message.getId());
        }
        if (!ids.isEmpty()) {
            messageService.readMessage(ids, 1);
        }

        List<Map<String, Object>> messagesVo = new ArrayList<>();
        for (Message message : messages) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", message);

            Map<String, Object> data = JSONObject.parseObject(message.getContent(), Map.class);
            map.put("user", userService.findUserById((int) data.get("userId")));
            map.put("entityType", data.get("entityType"));
            map.put("entityId", data.get("entityId"));
            map.put("postId", data.get("postId"));

            map.put("fromUser", userService.findUserById(1));
            map.put("topic", topic);
            map.put("createTime", message.getCreateTime());

            messagesVo.add(map);
        }
        model.addAttribute("notices", messagesVo);

        return "/site/notice-detail";
    }
}
