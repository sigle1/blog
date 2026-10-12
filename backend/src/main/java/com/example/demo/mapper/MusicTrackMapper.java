package com.example.demo.mapper;

import com.example.demo.entity.MusicTrack;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MusicTrackMapper {
    List<MusicTrack> selectAllOrdered();

    MusicTrack selectById(@Param("id") Long id);

    int selectNextSortOrder();

    void insert(@Param("track") MusicTrack track);

    void insertLegacy(@Param("track") MusicTrack track);

    void update(@Param("track") MusicTrack track);

    void updateSortOrder(@Param("id") Long id, @Param("sortOrder") int sortOrder);

    void delete(@Param("id") Long id);
}
