package com.example.demo.service.impl;

import com.example.demo.entity.Post;
import com.example.demo.mapper.PostMapper;
import com.example.demo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {
    @Autowired
    private PostMapper postMapper;

    @Override
    public List<Post> getAllPosts() {
        return postMapper.selectAll();
    }

    @Override
    public Post getPostById(Long id) {
        return postMapper.selectById(id);
    }

    @Override
    public Post createPost(Post post) {
        postMapper.insert(post);
        return post;
    }

    @Override
    public Post updatePost(Long id, Post post) {
        post.setId(id);
        postMapper.update(post);
        return post;
    }

    @Override
    public boolean deletePost(Long id) {
        postMapper.delete(id);
        return true;
    }

    @Override
    public void incrementViews(Long id) {
        postMapper.updateViews(id);
    }
}