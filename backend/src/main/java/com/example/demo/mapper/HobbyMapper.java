package com.example.demo.mapper;

import com.example.demo.entity.Hobby;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HobbyMapper {
    List<Hobby> selectAll();

    Hobby selectById(@Param("id") Long id);

    int insert(Hobby hobby);

    int update(Hobby hobby);

    int deleteById(@Param("id") Long id);
}
