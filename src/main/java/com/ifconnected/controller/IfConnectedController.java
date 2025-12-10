package com.ifconnected.controller;

import com.ifconnected.model.JDBC.User;
import com.ifconnected.model.NOSQL.Post;

import com.ifconnected.repository.mongo.PostRepository;
import com.ifconnected.service.MinioService;
import com.ifconnected.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ifconnected.service.GeoFeedService;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api")
public class IfConnectedController {
    @Autowired
    private GeoFeedService geoFeedService;
    private final UserService userService;
    private final PostRepository postRepository;
    private final MinioService minioService;

    public IfConnectedController(UserService userService,
                                 PostRepository postRepository,
                                 MinioService minioService) {
        this.userService = userService;
        this.postRepository = postRepository;
        this.minioService = minioService;
    }

    // --- RELACIONAL + REDIS (Usuários) ---

    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // ATUALIZAR PERFIL (PUT)
    @PutMapping("/users/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        // Garante que o ID do objeto é o mesmo da URL
        user.setId(id);
        // Chama o método novo unificado
        return userService.updateUser(user);
    }

    // UPLOAD DE FOTO DE PERFIL (POST)
    @PostMapping(value = "/users/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public User uploadProfilePhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        // 1. Upload imagem MinIO
        String imageUrl = minioService.uploadImage(file);

        // 2. Busca usuário atual
        User user = userService.getUserById(id);

        // 3. Atualiza URL da foto no objeto
        user.setProfileImageUrl(imageUrl);

        // 4. Salva usando o método unificado (CORREÇÃO AQUI)
        return userService.updateUser(user);
    }

    @PostMapping("/users/{followerId}/follow/{followedId}")
    public void followUser(@PathVariable Long followerId, @PathVariable Long followedId) {
        userService.follow(followerId, followedId);
    }

    // --- NOSQL + MINIO (Posts) ---

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Post createPost(@RequestParam("userId") Long userId,
                           @RequestParam("content") String content,
                           @RequestParam(value = "file", required = false) MultipartFile file) {

        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            imageUrl = minioService.uploadImage(file);
        }

        Post post = new Post();
        post.setUserId(userId);
        post.setContent(content);
        post.setImageUrl(imageUrl);

        return postRepository.save(post);
    }

    // Feed Global (For You)
    @GetMapping("/posts")
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    // Feed de Amigos (Friends)
    @GetMapping("/posts/feed/{userId}")
    public List<Post> getFriendsFeed(@PathVariable Long userId) {
        List<Long> followingIds = userService.getFollowingIds(userId);
        followingIds.add(userId); // Inclui o próprio usuário
        return postRepository.findByUserIdIn(followingIds);
    }

    // Feed de um usuário específico (Perfil)
    @GetMapping("/posts/user/{userId}")
    public List<Post> getPostsByUser(@PathVariable Long userId) {
        return postRepository.findByUserId(userId);
    }

    // Comentar
    @PostMapping("/posts/{postId}/comments")
    public Post addComment(@PathVariable String postId, @RequestBody Post.Comment comment) {
        Post post = postRepository.findById(postId).orElseThrow();
        if(post.getComments() == null) {
            post.setComments(new ArrayList<>());
        }
        post.getComments().add(comment);
        return postRepository.save(post);
    }

    // Dar Like
    @PostMapping("/posts/{postId}/like")
    public Post toggleLike(@PathVariable String postId, @RequestParam Long userId) {
        Post post = postRepository.findById(postId).orElseThrow();

        List<Long> likes = post.getLikes();
        if (likes.contains(userId)) {
            likes.remove(userId);
        } else {
            likes.add(userId);
        }

        post.setLikes(likes);
        return postRepository.save(post);
    }

    @GetMapping("/posts/feed/regional")
    public List<Post> getRegionalFeed(@RequestParam Long userId,
                                      @RequestParam(defaultValue = "50") double radiusKm) {

        User user = userService.getUserById(userId);
        if (user.getCampusId() == null) {
            throw new RuntimeException("Usuário não tem campus vinculado!");
        }

        return geoFeedService.getNearbyCampusFeed(user.getCampusId(), radiusKm);
    }

    // Sugestão de Amigos
    @GetMapping("/users/{id}/suggestions")
    public List<User> getSuggestions(@PathVariable Long id,
                                     @RequestParam(defaultValue = "50") double radiusKm) {

        User user = userService.getUserById(id);
        return geoFeedService.getPeopleYouMightKnow(id, user.getCampusId(), radiusKm);
    }
}