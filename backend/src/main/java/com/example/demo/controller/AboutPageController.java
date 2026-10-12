package com.example.demo.controller;

import com.example.demo.dto.AboutPageDto;
import com.example.demo.service.AboutPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AboutPageController {

    @Autowired
    private AboutPageService aboutPageService;

    @GetMapping("/about/content")
    public AboutPageDto getPublic() {
        return aboutPageService.getPublic();
    }

    @GetMapping("/admin/about/content")
    public AboutPageDto getAdmin() {
        return aboutPageService.getAdmin();
    }

    @PutMapping("/admin/about/content")
    public AboutPageDto update(@RequestBody AboutPageDto body) {
        aboutPageService.update(body);
        return aboutPageService.getAdmin();
    }
}
