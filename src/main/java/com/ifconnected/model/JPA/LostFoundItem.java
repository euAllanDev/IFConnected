package com.ifconnected.model.JPA;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lost_found_items")
public class LostFoundItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LostFoundStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ✅ ManyToOne tem que apontar pra entidade JPA
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    // (opcional) se você quiser guardar url da imagem no item:
    // @Column(name = "image_url")
    // private String imageUrl;

    // getters/setters
    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LostFoundStatus getStatus() { return status; }
    public void setStatus(LostFoundStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public UserEntity getOwner() { return owner; }
    public void setOwner(UserEntity owner) { this.owner = owner; }

    // public String getImageUrl() { return imageUrl; }
    // public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
