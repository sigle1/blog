package com.example.demo.service.impl;

import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class  UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public boolean validatePassword(User user, String password) {
        // 这里应该使用密码加密验证，暂时简单比较
        return user != null && user.getPassword().equals(password);
    }

    @Override
    public User findOrCreateByQqOpenid(String openid, String nickname) {
        User existing = userMapper.findByQqOpenid(openid);
        if (existing != null) {
            return existing;
        }
        String username = buildQqUsername(openid, nickname);
        User user = new User();
        user.setUsername(username);
        user.setPassword("QQ_OAUTH_NO_PASSWORD");
        user.setRole("admin");
        user.setQqOpenid(openid);
        userMapper.insert(user);
        return user;
    }

    private String buildQqUsername(String openid, String nickname) {
        String base;
        if (nickname != null && !nickname.isBlank()) {
            base = nickname.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5_]", "");
            if (base.length() > 20) {
                base = base.substring(0, 20);
            }
            if (!base.isBlank()) {
                base = "qq_" + base;
            } else {
                base = "qq_" + openid.substring(Math.max(0, openid.length() - 10));
            }
        } else {
            base = "qq_" + openid.substring(Math.max(0, openid.length() - 10));
        }
        if (userMapper.findByUsername(base) == null) {
            return base;
        }
        return base + "_" + openid.substring(Math.max(0, openid.length() - 6));
    }
}
