package com.ifconnected.model.JPA;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String githubUrl;
    private String demoUrl;
    private String imageUrl;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private LocalDateTime createdAt = LocalDateTime.now();

}