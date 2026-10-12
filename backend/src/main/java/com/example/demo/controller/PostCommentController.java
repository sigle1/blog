package com.example.demo.controller;

import com.example.demo.entity.Post;
import com.example.demo.entity.PostComment;
import com.example.demo.service.PostCommentService;
import com.example.demo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class PostCommentController {
    @Autowired
    private PostCommentService postCommentService;

    @Autowired
    private PostService postService;

    private static final long COMMENT_INTERVAL_MS = 30_000L;
    private static final Map<String, Long> COMMENT_RATE_LIMIT_MAP = new ConcurrentHashMap<>();
    private static final List<String> SENSITIVE_WORDS = Arrays.asList("傻逼", "妈的", "操", "垃圾");

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<PostComment>> getComments(@PathVariable Long postId) {
        Post post = postService.getPostById(postId);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(postCommentService.getCommentsByPostId(postId));
    }

    @GetMapping("/admin/comments")
    public ResponseEntity<Map<String, Object>> getAdminComments(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        List<PostComment> list = postCommentService.getAdminComments(keyword, page, size);
        long total = postCommentService.countAdminComments(keyword);
        Map<String, Object> result = new ConcurrentHashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<PostComment> addComment(@PathVariable Long postId, @RequestBody PostComment comment, HttpServletRequest request) {
        Post post = postService.getPostById(postId);
        if (post == null) {
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
            PostComment parent = postCommentService.getCommentById(comment.getParentId());
            if (parent == null || parent.getPostId() == null || !parent.getPostId().equals(postId)) {
                return ResponseEntity.badRequest().build();
            }
        }
        comment.setPostId(postId);
        comment.setLikes(0);
        comment.setContent(maskSensitiveWords(comment.getContent().trim()));
        comment.setNickname(comment.getNickname().trim());
        if (comment.getCreatedAt() == null) {
            comment.setCreatedAt(LocalDateTime.now());
        }
        COMMENT_RATE_LIMIT_MAP.put(clientIp, now);

        return ResponseEntity.ok(postCommentService.addComment(comment));
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/like")
    public ResponseEntity<Void> likeComment(@PathVariable Long postId, @PathVariable Long commentId) {
        PostComment comment = postCommentService.getCommentById(commentId);
        if (comment == null || comment.getPostId() == null || !comment.getPostId().equals(postId)) {
            return ResponseEntity.notFound().build();
        }
        postCommentService.likeComment(commentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/admin/posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
        PostComment comment = postCommentService.getCommentById(commentId);
        if (comment == null || comment.getPostId() == null || !comment.getPostId().equals(postId)) {
            return ResponseEntity.notFound().build();
        }
        postCommentService.deleteCommentsByParentId(commentId);
        postCommentService.deleteCommentById(commentId);
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
