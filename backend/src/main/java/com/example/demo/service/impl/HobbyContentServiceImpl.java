package com.example.demo.service.impl;

import com.example.demo.entity.HobbyContent;
import com.example.demo.mapper.HobbyContentMapper;
import com.example.demo.service.HobbyContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HobbyContentServiceImpl implements HobbyContentService {
    @Autowired
    private HobbyContentMapper hobbyContentMapper;

    @Override
    public void save(HobbyContent content) {
        hobbyContentMapper.insert(content);
    }

    @Override
    public List<HobbyContent> getContentsByHobbyId(Long hobbyId) {
        return hobbyContentMapper.selectByHobbyId(hobbyId);
    }
}

