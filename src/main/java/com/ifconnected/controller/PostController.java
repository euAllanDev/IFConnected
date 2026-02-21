package com.ifconnected.controller;

import com.ifconnected.model.JDBC.User;
import com.ifconnected.model.NOSQL.Post;
import com.ifconnected.repository.mongo.PostRepository;
import com.ifconnected.service.GeoFeedService;
import com.ifconnected.service.MinioService;
import com.ifconnected.service.NotificationService;
import com.ifconnected.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PostController {

    private final PostRepository postRepository;
    private final MinioService minioService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final GeoFeedService geoFeedService;

    public PostController(PostRepository postRepository, MinioService minioService,
                          UserService userService, NotificationService notificationService,
                          GeoFeedService geoFeedService) {
        this.postRepository = postRepository;
        this.minioService = minioService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.geoFeedService = geoFeedService;
    }

    @GetMapping("/posts")
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @GetMapping("/posts/{id}")
    public Post getPostById(@PathVariable String id) {
        return postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post não encontrado"));
    }

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Post createPost(@RequestParam("userId") Long userId,
                           @RequestParam("content") String content,
                           @RequestParam(value = "file", required = false) MultipartFile file) {
        String imageUrl = null;
        if (file != null && !file.isEmpty()) imageUrl = minioService.uploadImage(file);

        Post post = new Post();
        post.setUserId(userId);
        post.setContent(content);
        post.setImageUrl(imageUrl);
        return postRepository.save(post);
    }

    @GetMapping("/posts/feed/{userId}")
    public List<Post> getFriendsFeed(@PathVariable Long userId) {
        List<Long> followingIds = userService.getFollowingIds(userId);
        followingIds.add(userId);
        return postRepository.findByUserIdIn(followingIds);
    }

    @GetMapping("/posts/user/{userId}")
    public List<Post> getPostsByUser(@PathVariable Long userId) {
        return postRepository.findByUserId(userId);
    }

    @GetMapping("/posts/feed/regional")
    public List<Post> getRegionalFeed(@RequestParam Long userId, @RequestParam(defaultValue = "50") double radiusKm) {
        User user = userService.getUserEntityById(userId);
        if (user.getCampusId() == null) throw new RuntimeException("Usuário não tem campus vinculado!");
        return geoFeedService.getNearbyCampusFeed(user.getCampusId(), radiusKm);
    }

    @PostMapping("/posts/{postId}/comments")
    public Post addComment(@PathVariable String postId, @RequestBody Post.Comment comment) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post não encontrado"));
        if(post.getComments() == null) post.setComments(new ArrayList<>());

        comment.setPostedAt(LocalDateTime.now());
        if (comment.getCommentId() == null) comment.setCommentId(UUID.randomUUID().toString());
        post.getComments().add(comment);

        Post savedPost = postRepository.save(post);

        if (!post.getUserId().equals(comment.getUserId())) {
            User sender = userService.getUserEntityById(comment.getUserId());
            notificationService.createNotification(post.getUserId(), comment.getUserId(), sender.getUsername(), "COMMENT", postId);
        }
        return savedPost;
    }

    @PostMapping("/posts/{postId}/like")
    public Post toggleLike(@PathVariable String postId, @RequestParam Long userId) {
        Post post = postRepository.findById(postId).orElseThrow();
        List<Long> likes = post.getLikes();
        if (likes == null) likes = new ArrayList<>();

        if (likes.contains(userId)) {
            likes.remove(userId);
        } else {
            likes.add(userId);
            if (!post.getUserId().equals(userId)) {
                User sender = userService.getUserEntityById(userId);
                notificationService.createNotification(post.getUserId(), userId, sender.getUsername(), "LIKE", postId);
            }
        }
        post.setLikes(likes);
        return postRepository.save(post);
    }
}