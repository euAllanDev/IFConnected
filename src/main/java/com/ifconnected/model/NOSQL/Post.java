package com.ifconnected.model.NOSQL;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Document(collection = "posts")
public class Post {
    @Id
    private String id;
    private Long userId;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt = LocalDateTime.now();
    private List<Comment> comments;

    // --- Construtor Padrão ---
    public Post() {
    }

    private Set<Long> likes = new HashSet<>();

    public Set<Long> getLikes() {
        if(likes == null) likes = new HashSet<>();
        return likes;
    }
    public void setLikes(Set<Long> likes) {
        this.likes = likes;
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

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    // --- Classe Interna Comment (Manual) ---
    public static class Comment {
        private Long userId;
        private String text;
        private LocalDateTime postedAt = LocalDateTime.now();

        // Construtor vazio
        public Comment() {
        }

        // Getters e Setters Manuais (Comment)
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