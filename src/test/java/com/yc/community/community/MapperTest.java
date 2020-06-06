package com.yc.community.community;

import com.yc.community.CommunityApplication;
import com.yc.community.dao.DiscussPostMapper;
import com.yc.community.dao.LoginTicketMapper;
import com.yc.community.dao.MessageMapper;
import com.yc.community.dao.UserMapper;
import com.yc.community.entity.DiscussPost;
import com.yc.community.entity.LoginTicket;
import com.yc.community.entity.Message;
import com.yc.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Date;
import java.util.List;
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {
    @Autowired(required = false)
    private UserMapper userMapper;

    @Test
    public void selectUser() {
        User user = userMapper.selectById(151);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUserName("test4");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void updateUser() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "hello");
        System.out.println(rows);
    }

    @Autowired(required = false)
    DiscussPostMapper discussPostMapper;

    @Test
    public void selectDiscussPost() {
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 3, 10);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost.getId());
        }
    }

    @Test
    public void selectDiscussPostRows() {
        int i = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(i);
    }

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    public void insertLoginTicket() {
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 12));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void selectByTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket.toString());
    }

    @Test
    public void updateStatus() {
        loginTicketMapper.updateStatus("397525be0f60488b92f1a081295a81b8", 0);
        System.out.println(loginTicketMapper.selectByTicket("397525be0f60488b92f1a081295a81b8").getStatus());
    }

    @Autowired
    private MessageMapper messageMapper;
    @Test
    public void testMessage() {
        List<Message> messages = messageMapper.selectConversations(111, 0, 10);
        for (Message message : messages) {
            System.out.println(message);
        }
        int i = messageMapper.selectConversationCount(111);
        System.out.println(i);
        List<Message> messages1 = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : messages1) {
            System.out.println(message);
        }
        int i1 = messageMapper.selectLetterCount("111_112");
        System.out.println(i1);
        int i2 = messageMapper.selectLetterUnreadCount(112, "111_112");
        System.out.println(i2);

    }

    @Test
    public void insertMessage() {
        Message message = new Message();
        message.setContent("1111111111");
        message.setFromId(111);
        message.setToId(112);
        message.setConversationId("111_112");
        message.setCreateTime(new Date());
        int i3 = messageMapper.insertMessage(message);
        System.out.println(i3);

        int i4 = messageMapper.updateStatus(Collections.singletonList(message.getId()), 1);
        System.out.println(i4);
    }

}
