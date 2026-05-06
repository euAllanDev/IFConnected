package com.ifconnected.service;

import com.ifconnected.model.JPA.Project;
import com.ifconnected.repository.jpa.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // Renomeado de 'save' para 'createProject' para bater com o Controller
    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    // --- ESSE MÉTODO FALTAVA ---
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<Project> getProjectsByUser(Long userId) {
        // Garanta que o Repositório tenha este método declarado
        return projectRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void delete(Long id) {
        projectRepository.deleteById(id);
    }

    public Project update(Long id, Project newData) {
        return projectRepository.findById(id)
                .map(project -> {
                    project.setTitle(newData.getTitle());
                    project.setDescription(newData.getDescription());
                    project.setGithubUrl(newData.getGithubUrl());
                    project.setDemoUrl(newData.getDemoUrl());

                    // Atualiza tecnologias
                    if (newData.getTechnologies() != null) {
                        project.setTechnologies(newData.getTechnologies());
                    }

                    // Só atualiza a imagem se vier uma nova URL (não nula e não vazia)
                    if (newData.getImageUrl() != null && !newData.getImageUrl().isEmpty()) {
                        project.setImageUrl(newData.getImageUrl());
                    }

                    return projectRepository.save(project);
                })
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));
    }
}