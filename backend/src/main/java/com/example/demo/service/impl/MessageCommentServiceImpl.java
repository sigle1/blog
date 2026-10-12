package com.example.demo.service.impl;

import com.example.demo.entity.MessageComment;
import com.example.demo.mapper.MessageCommentMapper;
import com.example.demo.service.MessageCommentService;
import com.example.demo.support.GuestMessageSchemaSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@Service
public class MessageCommentServiceImpl implements MessageCommentService {
    @Autowired
    private MessageCommentMapper messageCommentMapper;

    @Autowired
    private GuestMessageSchemaSupport schemaSupport;

    @Override
    public List<MessageComment> getCommentsByMessageId(Long messageId) {
        if (!schemaSupport.isInteractionsReady()) {
            return Collections.emptyList();
        }
        return messageCommentMapper.selectByMessageId(messageId);
    }

    @Override
    public MessageComment getCommentById(Long id) {
        return messageCommentMapper.selectById(id);
    }

    @Override
    public MessageComment addComment(MessageComment comment) {
        if (!schemaSupport.isInteractionsReady()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "留言评论功能尚未初始化，请执行 migration_guest_message_interactions.sql 后重启后端"
            );
        }
        messageCommentMapper.insert(comment);
        return comment;
    }

    @Override
    public void likeComment(Long id) {
        messageCommentMapper.incrementLikes(id);
    }

    @Override
    public void deleteCommentById(Long id) {
        messageCommentMapper.deleteById(id);
    }

    @Override
    public void deleteCommentsByParentId(Long parentId) {
        messageCommentMapper.deleteByParentId(parentId);
    }

    @Override
    public void deleteCommentsByMessageId(Long messageId) {
        if (!schemaSupport.isInteractionsReady()) {
            return;
        }
        messageCommentMapper.deleteByMessageId(messageId);
    }
}
