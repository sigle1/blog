package com.example.demo.mapper;

import com.example.demo.entity.GuestMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GuestMessageMapper {
    List<GuestMessage> selectAll();

    List<GuestMessage> selectAllLegacy();

    GuestMessage selectById(@Param("id") Long id);

    GuestMessage selectByIdLegacy(@Param("id") Long id);

    List<GuestMessage> selectAdmin(@Param("keyword") String keyword, @Param("offset") Integer offset, @Param("size") Integer size);

    List<GuestMessage> selectAdminLegacy(@Param("keyword") String keyword, @Param("offset") Integer offset, @Param("size") Integer size);

    long countAdmin(@Param("keyword") String keyword);

    void insert(@Param("message") GuestMessage message);

    void insertLegacy(@Param("message") GuestMessage message);

    void incrementLikes(@Param("id") Long id);

    void incrementFavorites(@Param("id") Long id);

    void deleteById(@Param("id") Long id);
}
