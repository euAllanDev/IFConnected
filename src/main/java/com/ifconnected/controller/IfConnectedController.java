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
import com.ifconnected.security.UserLoginInfo;
import com.ifconnected.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "API", description = "Endpoints principais do IFConnected")
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
                                 ProjectService projectService) {
        this.userService = userService;
        this.postRepository = postRepository;
        this.minioService = minioService;
        this.geoFeedService = geoFeedService;
        this.eventService = eventService;
        this.notificationService = notificationService;
        this.campusService = campusService;
        this.projectService = projectService;
    }

    // ---------------- USERS ----------------

    @Operation(summary = "Listar usuários (dados públicos)")
    @GetMapping("/users")
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "Buscar usuário por ID (dados públicos)")
    @GetMapping("/users/{id}")
    public UserResponseDTO getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @Operation(summary = "Perfil do usuário (dados públicos + contadores)")
    @GetMapping("/users/{id}/profile")
    public UserProfileDTO getUserProfile(@PathVariable Long id) {
        return userService.getUserProfile(id);
    }

    @Operation(summary = "Atualizar usuário (username/bio/campusId)")
    @PutMapping("/users/{id}")
    public UserResponseDTO updateUser(@PathVariable Long id, @RequestBody UpdateUserDTO dto) {
        return userService.updateUser(id, dto);
    }

    @Operation(summary = "Atualizar campus do usuário")
    @PatchMapping("/users/{id}/campus")
    public void updateUserCampus(@PathVariable Long id, @RequestBody Long campusId) {
        userService.updateCampus(id, campusId);
    }

    @Operation(summary = "Upload de foto de perfil")
    @PostMapping(value = "/users/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponseDTO uploadProfilePhoto(@PathVariable Long id,
                                              @RequestParam("file") MultipartFile file) {
        String imageUrl = minioService.uploadImage(file);
        return userService.updateProfileImage(id, imageUrl);
    }

    @Operation(summary = "Retorna o usuário logado (dados públicos)")
    @GetMapping("/me")
    public UserResponseDTO me(@AuthenticationPrincipal UserLoginInfo principal) {
        return userService.getUserById(principal.getId());
    }

    // -------------- CAMPUS ----------------

    @Operation(summary = "Listar campi")
    @GetMapping("/campus")
    public List<CampusDTO> getAllCampuses() {
        return campusService.getAll();
    }

    // -------------- FOLLOW ----------------

    @Operation(summary = "Verifica se followerId segue followedId")
    @GetMapping("/users/{followerId}/isFollowing/{followedId}")
    public boolean isFollowing(@PathVariable Long followerId, @PathVariable Long followedId) {
        return userService.isFollowing(followerId, followedId);
    }

    @Operation(summary = "Seguir usuário")
    @PostMapping("/users/{followerId}/follow/{followedId}")
    public void followUser(@PathVariable Long followerId, @PathVariable Long followedId) {
        userService.follow(followerId, followedId);

        // notificação: pega entidade para username real
        User follower = userService.getUserEntityById(followerId);
        notificationService.createNotification(
                followedId,
                followerId,
                follower.getUsername(),
                "FOLLOW",
                null
        );
    }

    @Operation(summary = "Deixar de seguir usuário")
    @DeleteMapping("/users/{followerId}/follow/{followedId}")
    public void unfollowUser(@PathVariable Long followerId, @PathVariable Long followedId) {
        userService.unfollow(followerId, followedId);
    }

    // --------------- POSTS ----------------

    @Operation(summary = "Buscar post por ID")
    @GetMapping("/posts/{id}")
    public Post getPostById(@PathVariable String id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));
    }

    @Operation(summary = "Criar post (multipart)")
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

    @Operation(summary = "Listar todos os posts")
    @GetMapping("/posts")
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @Operation(summary = "Feed de quem eu sigo")
    @GetMapping("/posts/feed/{userId}")
    public List<Post> getFriendsFeed(@PathVariable Long userId) {
        List<Long> followingIds = userService.getFollowingIds(userId);
        followingIds.add(userId);
        return postRepository.findByUserIdIn(followingIds);
    }

    @Operation(summary = "Posts do usuário")
    @GetMapping("/posts/user/{userId}")
    public List<Post> getPostsByUser(@PathVariable Long userId) {
        return postRepository.findByUserId(userId);
    }

    @Operation(summary = "Comentar em um post")
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

        if (!post.getUserId().equals(comment.getUserId())) {
            User sender = userService.getUserEntityById(comment.getUserId());
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

    @Operation(summary = "Curtir/descurtir post")
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

    // ----------- GEO FEED / SUGGESTIONS -----------

    @Operation(summary = "Feed regional (por raio)")
    @GetMapping("/posts/feed/regional")
    public List<Post> getRegionalFeed(@RequestParam Long userId,
                                      @RequestParam(defaultValue = "50") double radiusKm) {
        User user = userService.getUserEntityById(userId);
        if (user.getCampusId() == null) {
            throw new RuntimeException("Usuário não tem campus vinculado! Selecione seu IF no perfil.");
        }
        return geoFeedService.getNearbyCampusFeed(user.getCampusId(), radiusKm);
    }

    @Operation(summary = "Sugestões de pessoas (por raio)")
    @GetMapping("/users/{id}/suggestions")
    public List<UserResponseDTO> getSuggestions(@PathVariable Long id,
                                                @RequestParam(defaultValue = "50") double radiusKm) {
        User me = userService.getUserEntityById(id);
        if (me.getCampusId() == null) return List.of();

        List<User> suggestions = geoFeedService.getPeopleYouMightKnow(id, me.getCampusId(), radiusKm);
        return suggestions.stream().map(this::toUserResponseDTO).toList();
    }

    private UserResponseDTO toUserResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getProfileImageUrl(),
                user.getCampusId()
        );
    }

    // ---------------- EVENTS ----------------

    @Operation(summary = "Criar evento")
    @PostMapping("/events")
    public Event createEvent(@RequestBody Event event) {
        return eventService.createEvent(event);
    }

    @Operation(summary = "Listar eventos por campus")
    @GetMapping("/events/campus/{campusId}")
    public List<Event> listEventsByCampus(@PathVariable Long campusId) {
        return eventService.getEventsByCampus(campusId);
    }

    @Operation(summary = "Participar de evento")
    @PostMapping("/events/{id}/join")
    public void joinEvent(@PathVariable Long id, @RequestParam Long userId) {
        eventService.toggleParticipation(id, userId, true);
    }

    @Operation(summary = "Sair de evento")
    @PostMapping("/events/{id}/leave")
    public void leaveEvent(@PathVariable Long id, @RequestParam Long userId) {
        eventService.toggleParticipation(id, userId, false);
    }

    @Operation(summary = "Atualizar evento")
    @PutMapping("/events/{id}")
    public Event updateEvent(@PathVariable Long id, @RequestBody Event event) {
        return eventService.updateEvent(id, event);
    }

    @Operation(summary = "Deletar evento")
    @DeleteMapping("/events/{id}")
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }

    // ------------- NOTIFICATIONS -------------

    @Operation(summary = "Notificações do usuário")
    @GetMapping("/notifications/user/{userId}")
    public List<Notification> getUserNotifications(@PathVariable Long userId) {
        return notificationService.getNotifications(userId);
    }

    @Operation(summary = "Marcar notificações como lidas")
    @PutMapping("/notifications/user/{userId}/read")
    public void markNotificationsAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
    }

    @Operation(summary = "Contagem de notificações não lidas")
    @GetMapping("/notifications/user/{userId}/count")
    public long getUnreadNotificationCount(@PathVariable Long userId) {
        return notificationService.getUnreadCount(userId);
    }

    // ---------------- PROJECTS ----------------

    @Operation(summary = "Listar projetos do usuário")
    @GetMapping("/users/{userId}/projects")
    public List<Project> getUserProjects(@PathVariable Long userId) {
        return projectService.getProjectsByUser(userId);
    }

    @Operation(summary = "Criar projeto (multipart)")
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

    @Operation(summary = "Deletar projeto")
    @DeleteMapping("/projects/{id}")
    public void deleteProject(@PathVariable Long id) {
        projectService.delete(id);
    }

    @Operation(summary = "Atualizar projeto (multipart)")
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
