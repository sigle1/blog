package com.example.demo.controller;

import com.example.demo.entity.GuestMessage;
import com.example.demo.service.GuestMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class GuestMessageController {
    @Autowired
    private GuestMessageService guestMessageService;

    @GetMapping("/messages")
    public ResponseEntity<List<GuestMessage>> getMessages() {
        return ResponseEntity.ok(guestMessageService.getAllMessages());
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<GuestMessage> getMessage(@PathVariable Long id) {
        GuestMessage message = guestMessageService.getMessageById(id);
        if (message == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(message);
    }

    @PostMapping("/messages/{id}/like")
    public ResponseEntity<Void> likeMessage(@PathVariable Long id) {
        GuestMessage message = guestMessageService.getMessageById(id);
        if (message == null) {
            return ResponseEntity.notFound().build();
        }
        guestMessageService.likeMessage(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/messages/{id}/favorite")
    public ResponseEntity<Void> favoriteMessage(@PathVariable Long id) {
        GuestMessage message = guestMessageService.getMessageById(id);
        if (message == null) {
            return ResponseEntity.notFound().build();
        }
        guestMessageService.favoriteMessage(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/messages")
    public ResponseEntity<GuestMessage> addMessage(@RequestBody GuestMessage message) {
        if (message == null || message.getContent() == null || message.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (message.getNickname() == null || message.getNickname().trim().isEmpty()) {
            message.setNickname("游客");
        }
        message.setNickname(message.getNickname().trim());
        message.setContent(message.getContent().trim());
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(LocalDateTime.now());
        }
        message.setLikes(0);
        message.setFavorites(0);
        return ResponseEntity.ok(guestMessageService.addMessage(message));
    }

    @GetMapping("/admin/messages")
    public ResponseEntity<Map<String, Object>> getAdminMessages(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return ResponseEntity.ok(guestMessageService.getAdminMessages(keyword, page, size));
    }

    @DeleteMapping("/admin/messages/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        guestMessageService.deleteMessageById(id);
        return ResponseEntity.ok().build();
    }
}
