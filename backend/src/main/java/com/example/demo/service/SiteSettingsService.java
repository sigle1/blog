package com.example.demo.service;

import com.example.demo.dto.SiteSettingsDto;

public interface SiteSettingsService {
    SiteSettingsDto getPublic();

    SiteSettingsDto getAdmin();

    void update(SiteSettingsDto dto);
}
