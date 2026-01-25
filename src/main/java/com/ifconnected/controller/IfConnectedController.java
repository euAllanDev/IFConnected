package com.ifconnected.controller;

import com.ifconnected.config.OpenApiConfig;
import com.ifconnected.model.DTO.CampusDTO;
import com.ifconnected.model.DTO.UpdateUserDTO;
import com.ifconnected.model.DTO.UserProfileDTO;
import com.ifconnected.model.DTO.UserResponseDTO;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.model.JPA.Event;
import com.ifconnected.model.JPA.Project;
import com.ifconnected.model.NOSQL.Notification;
import com.ifconnected.model.NOSQL.Post;
import com.ifconnected.repository.mongo.PostRepository;
import com.ifconnected.security.UserPrincipal;
import com.ifconnected.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "API", description = "Endpoints principais")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
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
    private final ProjectService projectService;

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

    // --- LEITURAS GERAIS ---

    @Operation(summary = "Listar todos os usuários (público)")
    @GetMapping("/users")
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsersDTO();
    }

    @Operation(summary = "Listar campus (público)")
    @GetMapping("/campus")
    public List<CampusDTO> getAllCampuses() {
        return campusService.getAll();
    }

    // --- PERFIL E SEGUIR ---

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

        // Notificação de Follow (precisa do "username" do usuário)
        UserResponseDTO follower = userService.getUserById(followerId);

        notificationService.createNotification(
                followedId,
                followerId,
                follower.username(),
                "FOLLOW",
                null
        );
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

    @PatchMapping("/users/{id}/campus")
    public void updateUserCampus(@PathVariable Long id, @RequestBody Long campusId) {
        userService.updateCampus(id, campusId);
    }

    @PostMapping(value = "/users/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponseDTO uploadProfilePhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String imageUrl = minioService.uploadImage(file);

        User entity = userService.getUserEntityById(id);
        if (entity == null) throw new RuntimeException("Usuário não encontrado");

        entity.setProfileImageUrl(imageUrl);
        return userService.updateUserEntity(entity);
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

        if (post.getComments() == null) {
            post.setComments(new ArrayList<>());
        }

        comment.setPostedAt(java.time.LocalDateTime.now());
        if (comment.getCommentId() == null) {
            comment.setCommentId(java.util.UUID.randomUUID().toString());
        }

        post.getComments().add(comment);
        Post savedPost = postRepository.save(post);

        // Notificação Comment
        if (!post.getUserId().equals(comment.getUserId())) {
            UserResponseDTO sender = userService.getUserById(comment.getUserId());

            notificationService.createNotification(
                    post.getUserId(),
                    comment.getUserId(),
                    sender.username(),
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
            likes.remove(userId);
        } else {
            likes.add(userId);

            // Notificação Like
            if (!post.getUserId().equals(userId)) {
                UserResponseDTO sender = userService.getUserById(userId);
                notificationService.createNotification(
                        post.getUserId(),
                        userId,
                        sender.username(),
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
        // ✅ aqui precisa de entidade (campusId)
        User user = userService.getUserEntityById(userId);
        if (user == null) throw new RuntimeException("Usuário não encontrado");

        if (user.getCampusId() == null) {
            throw new RuntimeException("Usuário não tem campus vinculado! Selecione seu IF no perfil.");
        }
        return geoFeedService.getNearbyCampusFeed(user.getCampusId(), radiusKm);
    }

    @GetMapping("/users/{id}/suggestions")
    public List<UserResponseDTO> getSuggestions(@PathVariable Long id,
                                                @RequestParam(defaultValue = "50") double radiusKm) {
        User user = userService.getUserEntityById(id);
        if (user == null) throw new RuntimeException("Usuário não encontrado");
        if (user.getCampusId() == null) return new ArrayList<>();

        List<User> suggestions = geoFeedService.getPeopleYouMightKnow(id, user.getCampusId(), radiusKm);

        return suggestions.stream()
                .map(userService::toUserResponse)
                .toList();
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

    @PutMapping("/events/{id}")
    public Event updateEvent(@PathVariable Long id, @RequestBody Event event) {
        return eventService.updateEvent(id, event);
    }

    @DeleteMapping("/events/{id}")
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
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

    // --- PROJETOS ---

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
        Project updates = new Project();
        updates.setTitle(title);
        updates.setDescription(description);
        updates.setGithubUrl(githubUrl);
        updates.setDemoUrl(demoUrl);
        updates.setTechnologies(technologies);

        if (file != null && !file.isEmpty()) {
            String imageUrl = minioService.uploadImage(file);
            updates.setImageUrl(imageUrl);
        }

        return projectService.update(id, updates);
    }

    @Operation(summary = "Usuário logado", description = "Retorna os dados públicos do usuário autenticado.")
    @GetMapping("/me")
    public UserResponseDTO me(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return userService.getUserById(principal.getId());
    }
}
