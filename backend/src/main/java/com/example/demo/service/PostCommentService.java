package com.example.demo.service;

import com.example.demo.entity.PostComment;

import java.util.List;

public interface PostCommentService {
    List<PostComment> getCommentsByPostId(Long postId);
    List<PostComment> getAdminComments(String keyword, Integer page, Integer size);
    long countAdminComments(String keyword);
    PostComment getCommentById(Long id);
    PostComment addComment(PostComment comment);
    void likeComment(Long id);
    void deleteCommentById(Long id);
    void deleteCommentsByParentId(Long parentId);
}
