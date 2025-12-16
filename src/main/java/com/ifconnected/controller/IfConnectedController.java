package com.ifconnected.controller;

import com.ifconnected.model.DTO.UserProfileDTO;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.model.NOSQL.Post;

import com.ifconnected.repository.mongo.PostRepository;
import com.ifconnected.service.MinioService;
import com.ifconnected.service.UserService;
import com.ifconnected.service.GeoFeedService; // Import do GeoService

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api")
public class IfConnectedController {

    // Todos final para garantir imutabilidade e injeção via construtor
    private final UserService userService;
    private final PostRepository postRepository;
    private final MinioService minioService;
    private final GeoFeedService geoFeedService;

    // CONSTRUTOR ÚNICO (A melhor prática do Spring)
    public IfConnectedController(UserService userService,
                                 PostRepository postRepository,
                                 MinioService minioService,
                                 GeoFeedService geoFeedService) {
        this.userService = userService;
        this.postRepository = postRepository;
        this.minioService = minioService;
        this.geoFeedService = geoFeedService;
    }
    @PostMapping("/login")
    public User login(@RequestBody User loginData) {
        return userService.login(loginData.getEmail());
    }


    // GET /api/users/{followerId}/isFollowing/{followedId}
    @GetMapping("/users/{followerId}/isFollowing/{followedId}")
    public boolean isFollowing(@PathVariable Long followerId, @PathVariable Long followedId) {
        return userService.isFollowing(followerId, followedId);
    }

    @DeleteMapping("/users/{followerId}/follow/{followedId}")
    public void unfollowUser(@PathVariable Long followerId, @PathVariable Long followedId) {
        // CORREÇÃO AQUI: Chama o UserService
        userService.unfollow(followerId, followedId);
    }

    // Rota usada pelo seu Register.tsx
    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        // Ao criar aqui, o campusId virá nulo.
        // O Front deve pedir a localização logo após o login para preencher isso.
        return userService.createUser(user);
    }

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // GET /api/posts/{id}
    @GetMapping("/posts/{id}")
    public Post getPostById(@PathVariable String id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));
    }

    @GetMapping("/users/{id}/profile")
    public UserProfileDTO getUserProfile(@PathVariable Long id) {
        return userService.getUserProfile(id);
    }

    // ATUALIZAR PERFIL (PUT)
    @PutMapping("/users/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return userService.updateUser(user);
    }

    // PATCH específico para vincular Campus (Útil para o modal de localização)
    @PatchMapping("/users/{id}/campus")
    public void updateUserCampus(@PathVariable Long id, @RequestBody Long campusId) {
        userService.updateCampus(id, campusId);
        // Precisa garantir que esse método existe no UserService -> UserRepository
    }

    // UPLOAD DE FOTO DE PERFIL (POST)
    @PostMapping(value = "/users/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public User uploadProfilePhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String imageUrl = minioService.uploadImage(file);
        User user = userService.getUserById(id);
        user.setProfileImageUrl(imageUrl);
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

    @GetMapping("/posts")
    public List<Post> getAllPosts() {
        return postRepository.findAll();
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

    @PostMapping("/posts/{postId}/comments")
    public Post addComment(@PathVariable String postId, @RequestBody Post.Comment comment) {
        Post post = postRepository.findById(postId).orElseThrow();
        if(post.getComments() == null) {
            post.setComments(new ArrayList<>());
        }
        post.getComments().add(comment);
        return postRepository.save(post);
    }

    @PostMapping("/posts/{postId}/like")
    public Post toggleLike(@PathVariable String postId, @RequestParam Long userId) {
        Post post = postRepository.findById(postId).orElseThrow();
        List<Long> likes = post.getLikes();

        if (likes == null) likes = new ArrayList<>(); // Proteção contra null

        if (likes.contains(userId)) {
            likes.remove(userId);
        } else {
            likes.add(userId);
        }

        post.setLikes(likes);
        return postRepository.save(post);
    }

    // --- GEOLOCALIZAÇÃO ---

    @GetMapping("/posts/feed/regional")
    public List<Post> getRegionalFeed(@RequestParam Long userId,
                                      @RequestParam(defaultValue = "50") double radiusKm) {
        User user = userService.getUserById(userId);
        if (user.getCampusId() == null) {
            // Retorna lista vazia ou erro 400 se o usuário não escolheu campus ainda
            throw new RuntimeException("Usuário não tem campus vinculado! Selecione seu IF no perfil.");
        }
        return geoFeedService.getNearbyCampusFeed(user.getCampusId(), radiusKm);
    }

    @GetMapping("/users/{id}/suggestions")
    public List<User> getSuggestions(@PathVariable Long id,
                                     @RequestParam(defaultValue = "50") double radiusKm) {
        User user = userService.getUserById(id);
        if (user.getCampusId() == null) return new ArrayList<>(); // Sem campus, sem sugestão

        return geoFeedService.getPeopleYouMightKnow(id, user.getCampusId(), radiusKm);
    }
}