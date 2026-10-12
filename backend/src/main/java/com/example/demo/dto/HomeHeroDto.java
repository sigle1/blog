package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HomeHeroDto {
    private String heroTitle;
    private String introTitle;
    private List<String> bannerImages = new ArrayList<>();
    private List<String> heroSubtitles = new ArrayList<>();
    /** 侧栏昵称 */
    private String authorDisplayName;
    /** 侧栏签名 */
    private String authorMotto;
    /** 侧栏头像 URL */
    private String authorAvatar;
    private Long postCount;
    private Long categoryCount;
    private Long commentCount;
}
