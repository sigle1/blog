package com.example.demo.service;

import com.example.demo.entity.Hobby;
import java.util.List;

public interface HobbyService {
    List<Hobby> getAllHobbies();
    Hobby getHobbyById(Long id);
    void addHobby(Hobby hobby);
    void updateHobby(Hobby hobby);
    void deleteHobbyById(Long id);

    Hobby getById(Long id);
}
