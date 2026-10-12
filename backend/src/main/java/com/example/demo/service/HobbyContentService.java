package com.example.demo.service;

import com.example.demo.entity.HobbyContent;
import java.util.List;

public interface HobbyContentService {
    void save(HobbyContent content);
    List<HobbyContent> getContentsByHobbyId(Long hobbyId);
}

