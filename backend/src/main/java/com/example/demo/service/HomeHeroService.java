package com.example.demo.service;

import com.example.demo.dto.HomeHeroDto;

public interface HomeHeroService {

    HomeHeroDto getPublic();

    HomeHeroDto getAdmin();

    void update(HomeHeroDto dto);
}
