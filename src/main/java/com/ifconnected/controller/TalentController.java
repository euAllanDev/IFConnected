package com.ifconnected.controller;

import com.ifconnected.model.DTO.UserResponseDTO;
import com.ifconnected.model.JPA.Project;
import com.ifconnected.repository.jpa.ProjectRepository;
import com.ifconnected.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/talents")
public class TalentController {

    private final ProjectRepository projectRepository;
    private final UserService userService;

    public TalentController(ProjectRepository projectRepository, UserService userService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    /**
     * BUSCA DE TALENTOS: Retorna alunos que tenham projetos com a tecnologia informada.
     * Exemplo de uso: GET /api/talents/search?tech=Spring Boot
     */
    @GetMapping("/search")
    public List<UserResponseDTO> searchTalentsByTechnology(@RequestParam String tech) {

        List<Project> matchingProjects = projectRepository.findByTechnologiesContainingIgnoreCase(tech);

        Set<Long> userIds = matchingProjects.stream()
                .map(Project::getUserId)
                .collect(Collectors.toSet());

        return userIds.stream()
                .map(userService::getUserById) // Usa o método que já retorna DTO sem senha!
                .collect(Collectors.toList());
    }
}