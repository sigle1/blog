package com.example.demo.service.impl;

import com.example.demo.entity.HobbyArticle;
import com.example.demo.mapper.HobbyArticleMapper;
import com.example.demo.service.HobbyArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HobbyArticleServiceImpl implements HobbyArticleService {
    @Autowired
    private HobbyArticleMapper hobbyArticleMapper;

    @Override
    public List<HobbyArticle> getArticlesByHobbyId(Long hobbyId) {
        return hobbyArticleMapper.selectByHobbyId(hobbyId);
    }

    @Override
    public HobbyArticle getArticleById(Long id) {
        return hobbyArticleMapper.selectById(id);
    }

    @Override
    public void save(HobbyArticle article) {
        hobbyArticleMapper.insert(article);
    }

    @Override
    public void update(HobbyArticle article) {
        hobbyArticleMapper.update(article);
    }

    @Override
    public void incrementViews(Long id) {
        hobbyArticleMapper.updateViews(id);
    }
}

