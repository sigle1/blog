package com.example.demo.controller;

import com.example.demo.entity.HobbyArticle;
import com.example.demo.service.HobbyArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/hobbies/{hobbyId}/articles", "/admin/hobbies/{hobbyId}/articles"})
public class HobbyArticleController {
    @Autowired
    private HobbyArticleService hobbyArticleService;

    // 获取某爱好下所有文章
    @GetMapping
    public List<HobbyArticle> getArticles(@PathVariable Long hobbyId) {
        return hobbyArticleService.getArticlesByHobbyId(hobbyId);
    }

    // 创建新文章
    @PostMapping
    public ResponseEntity<HobbyArticle> createArticle(@PathVariable Long hobbyId, @RequestBody HobbyArticle article) {
        try {
            if (article == null || article.getTitle() == null || article.getTitle().isEmpty() || 
                article.getContent() == null || article.getContent().isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            article.setHobbyId(hobbyId);
            if (article.getCreatedAt() == null) {
                article.setCreatedAt(LocalDateTime.now());
            }
            article.setUpdatedAt(LocalDateTime.now());
            if (article.getViews() == null) {
                article.setViews(0);
            }
            hobbyArticleService.save(article);
            return ResponseEntity.ok(article);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    // 获取单篇文章详情
    @GetMapping("/{articleId}")
    public ResponseEntity<HobbyArticle> getArticle(@PathVariable Long hobbyId, @PathVariable Long articleId) {
        try {
            hobbyArticleService.incrementViews(articleId); // 增加阅读量
            HobbyArticle article = hobbyArticleService.getArticleById(articleId);
            if (article == null) {
                return ResponseEntity.notFound().build();
            }
            if (article.getHobbyId() == null || !article.getHobbyId().equals(hobbyId)) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(article);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}

