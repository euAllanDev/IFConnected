package com.ifconnected.service;

import com.ifconnected.exception.ResourceNotFoundException;
import com.ifconnected.model.JPA.Project;
import com.ifconnected.repository.jpa.ProjectRepository;
import com.ifconnected.service.MinioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MinioService minioService;

    public ProjectService(ProjectRepository projectRepository, MinioService minioService) {
        this.projectRepository = projectRepository;
        this.minioService = minioService;
    }

    @Transactional
    public Project createProject(Long userId, String title, String description,
                                 String githubUrl, String demoUrl,
                                 List<String> technologies, MultipartFile file) {
        Project project = new Project();
        project.setUserId(userId);
        project.setTitle(title);
        project.setDescription(description);
        project.setGithubUrl(githubUrl);
        project.setDemoUrl(demoUrl);

        if (technologies != null) {
            project.setTechnologies(technologies);
        }

        if (file != null && !file.isEmpty()) {
            project.setImageUrl(minioService.uploadImage(file));
        }

        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProject(Long id, String title, String description,
                                 String githubUrl, String demoUrl,
                                 List<String> technologies, MultipartFile file) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));

        project.setTitle(title);
        project.setDescription(description);
        project.setGithubUrl(githubUrl);
        project.setDemoUrl(demoUrl);

        if (technologies != null) {
            project.setTechnologies(technologies);
        }

        if (file != null && !file.isEmpty()) {
            project.setImageUrl(minioService.uploadImage(file));
        }

        return projectRepository.save(project);
    }

    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Projeto não encontrado");
        }
        projectRepository.deleteById(id);
    }

    public List<Project> getByUser(Long userId) {
        return projectRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}