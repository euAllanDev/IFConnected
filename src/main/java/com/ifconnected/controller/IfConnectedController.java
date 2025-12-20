package com.ifconnected.controller;

import com.ifconnected.model.DTO.UserProfileDTO;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.model.JPA.Event; // Import da entidade JPA
import com.ifconnected.model.NOSQL.Post;

import com.ifconnected.repository.mongo.PostRepository;
import com.ifconnected.service.EventService; // Import do Service de Eventos
import com.ifconnected.service.MinioService;
import com.ifconnected.service.UserService;
import com.ifconnected.service.GeoFeedService;

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
    private final EventService eventService; // Novo Serviço Injetado

    // CONSTRUTOR ÚNICO (Atualizado com EventService)
    public IfConnectedController(UserService userService,
                                 PostRepository postRepository,
                                 MinioService minioService,
                                 GeoFeedService geoFeedService,
                                 EventService eventService) {
        this.userService = userService;
        this.postRepository = postRepository;
        this.minioService = minioService;
        this.geoFeedService = geoFeedService;
        this.eventService = eventService;
    }

    // --- LOGIN & AUTH ---

    @PostMapping("/login")
    public User login(@RequestBody User loginData) {
        return userService.login(loginData.getEmail());
    }

    // --- USUÁRIOS & SEGUIR ---

    // GET /api/users/{followerId}/isFollowing/{followedId}
    @GetMapping("/users/{followerId}/isFollowing/{followedId}")
    public boolean isFollowing(@PathVariable Long followerId, @PathVariable Long followedId) {
        return userService.isFollowing(followerId, followedId);
    }

    @DeleteMapping("/users/{followerId}/follow/{followedId}")
    public void unfollowUser(@PathVariable Long followerId, @PathVariable Long followedId) {
        userService.unfollow(followerId, followedId);
    }

    @PostMapping("/users/{followerId}/follow/{followedId}")
    public void followUser(@PathVariable Long followerId, @PathVariable Long followedId) {
        userService.follow(followerId, followedId);
    }

    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/users/{id}/profile")
    public UserProfileDTO getUserProfile(@PathVariable Long id) {
        return userService.getUserProfile(id);
    }

    @PutMapping("/users/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return userService.updateUser(user);
    }

    @PatchMapping("/users/{id}/campus")
    public void updateUserCampus(@PathVariable Long id, @RequestBody Long campusId) {
        userService.updateCampus(id, campusId);
    }

    @PostMapping(value = "/users/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public User uploadProfilePhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String imageUrl = minioService.uploadImage(file);
        User user = userService.getUserById(id);
        user.setProfileImageUrl(imageUrl);
        return userService.updateUser(user);
    }

    // --- POSTS (NoSQL) ---

    @GetMapping("/posts/{id}")
    public Post getPostById(@PathVariable String id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));
    }

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

        if (likes == null) likes = new ArrayList<>();

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
            throw new RuntimeException("Usuário não tem campus vinculado! Selecione seu IF no perfil.");
        }
        return geoFeedService.getNearbyCampusFeed(user.getCampusId(), radiusKm);
    }

    @GetMapping("/users/{id}/suggestions")
    public List<User> getSuggestions(@PathVariable Long id,
                                     @RequestParam(defaultValue = "50") double radiusKm) {
        User user = userService.getUserById(id);
        if (user.getCampusId() == null) return new ArrayList<>();

        return geoFeedService.getPeopleYouMightKnow(id, user.getCampusId(), radiusKm);
    }

    // --- EVENTOS (JPA) ---

    // Criar um novo evento
    @PostMapping("/events")
    public Event createEvent(@RequestBody Event event) {
        return eventService.createEvent(event);
    }

    // Listar eventos de um campus
    @GetMapping("/events/campus/{campusId}")
    public List<Event> listEventsByCampus(@PathVariable Long campusId) {
        return eventService.getEventsByCampus(campusId);
    }

    // Participar do evento
    @PostMapping("/events/{id}/join")
    public void joinEvent(@PathVariable Long id, @RequestParam Long userId) {
        // true = participar
        eventService.toggleParticipation(id, userId, true);
    }

    // Sair do evento
    @PostMapping("/events/{id}/leave")
    public void leaveEvent(@PathVariable Long id, @RequestParam Long userId) {
        // false = sair
        eventService.toggleParticipation(id, userId, false);
    }
}