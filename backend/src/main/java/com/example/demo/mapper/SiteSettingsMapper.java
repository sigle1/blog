package com.example.demo.mapper;

import com.example.demo.entity.SiteSettings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SiteSettingsMapper {
    SiteSettings selectSingleton();

    void upsert(@Param("row") SiteSettings row);
}
