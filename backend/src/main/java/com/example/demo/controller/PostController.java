package com.example.demo.controller;

import com.example.demo.entity.Post;
import com.example.demo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/posts", "/admin/posts"})
public class PostController {
    @Autowired
    private PostService postService;

    @GetMapping
    public List<Post> getPosts(@RequestParam(required = false) Integer limit) {
        List<Post> posts = postService.getAllPosts();
        if (limit != null && limit > 0 && posts.size() > limit) {
            return posts.subList(0, limit);
        }
        return posts;
    }

    @GetMapping("/{id}")
    public Post getPostById(@PathVariable Long id) {
        postService.incrementViews(id); // 增加阅读量
        return postService.getPostById(id);
    }

    @PostMapping
    public Post createPost(@RequestBody Post post) {
        post.setCreatedAt(LocalDateTime.now());
        if (post.getViews() == null) {
            post.setViews(0);
        }
        if (post.getLikes() == null) {
            post.setLikes(0);
        }
        return postService.createPost(post);
    }

    @PutMapping("/{id}")
    public Post updatePost(@PathVariable Long id, @RequestBody Post post) {
        return postService.updatePost(id, post);
    }

    @DeleteMapping("/{id}")
    public boolean deletePost(@PathVariable Long id) {
        return postService.deletePost(id);
    }
}

