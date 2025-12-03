package com.ifconnected.controller;

import com.ifconnected.model.JDBC.User;
import com.ifconnected.model.NOSQL.Post;
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

    public IfConnectedController(UserService userService, PostRepository postRepository, MinioService minioService) {
        this.userService = userService;
        this.postRepository = postRepository;
        this.minioService = minioService;
    }

    // --- USUÁRIOS ---

    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // ATUALIZAÇÃO: Upload de Foto de Perfil e Bio
    @PutMapping(value = "/users/{id}/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void updateProfile(@PathVariable Long id,
                              @RequestParam(value = "bio", required = false) String bio,
                              @RequestParam(value = "file", required = false) MultipartFile file) {

        String imageUrl = null;
        // Se enviou arquivo, faz upload no MinIO
        if (file != null && !file.isEmpty()) {
            imageUrl = minioService.uploadImage(file);
        }

        // Se não enviou foto nova, busca o user antigo para manter a foto (lógica simplificada)
        // Aqui vamos apenas atualizar o que veio.
        User currentUser = userService.getUserById(id);
        String finalImage = (imageUrl != null) ? imageUrl : currentUser.getProfileImageUrl();
        String finalBio = (bio != null) ? bio : currentUser.getBio();

        userService.updateProfile(id, finalBio, finalImage);
    }

    @PostMapping("/users/{followerId}/follow/{followedId}")
    public void followUser(@PathVariable Long followerId, @PathVariable Long followedId) {
        userService.follow(followerId, followedId);
    }

    // --- POSTS ---

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Post createPost(@RequestParam("userId") Long userId,
                           @RequestParam("content") String content,
                           @RequestParam("file") MultipartFile file) {
        String imageUrl = minioService.uploadImage(file);
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(content);
        post.setImageUrl(imageUrl);
        return postRepository.save(post);
    }

    // NOVO: Curtir Post
    @PostMapping("/posts/{postId}/like")
    public Post toggleLike(@PathVariable String postId, @RequestParam Long userId) {
        Post post = postRepository.findById(postId).orElseThrow();

        // Se já curtiu, remove. Se não, adiciona.
        if (post.getLikes().contains(userId)) {
            post.getLikes().remove(userId);
        } else {
            post.getLikes().add(userId);
        }

        return postRepository.save(post);
    }

    // NOVO: Feed (Apenas de quem eu sigo)
    @GetMapping("/feed/{myUserId}")
    public List<Post> getFeed(@PathVariable Long myUserId) {
        // 1. Pega a lista de IDs que eu sigo no Postgres
        List<Long> followingIds = userService.getFollowingIds(myUserId);

        // 2. Adiciona o meu próprio ID (pra eu ver meus posts também)
        followingIds.add(myUserId);

        // 3. Busca no Mongo os posts desses IDs
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
}
