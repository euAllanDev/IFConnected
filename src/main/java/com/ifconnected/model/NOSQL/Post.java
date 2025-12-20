package com.ifconnected.model.NOSQL;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "posts")
public class Post {
    @Id
    private String id;
    private Long userId;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt = LocalDateTime.now();

    // Inicializa as listas para evitar NullPointerException
    private List<Comment> comments = new ArrayList<>();
    private List<Long> likes = new ArrayList<>();

    // --- Geolocalização (Suporte ao Feed Regional) ---
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location;
    private String locationName;

    // --- Construtor Padrão ---
    public Post() {
    }

    // --- Getters e Setters Manuais (Post) ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Long> getLikes() {
        if (likes == null) likes = new ArrayList<>();
        return likes;
    }

    public void setLikes(List<Long> likes) {
        this.likes = likes;
    }

    public List<Comment> getComments() {
        if (comments == null) comments = new ArrayList<>();
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public GeoJsonPoint getLocation() {
        return location;
    }

    public void setLocation(GeoJsonPoint location) {
        this.location = location;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    // --- Classe Interna Comment (Manual) ---
    public static class Comment {
        // Gera um ID único para cada comentário automaticamente
        private String commentId = UUID.randomUUID().toString();
        private Long userId;
        private String text;
        private LocalDateTime postedAt = LocalDateTime.now();

        // Construtor vazio
        public Comment() {
        }

        // Construtor utilitário
        public Comment(Long userId, String text) {
            this.userId = userId;
            this.text = text;
            this.commentId = UUID.randomUUID().toString();
            this.postedAt = LocalDateTime.now();
        }

        // Getters e Setters Manuais (Comment)
        public String getCommentId() {
            return commentId;
        }

        public void setCommentId(String commentId) {
            this.commentId = commentId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public LocalDateTime getPostedAt() {
            return postedAt;
        }

        public void setPostedAt(LocalDateTime postedAt) {
            this.postedAt = postedAt;
        }
    }
}