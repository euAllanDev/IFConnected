package com.ifconnected.controller;

import com.ifconnected.model.DTO.*;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.model.JPA.Event;
import com.ifconnected.model.NOSQL.Notification;
import com.ifconnected.model.NOSQL.Post;
import com.ifconnected.repository.mongo.PostRepository;
import com.ifconnected.security.TokenService;
import com.ifconnected.security.UserLoginInfo;
import com.ifconnected.service.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class IfConnectedController {

    private final UserService userService;
    private final PostRepository postRepository;
    private final MinioService minioService;
    private final GeoFeedService geoFeedService;
    private final EventService eventService;
    private final NotificationService notificationService;
    private final CampusService campusService;
    private final GoogleAuthService googleAuthService;
    private final TokenService tokenService; // ✅ Adicionado

    public IfConnectedController(UserService userService,
                                 PostRepository postRepository,
                                 MinioService minioService,
                                 GeoFeedService geoFeedService,
                                 EventService eventService,
                                 NotificationService notificationService,
                                 CampusService campusService,
                                 GoogleAuthService googleAuthService,
                                 TokenService tokenService) { // ✅ Adicionado no construtor
        this.userService = userService;
        this.postRepository = postRepository;
        this.minioService = minioService;
        this.geoFeedService = geoFeedService;
        this.eventService = eventService;
        this.notificationService = notificationService;
        this.campusService = campusService;
        this.googleAuthService = googleAuthService;
        this.tokenService = tokenService; // ✅ Inicializado
    }

    // --- LOGIN & AUTH ---

    @PostMapping("/login")
    public UserResponseDTO login(@RequestBody User loginData) {
        return userService.login(loginData.getEmail());
    }

    @PostMapping("/auth/google")
    public ResponseEntity<Map<String, Object>> googleLogin(@RequestBody GoogleLoginDTO dto) {
        // 1. Valida o token do Google e retorna o User (criado ou buscado)
        User user = googleAuthService.authenticateGoogleUser(dto.token());

        // 2. Gera o token JWT interno
        String jwtToken = tokenService.generateToken(new UserLoginInfo(user));

        // 3. Retorna o pacote completo para o Frontend
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwtToken);
        response.put("user", new UserResponseDTO(user));

        return ResponseEntity.ok(response);
    }

    // --- USUÁRIOS ---

    @GetMapping("/users")
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/users")
    public UserResponseDTO createUser(@RequestBody RegisterDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setBio(dto.getBio());
        user.setProfileImageUrl(dto.getProfileImageUrl());
        user.setCampusId(dto.getCampusId());
        user.setRole(dto.getRole());
        return userService.createUser(user);
    }

    @GetMapping("/users/{id}")
    public UserResponseDTO getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/users/{id}/profile")
    public UserProfileDTO getUserProfile(@PathVariable Long id) {
        return userService.getUserProfile(id);
    }

    @PutMapping("/users/{id}")
    public UserResponseDTO updateUser(@PathVariable Long id, @RequestBody UpdateUserDTO dto) {
        return userService.updateUser(id, dto);
    }

    @PostMapping(value = "/users/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponseDTO uploadProfilePhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String imageUrl = minioService.uploadImage(file);
        return userService.updateProfileImage(id, imageUrl);
    }

    @PatchMapping("/users/{id}/campus")
    public void updateUserCampus(@PathVariable Long id, @RequestBody Long campusId) {
        userService.updateCampus(id, campusId);
    }

    // --- CAMPUS ---

    @GetMapping("/campus")
    public List<CampusDTO> getAllCampuses() {
        return campusService.getAll();
    }

    // --- SOCIAL & FOLLOW ---

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
        User follower = userService.getUserEntityById(followerId);
        notificationService.createNotification(followedId, followerId, follower.getUsername(), "FOLLOW", null);
    }

    @GetMapping("/users/{id}/suggestions")
    public List<User> getSuggestions(@PathVariable Long id, @RequestParam(defaultValue = "50") double radiusKm) {
        User user = userService.getUserEntityById(id);
        if (user.getCampusId() == null) return new ArrayList<>();
        return geoFeedService.getPeopleYouMightKnow(id, user.getCampusId(), radiusKm);
    }

    // --- POSTS ---

    @GetMapping("/posts/{id}")
    public Post getPostById(@PathVariable String id) {
        return postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post não encontrado"));
    }

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Post createPost(@RequestParam("userId") Long userId, @RequestParam("content") String content, @RequestParam(value = "file", required = false) MultipartFile file) {
        String imageUrl = null;
        if (file != null && !file.isEmpty()) imageUrl = minioService.uploadImage(file);
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

    // --- EVENTOS ---

    @PostMapping("/events")
    public Event createEvent(@RequestBody Event event) {
        return eventService.createEvent(event);
    }

    @PutMapping("/events/{id}")
    public Event updateEvent(@PathVariable Long id, @RequestBody Event event) {
        event.setId(id);
        return eventService.createEvent(event);
    }

    @GetMapping("/events/campus/{campusId}")
    public List<Event> listEventsByCampus(@PathVariable Long campusId) {
        return eventService.getEventsByCampus(campusId);
    }

    @PostMapping("/events/{id}/join")
    public void joinEvent(@PathVariable Long id, @RequestParam Long userId) {
        eventService.toggleParticipation(id, userId, true);
    }

    @PostMapping("/events/{id}/leave")
    public void leaveEvent(@PathVariable Long id, @RequestParam Long userId) {
        eventService.toggleParticipation(id, userId, false);
    }

    // --- NOTIFICAÇÕES ---

    @GetMapping("/notifications/user/{userId}")
    public List<Notification> getUserNotifications(@PathVariable Long userId) {
        return notificationService.getNotifications(userId);
    }

    @PutMapping("/notifications/user/{userId}/read")
    public void markNotificationsAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
    }

    @GetMapping("/notifications/user/{userId}/count")
    public long getUnreadNotificationCount(@PathVariable Long userId) {
        return notificationService.getUnreadCount(userId);
    }
}