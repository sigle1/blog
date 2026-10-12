package com.example.demo.mapper;

import com.example.demo.entity.AboutPageSettings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AboutPageMapper {

    AboutPageSettings selectSingleton();

    int upsert(@Param("row") AboutPageSettings row);
}
