package com.ifconnected.controller;

import com.ifconnected.model.JPA.Project;
import com.ifconnected.service.ProjectService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping(value = "/projects", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Project createProject(
            @RequestParam("userId") Long userId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "githubUrl", required = false) String githubUrl,
            @RequestParam(value = "demoUrl", required = false) String demoUrl,
            @RequestParam(value = "technologies", required = false) List<String> technologies,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        return projectService.createProject(userId, title, description, githubUrl, demoUrl, technologies, file);
    }

    @PutMapping(value = "/projects/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Project updateProject(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "githubUrl", required = false) String githubUrl,
            @RequestParam(value = "demoUrl", required = false) String demoUrl,
            @RequestParam(value = "technologies", required = false) List<String> technologies,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        return projectService.updateProject(id, title, description, githubUrl, demoUrl, technologies, file);
    }

    @DeleteMapping("/projects/{id}")
    public void deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
    }

    @GetMapping("/users/{userId}/projects")
    public List<Project> getUserProjects(@PathVariable Long userId) {
        return projectService.getByUser(userId);
    }
}