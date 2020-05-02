package com.yc.community.service;

import com.yc.community.dao.UserMapper;
import com.yc.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired(required = false)
    private UserMapper userMapper;

    public User selectById(int id) {
        return userMapper.selectById(id);
    }
}
