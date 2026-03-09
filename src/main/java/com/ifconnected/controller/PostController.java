package com.ifconnected.controller;

import com.ifconnected.model.NOSQL.Post;
import com.ifconnected.service.PostService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    public Post getPostById(@PathVariable String id) {
        return postService.getPostById(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Post createPost(@RequestParam("userId") Long userId,
                           @RequestParam("content") String content,
                           @RequestParam(value = "file", required = false) MultipartFile file) {
        return postService.createPost(userId, content, file);
    }

    @GetMapping("/feed/{userId}")
    public List<Post> getFriendsFeed(@PathVariable Long userId) {
        return postService.getFriendsFeed(userId);
    }

    @GetMapping("/user/{userId}")
    public List<Post> getPostsByUser(@PathVariable Long userId) {
        return postService.getPostsByUser(userId);
    }

    @GetMapping("/feed/regional")
    public List<Post> getRegionalFeed(@RequestParam Long userId,
                                      @RequestParam(defaultValue = "50") double radiusKm) {
        return postService.getRegionalFeed(userId, radiusKm);
    }

    @PostMapping("/{postId}/comments")
    public Post addComment(@PathVariable String postId, @RequestBody Post.Comment comment) {
        return postService.addComment(postId, comment);
    }

    @PostMapping("/{postId}/like")
    public Post toggleLike(@PathVariable String postId, @RequestParam Long userId) {
        return postService.toggleLike(postId, userId);
    }
}