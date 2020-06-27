package com.yc.community.service;

import com.yc.community.dao.MessageMapper;
import com.yc.community.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;

    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    public int addMessage(Message message) {
        return messageMapper.insertMessage(message);
    }

    public int readMessage(List<Integer> ids, int status) {
        return messageMapper.updateStatus(ids, status);
    }

    public List<Message> findNotices(int id, String topic, int offset, int limit) {
        return messageMapper.selectNotices(id, topic, offset, limit);
    }

    public Message findLatestNotice(int id, String topic) {
        return messageMapper.selectLatestNotice(id, topic);
    }

    public int findNoticeCount(int id, String topic) {
        return messageMapper.selectNoticeCount(id, topic);
    }

    public int findNoticeUnreadCount(int id, String topic) {
        return messageMapper.selectNoticeUnreadCount(id, topic);
    }
}
