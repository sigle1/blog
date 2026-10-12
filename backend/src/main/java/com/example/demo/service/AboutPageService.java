package com.example.demo.service;

import com.example.demo.dto.AboutPageDto;

public interface AboutPageService {

    AboutPageDto getPublic();

    AboutPageDto getAdmin();

    void update(AboutPageDto dto);
}
