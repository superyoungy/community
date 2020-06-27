package com.yc.community.dao;

import com.yc.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    //查询当前用户的会话列表，针对每个会话只返回一条最新私信
    List<Message> selectConversations(int userId, int offset, int limit);

    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询每个会话的私信列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    //查询每个会话的私信数量
    int selectLetterCount(String conversationId);

    //查询未读私信的数量
    int selectLetterUnreadCount(int userId, String conversationId);

    //添加私信
    int insertMessage(Message message);

    //更改私信状态
    int updateStatus(List<Integer> ids, int status);

    //查询所有通知
    List<Message> selectNotices(int id, String topic, int offset, int limit);

    //查询最新通知
    Message selectLatestNotice(int id, String topic);

    //查询通知数量
    int selectNoticeCount(int id, String topic);

    //查询未读通知数量
    int selectNoticeUnreadCount(int id, String topic);

}
