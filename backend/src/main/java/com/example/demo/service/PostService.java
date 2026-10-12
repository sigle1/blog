package com.example.demo.service;

import com.example.demo.entity.Post;
import java.util.List;

public interface PostService {
    List<Post> getAllPosts();
    Post getPostById(Long id);
    Post createPost(Post post);
    Post updatePost(Long id, Post post);
    boolean deletePost(Long id);
    void incrementViews(Long id);
}