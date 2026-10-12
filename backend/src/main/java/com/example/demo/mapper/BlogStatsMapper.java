package com.example.demo.mapper;

import com.example.demo.dto.BlogSidebarStats;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BlogStatsMapper {
    BlogSidebarStats selectSidebarStats();
}
