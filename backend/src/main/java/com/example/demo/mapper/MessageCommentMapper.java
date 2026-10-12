package com.example.demo.mapper;

import com.example.demo.entity.MessageComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageCommentMapper {
    List<MessageComment> selectByMessageId(@Param("messageId") Long messageId);

    MessageComment selectById(@Param("id") Long id);

    void insert(@Param("comment") MessageComment comment);

    void incrementLikes(@Param("id") Long id);

    void deleteById(@Param("id") Long id);

    void deleteByParentId(@Param("parentId") Long parentId);

    void deleteByMessageId(@Param("messageId") Long messageId);
}
