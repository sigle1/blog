package com.example.demo.dto;

import lombok.Data;

@Data
public class SiteSettingsDto {
    /** 浏览器标签图标地址，空则使用默认 /favicon.svg */
    private String faviconUrl;
}
