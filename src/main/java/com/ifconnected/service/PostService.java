package com.ifconnected.service;

import com.ifconnected.exception.BusinessRuleException;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.model.NOSQL.Post;
import com.ifconnected.repository.mongo.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final MinioService minioService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final GeoFeedService geoFeedService;

    public PostService(PostRepository postRepository, MinioService minioService,
                       UserService userService, NotificationService notificationService,
                       GeoFeedService geoFeedService) {
        this.postRepository = postRepository;
        this.minioService = minioService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.geoFeedService = geoFeedService;
    }

    // --- LEITURA BÁSICA ---
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostById(String id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));
    }

    public List<Post> getPostsByUser(Long userId) {
        return postRepository.findByUserId(userId);
    }

    // --- ESCRITA E LÓGICA PESADA ---

    @Transactional // Trata a subida da imagem e salvamento no Mongo
    public Post createPost(Long userId, String content, MultipartFile file) {
        String imageUrl = null;

        // Regra de Negócio: Fazer upload só se existir arquivo
        if (file != null && !file.isEmpty()) {
            imageUrl = minioService.uploadImage(file);
        }

        Post post = new Post();
        post.setUserId(userId);
        post.setContent(content);
        post.setImageUrl(imageUrl);

        return postRepository.save(post);
    }

    public Post addComment(String postId, Post.Comment comment) {
        Post post = getPostById(postId);

        if (post.getComments() == null) {
            post.setComments(new ArrayList<>());
        }

        comment.setPostedAt(LocalDateTime.now());
        if (comment.getCommentId() == null) {
            comment.setCommentId(UUID.randomUUID().toString());
        }

        post.getComments().add(comment);
        Post savedPost = postRepository.save(post);

        // Regra de Negócio: Notificar o dono do post, se não for ele mesmo comentando
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

    public Post toggleLike(String postId, Long userId) {
        Post post = getPostById(postId);

        List<Long> likes = post.getLikes();
        if (likes == null) {
            likes = new ArrayList<>();
        }

        if (likes.contains(userId)) {
            // Se já curtiu, descurte
            likes.remove(userId);
        } else {
            // Se não curtiu, adiciona like e notifica
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

    // --- FEEDS INTELIGENTES ---

    public List<Post> getFriendsFeed(Long userId) {
        List<Long> followingIds = userService.getFollowingIds(userId);
        followingIds.add(userId); // Inclui o próprio usuário
        return postRepository.findByUserIdIn(followingIds);
    }

    public List<Post> getRegionalFeed(Long userId, double radiusKm) {
        User user = userService.getUserEntityById(userId);
        if (user.getCampusId() == null) {
            throw new BusinessRuleException("Usuário não tem campus vinculado!");
        }
        return geoFeedService.getNearbyCampusFeed(user.getCampusId(), radiusKm);
    }
}