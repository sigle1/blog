package com.example.demo.mapper;

import com.example.demo.entity.HobbyContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface HobbyContentMapper {
    void insert(@Param("content") HobbyContent content);
    List<HobbyContent> selectByHobbyId(@Param("hobbyId") Long hobbyId);
}

