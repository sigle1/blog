package com.example.demo.service.impl;

import com.example.demo.entity.GuestMessage;
import com.example.demo.mapper.GuestMessageMapper;
import com.example.demo.service.GuestMessageService;
import com.example.demo.service.MessageCommentService;
import com.example.demo.support.GuestMessageSchemaSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GuestMessageServiceImpl implements GuestMessageService {
    @Autowired
    private GuestMessageMapper guestMessageMapper;

    @Autowired
    private MessageCommentService messageCommentService;

    @Autowired
    private GuestMessageSchemaSupport schemaSupport;

    @Override
    public List<GuestMessage> getAllMessages() {
        if (schemaSupport.isInteractionsReady()) {
            return guestMessageMapper.selectAll();
        }
        return guestMessageMapper.selectAllLegacy();
    }

    @Override
    public GuestMessage getMessageById(Long id) {
        if (schemaSupport.isInteractionsReady()) {
            return guestMessageMapper.selectById(id);
        }
        return guestMessageMapper.selectByIdLegacy(id);
    }

    @Override
    public GuestMessage addMessage(GuestMessage message) {
        if (schemaSupport.isInteractionsReady()) {
            if (message.getLikes() == null) {
                message.setLikes(0);
            }
            if (message.getFavorites() == null) {
                message.setFavorites(0);
            }
            guestMessageMapper.insert(message);
            return guestMessageMapper.selectById(message.getId());
        }
        guestMessageMapper.insertLegacy(message);
        return guestMessageMapper.selectByIdLegacy(message.getId());
    }

    @Override
    public void likeMessage(Long id) {
        requireInteractionsReady();
        guestMessageMapper.incrementLikes(id);
    }

    @Override
    public void favoriteMessage(Long id) {
        requireInteractionsReady();
        guestMessageMapper.incrementFavorites(id);
    }

    @Override
    public Map<String, Object> getAdminMessages(String keyword, Integer page, Integer size) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null || size < 1 ? 10 : size;
        int offset = (safePage - 1) * safeSize;

        List<GuestMessage> list;
        if (schemaSupport.isInteractionsReady()) {
            list = guestMessageMapper.selectAdmin(keyword, offset, safeSize);
        } else {
            list = guestMessageMapper.selectAdminLegacy(keyword, offset, safeSize);
        }
        long total = guestMessageMapper.countAdmin(keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", safePage);
        result.put("size", safeSize);
        return result;
    }

    @Override
    public void deleteMessageById(Long id) {
        if (schemaSupport.isInteractionsReady()) {
            messageCommentService.deleteCommentsByMessageId(id);
        }
        guestMessageMapper.deleteById(id);
    }

    private void requireInteractionsReady() {
        if (!schemaSupport.isInteractionsReady()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "留言互动功能尚未初始化，请在数据库执行 migration_guest_message_interactions.sql 后重启后端"
            );
        }
    }
}
