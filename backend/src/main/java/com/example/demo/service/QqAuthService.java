package com.example.demo.service;

import com.example.demo.entity.User;

public interface QqAuthService {
    String buildAuthorizeUrl();

    String handleCallback(String code, String state);
}
