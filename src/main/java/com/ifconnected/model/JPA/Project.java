package com.ifconnected.model.JPA;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String githubUrl;
    private String demoUrl; // Link para o site rodando ou vídeo
    private String imageUrl; // Banner do projeto

    private Long userId; // Dono do projeto

    private LocalDateTime createdAt;

    // Lista simples de tecnologias: ["Java", "React", "Docker"]
    @ElementCollection
    @CollectionTable(name = "project_technologies", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "tech_name")
    private List<String> technologies;

    public Project() {
        this.createdAt = LocalDateTime.now();
    }
}