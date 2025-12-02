package com.ifconnected.controller;


// --- CORREÇÃO AQUI: Importar dos pacotes corretos criados anteriormente ---
import com.ifconnected.model.NOSQL.Post;
import com.ifconnected.model.JDBC.User;

import com.ifconnected.repository.mongo.PostRepository;
import com.ifconnected.service.MinioService;
import com.ifconnected.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api")
public class IfConnectedController {

    private final UserService userService;
    private final PostRepository postRepository;
    private final MinioService minioService;

    // Construtor Manual (Correto para evitar erro de inicialização)
    public IfConnectedController(UserService userService,
                                 PostRepository postRepository,
                                 MinioService minioService) {
        this.userService = userService;
        this.postRepository = postRepository;
        this.minioService = minioService;
    }

    // --- RELACIONAL + REDIS ---

    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping("/users/{followerId}/follow/{followedId}")
    public void followUser(@PathVariable Long followerId, @PathVariable Long followedId) {
        userService.follow(followerId, followedId);
    }

    // --- NOSQL + MINIO ---

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Post createPost(@RequestParam("userId") Long userId,
                           @RequestParam("content") String content,
                           @RequestParam("file") MultipartFile file) {

        // 1. Upload imagem MinIO
        String imageUrl = minioService.uploadImage(file);

        // 2. Salvar metadados no Mongo
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(content);
        post.setImageUrl(imageUrl);

        return postRepository.save(post);
    }

    @GetMapping("/posts/user/{userId}")
    public List<Post> getPostsByUser(@PathVariable Long userId) {
        return postRepository.findByUserId(userId);
    }

    @PostMapping("/posts/{postId}/comments")
    public Post addComment(@PathVariable String postId, @RequestBody Post.Comment comment) {
        Post post = postRepository.findById(postId).orElseThrow();
        if(post.getComments() == null) {
            post.setComments(new ArrayList<>());
        }
        post.getComments().add(comment);
        return postRepository.save(post);
    }
}