package com.example.demo.controller;

import com.example.demo.entity.GuestMessage;
import com.example.demo.entity.MessageComment;
import com.example.demo.service.GuestMessageService;
import com.example.demo.service.MessageCommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class MessageCommentController {
    @Autowired
    private MessageCommentService messageCommentService;

    @Autowired
    private GuestMessageService guestMessageService;

    private static final long COMMENT_INTERVAL_MS = 30_000L;
    private static final Map<String, Long> COMMENT_RATE_LIMIT_MAP = new ConcurrentHashMap<>();
    private static final List<String> SENSITIVE_WORDS = Arrays.asList("傻逼", "妈的", "操", "垃圾");

    @GetMapping("/messages/{messageId}/comments")
    public ResponseEntity<List<MessageComment>> getComments(@PathVariable Long messageId) {
        GuestMessage message = guestMessageService.getMessageById(messageId);
        if (message == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(messageCommentService.getCommentsByMessageId(messageId));
    }

    @PostMapping("/messages/{messageId}/comments")
    public ResponseEntity<MessageComment> addComment(
            @PathVariable Long messageId,
            @RequestBody MessageComment comment,
            HttpServletRequest request
    ) {
        GuestMessage message = guestMessageService.getMessageById(messageId);
        if (message == null) {
            return ResponseEntity.notFound().build();
        }
        if (comment == null || comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String clientIp = request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
        Long lastCommentAt = COMMENT_RATE_LIMIT_MAP.get(clientIp);
        long now = System.currentTimeMillis();
        if (lastCommentAt != null && now - lastCommentAt < COMMENT_INTERVAL_MS) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        if (comment.getNickname() == null || comment.getNickname().trim().isEmpty()) {
            comment.setNickname("游客");
        }
        if (comment.getParentId() != null) {
            MessageComment parent = messageCommentService.getCommentById(comment.getParentId());
            if (parent == null || parent.getMessageId() == null || !parent.getMessageId().equals(messageId)) {
                return ResponseEntity.badRequest().build();
            }
        }
        comment.setMessageId(messageId);
        comment.setLikes(0);
        comment.setContent(maskSensitiveWords(comment.getContent().trim()));
        comment.setNickname(comment.getNickname().trim());
        if (comment.getCreatedAt() == null) {
            comment.setCreatedAt(LocalDateTime.now());
        }
        COMMENT_RATE_LIMIT_MAP.put(clientIp, now);

        return ResponseEntity.ok(messageCommentService.addComment(comment));
    }

    @PostMapping("/messages/{messageId}/comments/{commentId}/like")
    public ResponseEntity<Void> likeComment(@PathVariable Long messageId, @PathVariable Long commentId) {
        MessageComment comment = messageCommentService.getCommentById(commentId);
        if (comment == null || comment.getMessageId() == null || !comment.getMessageId().equals(messageId)) {
            return ResponseEntity.notFound().build();
        }
        messageCommentService.likeComment(commentId);
        return ResponseEntity.ok().build();
    }

    private String maskSensitiveWords(String text) {
        String result = text;
        for (String word : SENSITIVE_WORDS) {
            if (result.contains(word)) {
                result = result.replace(word, "**");
            }
        }
        return result;
    }
}
