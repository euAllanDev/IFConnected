package com.ifconnected.controller;

import com.ifconnected.config.OpenApiConfig;
import com.ifconnected.model.DTO.CampusDTO;
import com.ifconnected.model.DTO.UpdateUserDTO;
import com.ifconnected.model.DTO.UserProfileDTO;
import com.ifconnected.model.DTO.UserResponseDTO;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.model.JPA.Event;
import com.ifconnected.model.JPA.Opportunity;
import com.ifconnected.model.JPA.Project;
import com.ifconnected.model.NOSQL.Notification;
import com.ifconnected.model.NOSQL.Post;
import com.ifconnected.repository.mongo.PostRepository;
import com.ifconnected.security.UserLoginInfo;
import com.ifconnected.service.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.http.HttpStatus;
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
    private final OpportunityService opportunityService;

    // CONSTRUTOR COMPLETO (Injeção de Dependência)
    public IfConnectedController(
            UserService userService,
            PostRepository postRepository,
            MinioService minioService,
            GeoFeedService geoFeedService,
            EventService eventService,
            NotificationService notificationService,
            CampusService campusService,
            ProjectService projectService,
            OpportunityService opportunityService // <--- Adicionado
    ) {
        this.userService = userService;
        this.postRepository = postRepository;
        this.minioService = minioService;
        this.geoFeedService = geoFeedService;
        this.eventService = eventService;
        this.notificationService = notificationService;
        this.campusService = campusService;
        this.projectService = projectService;
        this.opportunityService = opportunityService; // <--- Inicializado
    }

    // =========================
    // USERS
    // =========================

    @Operation(summary = "Listar usuários (dados públicos)", tags = {"Users"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @GetMapping("/users")
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "Buscar usuário por ID (dados públicos)", tags = {"Users"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/users/{id}")
    public UserResponseDTO getUser(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long id
    ) {
        return userService.getUserById(id);
    }

    @Operation(summary = "Perfil do usuário (dados públicos + contadores)", tags = {"Users"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/users/{id}/profile")
    public UserProfileDTO getUserProfile(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long id
    ) {
        return userService.getUserProfile(id);
    }

    @Operation(summary = "Atualizar usuário (username/bio/campusId)", tags = {"Users"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PutMapping("/users/{id}")
    public UserResponseDTO updateUser(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long id,
            @RequestBody UpdateUserDTO dto
    ) {
        return userService.updateUser(id, dto);
    }

    @Operation(summary = "Atualizar campus do usuário", tags = {"Users"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Campus atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/users/{id}/campus")
    public void updateUserCampus(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long id,
            @Parameter(description = "ID do campus", example = "3")
            @RequestBody Long campusId
    ) {
        userService.updateCampus(id, campusId);
    }

    @Operation(
            summary = "Upload de foto de perfil (multipart)",
            description = "Envia uma imagem e atualiza o profileImageUrl do usuário.",
            tags = {"Users"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Foto atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PostMapping(value = "/users/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponseDTO uploadProfilePhoto(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Arquivo de imagem (jpg/png/etc).", required = true)
            @RequestParam("file") MultipartFile file
    ) {
        String imageUrl = minioService.uploadImage(file);
        return userService.updateProfileImage(id, imageUrl);
    }

    @Operation(summary = "Retorna o usuário logado (dados públicos)", tags = {"Users"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário logado retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @GetMapping("/me")
    public UserResponseDTO me(@AuthenticationPrincipal UserLoginInfo principal) {
        return userService.getUserById(principal.getId());
    }

    // =========================
    // CAMPUS
    // =========================

    @Operation(summary = "Listar campi", tags = {"Campus"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @GetMapping("/campus")
    public List<CampusDTO> getAllCampuses() {
        return campusService.getAll();
    }

    // =========================
    // FOLLOW
    // =========================

    @Operation(summary = "Verifica se followerId segue followedId", tags = {"Users"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @GetMapping("/users/{followerId}/isFollowing/{followedId}")
    public boolean isFollowing(
            @Parameter(description = "ID do usuário seguidor", example = "1")
            @PathVariable Long followerId,
            @Parameter(description = "ID do usuário seguido", example = "2")
            @PathVariable Long followedId
    ) {
        return userService.isFollowing(followerId, followedId);
    }

    @Operation(summary = "Seguir usuário", tags = {"Users"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Agora está seguindo"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/users/{followerId}/follow/{followedId}")
    public void followUser(
            @Parameter(description = "ID do usuário seguidor", example = "1")
            @PathVariable Long followerId,
            @Parameter(description = "ID do usuário seguido", example = "2")
            @PathVariable Long followedId
    ) {
        userService.follow(followerId, followedId);

        // notificação: usa a entidade para pegar username real do perfil
        User follower = userService.getUserEntityById(followerId);
        notificationService.createNotification(
                followedId,
                followerId,
                follower.getUsername(),
                "FOLLOW",
                null
        );
    }

    @Operation(summary = "Deixar de seguir usuário", tags = {"Users"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deixou de seguir"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/users/{followerId}/follow/{followedId}")
    public void unfollowUser(
            @Parameter(description = "ID do usuário seguidor", example = "1")
            @PathVariable Long followerId,
            @Parameter(description = "ID do usuário seguido", example = "2")
            @PathVariable Long followedId
    ) {
        userService.unfollow(followerId, followedId);
    }

    // =========================
    // POSTS
    // =========================

    @Operation(summary = "Buscar post por ID", tags = {"Posts"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Post não encontrado")
    })
    @GetMapping("/posts/{id}")
    public Post getPostById(
            @Parameter(description = "ID do post (MongoDB)", example = "65b3f2...")
            @PathVariable String id
    ) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));
    }

    @Operation(
            summary = "Criar post (multipart)",
            description = "Cria um post com texto e opcionalmente uma imagem.",
            tags = {"Posts"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Post createPost(
            @Parameter(description = "ID do usuário autor", example = "1")
            @RequestParam("userId") Long userId,
            @Parameter(description = "Conteúdo do post", example = "Olá, IF!")
            @RequestParam("content") String content,
            @Parameter(description = "Imagem do post (opcional)")
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
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

    @Operation(summary = "Listar todos os posts", tags = {"Posts"})
    @GetMapping("/posts")
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @Operation(summary = "Feed de quem eu sigo", tags = {"Posts"})
    @GetMapping("/posts/feed/{userId}")
    public List<Post> getFriendsFeed(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long userId
    ) {
        List<Long> followingIds = userService.getFollowingIds(userId);
        followingIds.add(userId);
        return postRepository.findByUserIdIn(followingIds);
    }

    @Operation(summary = "Posts do usuário", tags = {"Posts"})
    @GetMapping("/posts/user/{userId}")
    public List<Post> getPostsByUser(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long userId
    ) {
        return postRepository.findByUserId(userId);
    }

    @Operation(summary = "Comentar em um post", tags = {"Posts"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comentário adicionado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Post não encontrado")
    })
    @PostMapping("/posts/{postId}/comments")
    public Post addComment(
            @Parameter(description = "ID do post (MongoDB)", example = "65b3f2...")
            @PathVariable String postId,
            @RequestBody Post.Comment comment
    ) {
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

    @Operation(summary = "Curtir/descurtir post", tags = {"Posts"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Like alternado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Post não encontrado")
    })
    @PostMapping("/posts/{postId}/like")
    public Post toggleLike(
            @Parameter(description = "ID do post (MongoDB)", example = "65b3f2...")
            @PathVariable String postId,
            @Parameter(description = "ID do usuário que curtiu", example = "1")
            @RequestParam Long userId
    ) {
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

    // =========================
    // GEO FEED / SUGGESTIONS
    // =========================

    @Operation(summary = "Feed regional (por raio)", tags = {"Posts"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feed regional retornado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Usuário sem campus vinculado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @GetMapping("/posts/feed/regional")
    public List<Post> getRegionalFeed(
            @Parameter(description = "ID do usuário", example = "1")
            @RequestParam Long userId,
            @Parameter(description = "Raio em KM", example = "50")
            @RequestParam(defaultValue = "50") double radiusKm
    ) {
        User user = userService.getUserEntityById(userId);
        if (user.getCampusId() == null) {
            throw new RuntimeException("Usuário não tem campus vinculado! Selecione seu IF no perfil.");
        }
        return geoFeedService.getNearbyCampusFeed(user.getCampusId(), radiusKm);
    }

    @Operation(summary = "Sugestões de pessoas (por raio)", tags = {"Users"})
    @GetMapping("/users/{id}/suggestions")
    public List<UserResponseDTO> getSuggestions(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Raio em KM", example = "50")
            @RequestParam(defaultValue = "50") double radiusKm
    ) {
        User me = userService.getUserEntityById(id);
        if (me.getCampusId() == null) return List.of();

        List<User> suggestions = geoFeedService.getPeopleYouMightKnow(id, me.getCampusId(), radiusKm);
        return suggestions.stream().map(this::toUserResponseDTO).toList();
    }

    private UserResponseDTO toUserResponseDTO(User user) {
        return new UserResponseDTO(user);
    }

    // =========================
    // EVENTS
    // =========================

    @Operation(summary = "Criar evento", tags = {"Events"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/events")
    public Event createEvent(@RequestBody Event event) {
        return eventService.createEvent(event);
    }

    @Operation(summary = "Listar eventos por campus", tags = {"Events"})
    @GetMapping("/events/campus/{campusId}")
    public List<Event> listEventsByCampus(
            @Parameter(description = "ID do campus", example = "3")
            @PathVariable Long campusId
    ) {
        return eventService.getEventsByCampus(campusId);
    }

    @Operation(summary = "Participar de evento", tags = {"Events"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/events/{id}/join")
    public void joinEvent(
            @Parameter(description = "ID do evento", example = "10")
            @PathVariable Long id,
            @Parameter(description = "ID do usuário", example = "1")
            @RequestParam Long userId
    ) {
        eventService.toggleParticipation(id, userId, true);
    }

    @Operation(summary = "Sair de evento", tags = {"Events"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/events/{id}/leave")
    public void leaveEvent(
            @Parameter(description = "ID do evento", example = "10")
            @PathVariable Long id,
            @Parameter(description = "ID do usuário", example = "1")
            @RequestParam Long userId
    ) {
        eventService.toggleParticipation(id, userId, false);
    }

    @Operation(summary = "Atualizar evento", tags = {"Events"})
    @PutMapping("/events/{id}")
    public Event updateEvent(
            @Parameter(description = "ID do evento", example = "10")
            @PathVariable Long id,
            @RequestBody Event event
    ) {
        return eventService.updateEvent(id, event);
    }

    @Operation(summary = "Deletar evento", tags = {"Events"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/events/{id}")
    public void deleteEvent(
            @Parameter(description = "ID do evento", example = "10")
            @PathVariable Long id
    ) {
        eventService.deleteEvent(id);
    }

    // =========================
    // NOTIFICATIONS
    // =========================

    @Operation(summary = "Notificações do usuário", tags = {"Notifications"})
    @GetMapping("/notifications/user/{userId}")
    public List<Notification> getUserNotifications(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long userId
    ) {
        return notificationService.getNotifications(userId);
    }

    @Operation(summary = "Marcar notificações como lidas", tags = {"Notifications"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/notifications/user/{userId}/read")
    public void markNotificationsAsRead(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long userId
    ) {
        notificationService.markAllAsRead(userId);
    }

    @Operation(summary = "Contagem de notificações não lidas", tags = {"Notifications"})
    @GetMapping("/notifications/user/{userId}/count")
    public long getUnreadNotificationCount(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long userId
    ) {
        return notificationService.getUnreadCount(userId);
    }

    // =========================
    // PROJECTS
    // =========================

    @Operation(summary = "Listar projetos do usuário", tags = {"Projects"})
    @GetMapping("/users/{userId}/projects")
    public List<Project> getUserProjects(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long userId
    ) {
        return projectService.getProjectsByUser(userId);
    }

    @Operation(
            summary = "Criar projeto (multipart)",
            description = "Cria projeto com tecnologias e imagem opcional.",
            tags = {"Projects"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Projeto criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/projects", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Project createProject(
            @Parameter(description = "Título do projeto", example = "Meu Portfólio")
            @RequestParam("title") String title,
            @Parameter(description = "Descrição do projeto", example = "Site pessoal com Next.js")
            @RequestParam("description") String description,
            @Parameter(description = "ID do usuário dono", example = "1")
            @RequestParam("userId") Long userId,
            @Parameter(description = "URL do GitHub", example = "https://github.com/user/repo")
            @RequestParam(value = "githubUrl", required = false) String githubUrl,
            @Parameter(description = "URL de demonstração", example = "https://meusite.com")
            @RequestParam(value = "demoUrl", required = false) String demoUrl,
            @Parameter(description = "Tecnologias (envie múltiplos campos technologies=...)", example = "[\"Java\",\"Spring\"]")
            @RequestParam(value = "technologies", required = false) List<String> technologies,
            @Parameter(description = "Imagem do projeto (opcional)")
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

        return projectService.createProject(project);
    }

    @Operation(summary = "Deletar projeto", tags = {"Projects"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Projeto deletado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/projects/{id}")
    public void deleteProject(
            @Parameter(description = "ID do projeto", example = "10")
            @PathVariable Long id
    ) {
        projectService.delete(id);
    }

    @Operation(
            summary = "Atualizar projeto (multipart)",
            description = "Atualiza título/descrição/links/tech e imagem opcional.",
            tags = {"Projects"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projeto atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    @PutMapping(value = "/projects/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Project updateProject(
            @Parameter(description = "ID do projeto", example = "10")
            @PathVariable Long id,
            @Parameter(description = "Título do projeto", example = "Meu Portfólio")
            @RequestParam("title") String title,
            @Parameter(description = "Descrição do projeto", example = "Atualização da descrição")
            @RequestParam("description") String description,
            @Parameter(description = "URL do GitHub", example = "https://github.com/user/repo")
            @RequestParam(value = "githubUrl", required = false) String githubUrl,
            @Parameter(description = "URL de demonstração", example = "https://meusite.com")
            @RequestParam(value = "demoUrl", required = false) String demoUrl,
            @Parameter(description = "Tecnologias (envie múltiplos campos technologies=...)", example = "[\"Java\",\"Spring\"]")
            @RequestParam(value = "technologies", required = false) List<String> technologies,
            @Parameter(description = "Imagem do projeto (opcional)")
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

    // --- OPORTUNIDADES (JPA DAO) ---

    @Operation(summary = "Listar oportunidades", tags = {"Opportunities"})
    @GetMapping("/opportunities")
    public List<Opportunity> listOpportunities() {
        // CORREÇÃO: Chama o método getAll() do opportunityService
        return opportunityService.getAll();
    }

    @Operation(summary = "Criar oportunidade (Admin)", tags = {"Opportunities"})
    @PostMapping("/opportunities")
    public void createOpportunity(
            @RequestBody Opportunity opportunity,
            @Parameter(description = "ID do usuário (precisa ser admin)", example = "1")
            @RequestParam Long userId
    ) {
        opportunityService.createOpportunity(opportunity, userId);
    }

    @Operation(summary = "Deletar oportunidade (Admin)", tags = {"Opportunities"})
    @DeleteMapping("/opportunities/{id}")
    public void deleteOpportunity(
            @Parameter(description = "ID da oportunidade", example = "5")
            @PathVariable Long id,
            @Parameter(description = "ID do usuário (precisa ser admin)", example = "1")
            @RequestParam Long userId
    ) {
        opportunityService.deleteOpportunity(id, userId);
    }

    @GetMapping("/events")
    public List<Event> getAllEvents() {

        return eventService.getEventsByCampus(1L);
    }

    @PostMapping("/projects")
    public Project createProject(@RequestBody Project project) {
        return projectService.createProject(project);
    }

}