package com.example.demo.controller;

import com.example.demo.dto.SiteSettingsDto;
import com.example.demo.service.SiteSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SiteSettingsController {

    @Autowired
    private SiteSettingsService siteSettingsService;

    @GetMapping("/site/settings")
    public SiteSettingsDto getPublic() {
        return siteSettingsService.getPublic();
    }

    @GetMapping("/admin/site/settings")
    public SiteSettingsDto getAdmin() {
        return siteSettingsService.getAdmin();
    }

    @PutMapping("/admin/site/settings")
    public SiteSettingsDto update(@RequestBody SiteSettingsDto body) {
        siteSettingsService.update(body);
        return siteSettingsService.getAdmin();
    }
}
