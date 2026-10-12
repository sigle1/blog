package com.example.demo.service;

import com.example.demo.entity.User;

public interface UserService {
    User findByUsername(String username);

    boolean validatePassword(User user, String password);

    User findOrCreateByQqOpenid(String openid, String nickname);
}
