package com.ifconnected.controller;

import com.ifconnected.model.JPA.Project;
import com.ifconnected.repository.jpa.ProjectRepository;
import com.ifconnected.service.MinioService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final MinioService minioService; // <-- Injetar o MinioService

    public ProjectController(ProjectRepository projectRepository, MinioService minioService) {
        this.projectRepository = projectRepository;
        this.minioService = minioService;
    }

    @PostMapping(value = "/projects", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Project createProject(
            @RequestParam("userId") Long userId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "githubUrl", required = false) String githubUrl,
            @RequestParam(value = "demoUrl", required = false) String demoUrl,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        Project project = new Project();
        project.setUserId(userId);
        project.setTitle(title);
        project.setDescription(description);
        project.setGithubUrl(githubUrl);
        project.setDemoUrl(demoUrl);

        if (file != null && !file.isEmpty()) {
            String imageUrl = minioService.uploadImage(file);
            project.setImageUrl(imageUrl);
        }

        return projectRepository.save(project);
    }

    @PutMapping(value = "/projects/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Project updateProject(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "githubUrl", required = false) String githubUrl,
            @RequestParam(value = "demoUrl", required = false) String demoUrl,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado!"));

        existingProject.setTitle(title);
        existingProject.setDescription(description);
        existingProject.setGithubUrl(githubUrl);
        existingProject.setDemoUrl(demoUrl);

        if (file != null && !file.isEmpty()) {
            String newImageUrl = minioService.uploadImage(file);
            existingProject.setImageUrl(newImageUrl);
        }

        return projectRepository.save(existingProject);
    }

    @DeleteMapping("/projects/{id}")
    public void deleteProject(@PathVariable Long id) {
        projectRepository.deleteById(id);
    }

    @GetMapping("/users/{userId}/projects")
    public List<Project> getUserProjects(@PathVariable Long userId) {
        return projectRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}