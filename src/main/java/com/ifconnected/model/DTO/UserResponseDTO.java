package com.ifconnected.model.DTO;

import com.ifconnected.model.JDBC.User;
import com.ifconnected.model.enums.Role;

// Usamos 'record' para criar um DTO imutável e conciso
public record UserResponseDTO(
        Long id,
        String username,
        String email,
        String bio,
        String profileImageUrl,
        Long campusId,
        Role role
) {
    // --- CONSTRUTOR PERSONALIZADO ---
    // Permite criar o DTO passando a entidade inteira: new UserResponseDTO(user)
    public UserResponseDTO(User user) {
        this(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getProfileImageUrl(),
                user.getCampusId(),
                user.getRole()
        );
    }
}