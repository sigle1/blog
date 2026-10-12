package com.example.demo.service.impl;

import com.example.demo.dto.SiteSettingsDto;
import com.example.demo.entity.SiteSettings;
import com.example.demo.mapper.SiteSettingsMapper;
import com.example.demo.service.SiteSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SiteSettingsServiceImpl implements SiteSettingsService {

    private static final String DEFAULT_FAVICON = "/favicon.svg";

    @Autowired
    private SiteSettingsMapper siteSettingsMapper;

    @Override
    public SiteSettingsDto getPublic() {
        return toDto(loadRowOrNull(), true);
    }

    @Override
    public SiteSettingsDto getAdmin() {
        SiteSettingsDto out = toDto(loadRowOrNull(), false);
        if (out.getFaviconUrl() == null || out.getFaviconUrl().isBlank()) {
            out.setFaviconUrl("");
        }
        return out;
    }

    @Override
    public void update(SiteSettingsDto dto) {
        String url = dto.getFaviconUrl() != null ? dto.getFaviconUrl().trim() : "";
        if (url.length() > 512) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "图标地址过长");
        }
        if (!url.isEmpty() && !url.startsWith("/") && !url.startsWith("http://") && !url.startsWith("https://")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "图标地址须以 / 或 http(s):// 开头");
        }
        SiteSettings row = new SiteSettings();
        row.setId(1);
        row.setFaviconUrl(url.isEmpty() ? null : url);
        siteSettingsMapper.upsert(row);
    }

    private SiteSettings loadRowOrNull() {
        try {
            return siteSettingsMapper.selectSingleton();
        } catch (Exception e) {
            return null;
        }
    }

    private SiteSettingsDto toDto(SiteSettings row, boolean useDefault) {
        SiteSettingsDto out = new SiteSettingsDto();
        String raw = row != null ? row.getFaviconUrl() : null;
        if (raw != null && !raw.isBlank()) {
            out.setFaviconUrl(raw.trim());
        } else if (useDefault) {
            out.setFaviconUrl(DEFAULT_FAVICON);
        } else {
            out.setFaviconUrl("");
        }
        return out;
    }
}
