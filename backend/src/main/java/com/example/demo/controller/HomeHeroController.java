package com.example.demo.controller;

import com.example.demo.dto.HomeHeroDto;
import com.example.demo.service.HomeHeroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeHeroController {

    @Autowired
    private HomeHeroService homeHeroService;

    @GetMapping("/home/hero")
    public HomeHeroDto getPublic() {
        return homeHeroService.getPublic();
    }

    @GetMapping("/admin/home/hero")
    public HomeHeroDto getAdmin() {
        return homeHeroService.getAdmin();
    }

    @PutMapping("/admin/home/hero")
    public HomeHeroDto update(@RequestBody HomeHeroDto body) {
        homeHeroService.update(body);
        return homeHeroService.getAdmin();
    }
}
