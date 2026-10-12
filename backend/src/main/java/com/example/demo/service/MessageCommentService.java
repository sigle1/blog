package com.example.demo.service;

import com.example.demo.entity.MessageComment;

import java.util.List;

public interface MessageCommentService {
    List<MessageComment> getCommentsByMessageId(Long messageId);

    MessageComment getCommentById(Long id);

    MessageComment addComment(MessageComment comment);

    void likeComment(Long id);

    void deleteCommentById(Long id);

    void deleteCommentsByParentId(Long parentId);

    void deleteCommentsByMessageId(Long messageId);
}
