package com.ifconnected.model.JPA;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements; // Adicionei requisitos separados

    private String location;
    private String type;

    private boolean active = true;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    private LocalDateTime createdAt = LocalDateTime.now();
}