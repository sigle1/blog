package com.example.demo.mapper;

import com.example.demo.entity.HobbyArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface HobbyArticleMapper {
    List<HobbyArticle> selectByHobbyId(@Param("hobbyId") Long hobbyId);
    HobbyArticle selectById(@Param("id") Long id);
    void insert(@Param("article") HobbyArticle article);
    void update(@Param("article") HobbyArticle article);
    void updateViews(@Param("id") Long id);
}

