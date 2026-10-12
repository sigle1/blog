package com.example.demo.service.impl;

import com.example.demo.dto.BlogSidebarStats;
import com.example.demo.dto.HomeHeroDto;
import com.example.demo.entity.HomeHeroSettings;
import com.example.demo.mapper.BlogStatsMapper;
import com.example.demo.mapper.HomeHeroMapper;
import com.example.demo.service.HomeHeroService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

@Service
public class HomeHeroServiceImpl implements HomeHeroService {

    private static final Logger log = LoggerFactory.getLogger(HomeHeroServiceImpl.class);

    private static final TypeReference<List<String>> LIST_STRING = new TypeReference<>() {};

    @Autowired
    private HomeHeroMapper homeHeroMapper;

    @Autowired
    private BlogStatsMapper blogStatsMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public HomeHeroDto getPublic() {
        return toDto(loadRowOrNull());
    }

    @Override
    public HomeHeroDto getAdmin() {
        return toDto(loadRowOrNull());
    }

    @Override
    public void update(HomeHeroDto dto) {
        if (dto.getHeroTitle() == null || dto.getHeroTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "主标题不能为空");
        }
        List<String> banners = dto.getBannerImages() != null ? dto.getBannerImages() : Collections.emptyList();
        List<String> subs = dto.getHeroSubtitles() != null ? dto.getHeroSubtitles() : Collections.emptyList();
        if (banners.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "至少配置一条首页背景（图片或视频 URL）");
        }
        if (subs.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "至少一句副标题");
        }
        try {
            HomeHeroSettings existing = loadRowOrNull();
            HomeHeroSettings row = new HomeHeroSettings();
            row.setId(1);
            row.setHeroTitle(dto.getHeroTitle().trim());
            row.setIntroTitle(dto.getIntroTitle() != null && !dto.getIntroTitle().isBlank()
                    ? dto.getIntroTitle().trim() : "欢迎来到琉璃月");
            row.setBannerImagesJson(objectMapper.writeValueAsString(banners));
            row.setHeroSubtitlesJson(objectMapper.writeValueAsString(subs));
            row.setAuthorDisplayName(mergeAuthorString(dto.getAuthorDisplayName(),
                    existing != null ? existing.getAuthorDisplayName() : null, "黑虎阿昊"));
            row.setAuthorMotto(mergeAuthorString(dto.getAuthorMotto(),
                    existing != null ? existing.getAuthorMotto() : null,
                    "不经一番寒彻骨，怎得梅花扑鼻香"));
            row.setAuthorAvatar(mergeAuthorNullable(dto.getAuthorAvatar(),
                    existing != null ? existing.getAuthorAvatar() : null));
            homeHeroMapper.upsert(row);
        } catch (Exception e) {
            log.error("保存首页壁纸失败（检查表 home_hero_settings 是否存在、是否已执行 migration_home_hero_author_sidebar.sql）", e);
            String detail = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            if (detail.length() > 300) {
                detail = detail.substring(0, 300) + "…";
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "保存失败: " + detail, e);
        }
    }

    private static String mergeAuthorString(String incoming, String previous, String fallback) {
        if (incoming != null && !incoming.isBlank()) {
            return incoming.trim();
        }
        if (previous != null && !previous.isBlank()) {
            return previous.trim();
        }
        return fallback;
    }

    private static String mergeAuthorNullable(String incoming, String previous) {
        if (incoming != null) {
            String t = incoming.trim();
            return t.isEmpty() ? null : t;
        }
        if (previous != null && !previous.isBlank()) {
            return previous.trim();
        }
        return null;
    }

    private HomeHeroSettings loadRowOrNull() {
        try {
            return homeHeroMapper.selectSingleton();
        } catch (Exception e) {
            return null;
        }
    }

    private HomeHeroDto toDto(HomeHeroSettings row) {
        HomeHeroDto out = new HomeHeroDto();
        if (row == null) {
            out.setHeroTitle("琉璃月");
            out.setIntroTitle("欢迎来到琉璃月");
            out.setBannerImages(Collections.emptyList());
            out.setHeroSubtitles(Collections.emptyList());
            out.setAuthorDisplayName("黑虎阿昊");
            out.setAuthorMotto("不经一番寒彻骨，怎得梅花扑鼻香");
            out.setAuthorAvatar(null);
        } else {
            out.setHeroTitle(row.getHeroTitle());
            out.setIntroTitle(row.getIntroTitle());
            out.setBannerImages(parseList(row.getBannerImagesJson()));
            out.setHeroSubtitles(parseList(row.getHeroSubtitlesJson()));
            out.setAuthorDisplayName(mergeAuthorString(row.getAuthorDisplayName(), null, "黑虎阿昊"));
            out.setAuthorMotto(mergeAuthorString(row.getAuthorMotto(), null, "不经一番寒彻骨，怎得梅花扑鼻香"));
            out.setAuthorAvatar(row.getAuthorAvatar() != null && !row.getAuthorAvatar().isBlank()
                    ? row.getAuthorAvatar().trim() : null);
        }
        attachStats(out);
        return out;
    }

    private void attachStats(HomeHeroDto out) {
        try {
            BlogSidebarStats s = blogStatsMapper.selectSidebarStats();
            if (s != null) {
                out.setPostCount(s.getPostCount());
                out.setCategoryCount(s.getCategoryCount());
                out.setCommentCount(s.getCommentCount());
            } else {
                out.setPostCount(0L);
                out.setCategoryCount(0L);
                out.setCommentCount(0L);
            }
        } catch (Exception e) {
            out.setPostCount(0L);
            out.setCategoryCount(0L);
            out.setCommentCount(0L);
        }
    }

    private List<String> parseList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<String> list = objectMapper.readValue(json, LIST_STRING);
            return list != null ? list : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
