package com.example.demo.service;

import com.example.demo.entity.HobbyArticle;
import java.util.List;

public interface HobbyArticleService {
    List<HobbyArticle> getArticlesByHobbyId(Long hobbyId);
    HobbyArticle getArticleById(Long id);
    void save(HobbyArticle article);
    void update(HobbyArticle article);
    void incrementViews(Long id);
}

