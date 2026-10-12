package com.example.demo.mapper;

import com.example.demo.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PostMapper {
    List<Post> selectAll();
    Post selectById(@Param("id") Long id);
    void insert(@Param("post") Post post);
    void update(@Param("post") Post post);
    void delete(@Param("id") Long id);
    void updateViews(@Param("id") Long id);
}