package com.example.demo.controller;

import com.example.demo.entity.Hobby;
import com.example.demo.entity.HobbyContent;
import com.example.demo.service.HobbyService;
import com.example.demo.service.HobbyContentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping({"/hobbies", "/admin/hobbies"})
public class HobbyController {
    @Autowired
    private HobbyService hobbyService;
    @Autowired
    private HobbyContentService hobbyContentService;

    // 获取所有爱好
    @GetMapping
    public List<Hobby> getAllHobbies() {
        return hobbyService.getAllHobbies();
    }

    // 创建新爱好
    @PostMapping
    public ResponseEntity<Hobby> createHobby(@RequestBody Hobby hobby) {
        try {
            if (hobby == null || hobby.getName() == null || hobby.getName().isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            if (hobby.getCreatedAt() == null) {
                hobby.setCreatedAt(LocalDateTime.now());
            }
            hobbyService.addHobby(hobby);
            return ResponseEntity.ok(hobby);
        } catch (Exception e) {
            log.error("Error creating hobby: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    // 获取单个爱好详情
    @GetMapping("/{id}")
    public ResponseEntity<?> getHobbyDetail(@PathVariable Long id) {
        Hobby hobby = hobbyService.getById(id);
        if (hobby == null) {
            return ResponseEntity.notFound().build();
        }
        List<HobbyContent> contents = hobbyContentService.getContentsByHobbyId(id);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("hobby", hobby);
        result.put("contents", contents);
        return ResponseEntity.ok(result);
    }

    // 为爱好添加内容
    @PostMapping("/{id}/contents")
    public ResponseEntity<HobbyContent> addHobbyContent(@PathVariable Long id, @RequestBody HobbyContent content) {
        try {
            Hobby hobby = hobbyService.getById(id);
            if (hobby == null) {
                return ResponseEntity.notFound().build();
            }
            if (content == null || content.getContent() == null || content.getContent().isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            content.setHobbyId(id);
            if (content.getCreatedAt() == null) {
                content.setCreatedAt(LocalDateTime.now());
            }
            hobbyContentService.save(content);
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            log.error("Error adding hobby content: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    // 更新爱好
    @PutMapping("/{id}")
    public ResponseEntity<Hobby> updateHobby(@PathVariable Long id, @RequestBody Hobby hobbyDetails) {
        try {
            Hobby hobby = hobbyService.getById(id);
            if (hobby == null) {
                return ResponseEntity.notFound().build();
            }
            if (hobbyDetails == null || hobbyDetails.getName() == null || hobbyDetails.getName().isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            hobby.setName(hobbyDetails.getName());
            hobby.setDescription(hobbyDetails.getDescription());
            hobby.setCoverUrl(hobbyDetails.getCoverUrl());
            hobbyService.updateHobby(hobby);
            return ResponseEntity.ok(hobby);
        } catch (Exception e) {
            log.error("Error updating hobby: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    // 删除爱好
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteHobby(@PathVariable Long id) {
        try {
            Hobby hobby = hobbyService.getById(id);
            if (hobby == null) {
                return ResponseEntity.notFound().build();
            }
            hobbyService.deleteHobbyById(id);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            log.error("Error deleting hobby: {}", e.getMessage());
            return ResponseEntity.status(500).body(false);
        }
    }
}
