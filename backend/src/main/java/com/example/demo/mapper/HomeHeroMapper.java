package com.example.demo.mapper;

import com.example.demo.entity.HomeHeroSettings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HomeHeroMapper {

    HomeHeroSettings selectSingleton();

    int upsert(@Param("row") HomeHeroSettings row);
}
