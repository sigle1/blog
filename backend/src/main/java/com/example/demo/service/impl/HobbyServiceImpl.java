package com.example.demo.service.impl;

import com.example.demo.entity.Hobby;
import com.example.demo.mapper.HobbyMapper;
import com.example.demo.service.HobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HobbyServiceImpl implements HobbyService {
    @Autowired
    private HobbyMapper hobbyMapper;

    @Override
    public List<Hobby> getAllHobbies() {
        return hobbyMapper.selectAll();
    }

    @Override
    public Hobby getHobbyById(Long id) {
        return hobbyMapper.selectById(id);
    }

    @Override
    public void addHobby(Hobby hobby) {
        hobbyMapper.insert(hobby);
    }

    @Override
    public void updateHobby(Hobby hobby) {
        hobbyMapper.update(hobby);
    }

    @Override
    public void deleteHobbyById(Long id) {
        hobbyMapper.deleteById(id);
    }

    @Override
    public Hobby getById(Long id) {
        return hobbyMapper.selectById(id);
    }
}
