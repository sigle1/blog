package com.example.demo.service.impl;

import com.example.demo.entity.PostComment;
import com.example.demo.mapper.PostCommentMapper;
import com.example.demo.service.PostCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostCommentServiceImpl implements PostCommentService {
    @Autowired
    private PostCommentMapper postCommentMapper;

    @Override
    public List<PostComment> getCommentsByPostId(Long postId) {
        return postCommentMapper.selectByPostId(postId);
    }

    @Override
    public List<PostComment> getAdminComments(String keyword, Integer page, Integer size) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null || size < 1 ? 10 : size;
        int offset = (safePage - 1) * safeSize;
        return postCommentMapper.selectAdminComments(keyword, offset, safeSize);
    }

    @Override
    public long countAdminComments(String keyword) {
        return postCommentMapper.countAdminComments(keyword);
    }

    @Override
    public PostComment getCommentById(Long id) {
        return postCommentMapper.selectById(id);
    }

    @Override
    public PostComment addComment(PostComment comment) {
        postCommentMapper.insert(comment);
        return comment;
    }

    @Override
    public void likeComment(Long id) {
        postCommentMapper.incrementLikes(id);
    }

    @Override
    public void deleteCommentById(Long id) {
        postCommentMapper.deleteById(id);
    }

    @Override
    public void deleteCommentsByParentId(Long parentId) {
        postCommentMapper.deleteByParentId(parentId);
    }
}
