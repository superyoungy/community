package com.yc.community.service;

import com.yc.community.dao.UserMapper;
import com.yc.community.entity.LoginTicket;
import com.yc.community.entity.User;
import com.yc.community.util.CommunityConstant;
import com.yc.community.util.CommunityUtil;
import com.yc.community.util.MailClient;
import com.yc.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
//        return userMapper.selectById(id);
        User user = getCache(id);
        if (user != null) {
            return user;
        } else {
            return initCache(id);
        }
    }

    public User findUserByName(String userName) {
        return userMapper.selectByName(userName);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        //空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUserName())) {
            map.put("userNameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }

        //验证账号
        if (userMapper.selectByName(user.getUserName()) != null) {
            map.put("userNameMsg", "该账号已存在！");
            return map;
        }

        //验证邮箱
        if (userMapper.selectByEmail(user.getEmail()) != null) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //激活邮件
        Context context = new Context();
        String email = user.getEmail();
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("email", email);
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (!code.equals(user.getActivationCode())){
            return ACTIVATION_FAILURE;
        } else {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        }
    }

    public Map<String, Object> login(String userName, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空处理
        if (StringUtils.isBlank(userName)) {
            map.put("userNameMsg", "账号不能为空！") ;
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！") ;
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(userName);
        if (user == null) {
            map.put("userNameMsg", "该账号不存在！");
            return map;
        }

        //验证状态
        if (user.getStatus() == 0) {
            map.put("userNameMsg", "该账号未激活！");
            return map;
        }

        //验证密码
        if (!CommunityUtil.md5(password + user.getSalt()).equals(user.getPassword())) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
//        redisTemplate.opsForValue().set(ticketKey, loginTicket, 60 * 60, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(ticketKey, loginTicket, expiredSeconds, TimeUnit.SECONDS);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket) {
//        LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        return loginTicket;
    }

    public void updateHeader(int id, String headerUrl) {
        userMapper.updateHeader(id, headerUrl);
        clearCache(id);
    }

    public Map<String, Object> updatePassword(int id, String originPassword, String newPassword, String repetition) {
        Map<String, Object> map = new HashMap<>();
        if (originPassword == null) {
            map.put("originPasswordMsg", "请输入原密码！");
            return map;
        }
        if(newPassword == null) {
            map.put("newPasswordMsg", "新密码不能为空！");
            return map;
        }
        if(repetition == null) {
            map.put("passwordMsg", "请填写确认密码！");
            return map;
        }

        User user = userMapper.selectById(id);
        String md5OriginPassword = CommunityUtil.md5(originPassword + user.getSalt());
        if (!md5OriginPassword.equals(user.getPassword())) {
            map.put("originPasswordMsg", "原密码不正确！");
            return map;
        }

        if (!newPassword.equals(repetition)) {
            map.put("passwordMsg", "两次输入的密码不一致！");
            return map;
        }

        if(originPassword.equals(newPassword)) {
            map.put("newPasswordMsg", "新密码不能和原密码相同！");
            return map;
        }

        String md5NewPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(id, md5NewPassword);
        clearCache(id);

        return map;
    }

    // 1、优先从缓存中取值
    private User getCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    // 2、取不到值就初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user, 60 * 60, TimeUnit.SECONDS);
        return user;
    }

    // 3、数据更新就清除缓存数据
    private void clearCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }
}
