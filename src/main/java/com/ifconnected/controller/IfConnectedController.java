package com.ifconnected.controller;

import com.ifconnected.model.DTO.CampusDTO;
import com.ifconnected.model.DTO.UserProfileDTO;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.model.JPA.Event;
import com.ifconnected.model.JPA.Project;
import com.ifconnected.model.NOSQL.Notification; // Import Notification
import com.ifconnected.model.NOSQL.Post;

import com.ifconnected.repository.mongo.PostRepository;
import com.ifconnected.service.*;

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
    private final GeoFeedService geoFeedService;
    private final EventService eventService;
    private final NotificationService notificationService; // Novo Serviço
    private final CampusService campusService; // <--- NOVA INJEÇÃO
    private final ProjectService projectService;


    // CONSTRUTOR ÚNICO (Atualizado com NotificationService)
    public IfConnectedController(UserService userService,
                                 PostRepository postRepository,
                                 MinioService minioService,
                                 GeoFeedService geoFeedService,
                                 EventService eventService,
                                 NotificationService notificationService,
                                 CampusService campusService,
                                 ProjectService projectService
    ) {
        this.userService = userService;
        this.postRepository = postRepository;
        this.minioService = minioService;
        this.geoFeedService = geoFeedService;
        this.eventService = eventService;
        this.notificationService = notificationService;
        this.campusService = campusService;
        this.projectService = projectService;
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }



    @GetMapping("/campus")
    public List<CampusDTO> getAllCampuses() {
        return campusService.getAll();
    }

    // --- LOGIN & AUTH ---

    @PostMapping("/login")
    public User login(@RequestBody User loginData) {
        return userService.login(loginData.getEmail());
    }

    // --- USUÁRIOS & SEGUIR ---

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

        // --- GATILHO NOTIFICAÇÃO: FOLLOW ---
        User follower = userService.getUserById(followerId);
        notificationService.createNotification(
                followedId,             // Quem recebe (o seguido)
                followerId,             // Quem enviou (o seguidor)
                follower.getUsername(), // Nome de quem seguiu
                "FOLLOW",
                null
        );
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
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        // Garante que a lista existe (embora a classe Post agora já garanta)
        if(post.getComments() == null) {
            post.setComments(new ArrayList<>());
        }

        // Força a data e o ID no momento da adição para garantir unicidade
        comment.setPostedAt(java.time.LocalDateTime.now());
        if (comment.getCommentId() == null) {
            comment.setCommentId(java.util.UUID.randomUUID().toString());
        }

        post.getComments().add(comment);

        // Salva primeiro para garantir consistência
        Post savedPost = postRepository.save(post);

        // Notificação
        if (!post.getUserId().equals(comment.getUserId())) {
            User sender = userService.getUserById(comment.getUserId());
            notificationService.createNotification(
                    post.getUserId(),
                    comment.getUserId(),
                    sender.getUsername(),
                    "COMMENT",
                    postId
            );
        }

        return savedPost;
    }

    @PostMapping("/posts/{postId}/like")
    public Post toggleLike(@PathVariable String postId, @RequestParam Long userId) {
        Post post = postRepository.findById(postId).orElseThrow();
        List<Long> likes = post.getLikes();

        if (likes == null) likes = new ArrayList<>();

        if (likes.contains(userId)) {
            likes.remove(userId); // Descurtiu
        } else {
            likes.add(userId); // Curtiu

            // --- GATILHO NOTIFICAÇÃO: LIKE ---
            // Só notifica se não for o dono curtindo o próprio post
            if (!post.getUserId().equals(userId)) {
                User sender = userService.getUserById(userId);
                notificationService.createNotification(
                        post.getUserId(),
                        userId,
                        sender.getUsername(),
                        "LIKE",
                        postId
                );
            }
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

    @PostMapping("/events")
    public Event createEvent(@RequestBody Event event) {
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

    // --- NOTIFICAÇÕES (MongoDB) ---

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

    @PutMapping("/events/{id}")
    public Event updateEvent(@PathVariable Long id, @RequestBody Event event) {
        return eventService.updateEvent(id, event);
    }

    @DeleteMapping("/events/{id}")
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }

    @GetMapping("/users/{userId}/projects")
    public List<Project> getUserProjects(@PathVariable Long userId) {
        return projectService.getProjectsByUser(userId);
    }

    @PostMapping(value = "/projects", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Project createProject(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "githubUrl", required = false) String githubUrl,
            @RequestParam(value = "demoUrl", required = false) String demoUrl,
            @RequestParam(value = "technologies", required = false) List<String> technologies,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        Project project = new Project();
        project.setTitle(title);
        project.setDescription(description);
        project.setUserId(userId);
        project.setGithubUrl(githubUrl);
        project.setDemoUrl(demoUrl);
        project.setTechnologies(technologies);

        if (file != null && !file.isEmpty()) {
            String imageUrl = minioService.uploadImage(file);
            project.setImageUrl(imageUrl);
        }

        return projectService.save(project);
    }

    @DeleteMapping("/projects/{id}")
    public void deleteProject(@PathVariable Long id) {
        projectService.delete(id);
    }

    @PutMapping(value = "/projects/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Project updateProject(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "githubUrl", required = false) String githubUrl,
            @RequestParam(value = "demoUrl", required = false) String demoUrl,
            @RequestParam(value = "technologies", required = false) List<String> technologies,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        Project projectUpdates = new Project();
        projectUpdates.setTitle(title);
        projectUpdates.setDescription(description);
        projectUpdates.setGithubUrl(githubUrl);
        projectUpdates.setDemoUrl(demoUrl);
        projectUpdates.setTechnologies(technologies);

        if (file != null && !file.isEmpty()) {
            String imageUrl = minioService.uploadImage(file);
            projectUpdates.setImageUrl(imageUrl);
        }

        return projectService.update(id, projectUpdates);
    }
}