package com.example.demo.entity;

import lombok.Data;

@Data
public class HomeHeroSettings {
    private Integer id;
    private String heroTitle;
    private String introTitle;
    private String bannerImagesJson;
    private String heroSubtitlesJson;
    private String authorDisplayName;
    private String authorMotto;
    private String authorAvatar;
}
