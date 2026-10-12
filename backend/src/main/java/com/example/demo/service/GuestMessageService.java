package com.example.demo.service;

import com.example.demo.entity.GuestMessage;

import java.util.List;
import java.util.Map;

public interface GuestMessageService {
    List<GuestMessage> getAllMessages();

    GuestMessage getMessageById(Long id);

    GuestMessage addMessage(GuestMessage message);

    void likeMessage(Long id);

    void favoriteMessage(Long id);

    Map<String, Object> getAdminMessages(String keyword, Integer page, Integer size);

    void deleteMessageById(Long id);
}
