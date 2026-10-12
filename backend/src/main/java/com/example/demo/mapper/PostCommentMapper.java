package com.example.demo.mapper;

import com.example.demo.entity.PostComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostCommentMapper {
    List<PostComment> selectByPostId(@Param("postId") Long postId);
    List<PostComment> selectAdminComments(@Param("keyword") String keyword, @Param("offset") Integer offset, @Param("size") Integer size);
    long countAdminComments(@Param("keyword") String keyword);
    PostComment selectById(@Param("id") Long id);
    void insert(@Param("comment") PostComment comment);
    void incrementLikes(@Param("id") Long id);
    void deleteById(@Param("id") Long id);
    void deleteByParentId(@Param("parentId") Long parentId);
}
