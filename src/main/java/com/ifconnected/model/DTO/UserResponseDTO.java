package com.ifconnected.model.DTO;

import com.ifconnected.model.JDBC.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserResponseDTO", description = "Dados públicos do usuário (sem informações de segurança).")
public record UserResponseDTO(
        @Schema(example = "3") Long id,
        @Schema(example = "Paulo") String username,
        @Schema(example = "paulo@email.com") String email,
        @Schema(example = "Minha bio...") String bio,
        @Schema(example = "https://.../img.png") String profileImageUrl,
        @Schema(example = "1") Long campusId,
        @Schema(example = "STUDENT") String role
) {
    // --- CONSTRUTOR ADICIONAL ---
    // Permite fazer: new UserResponseDTO(userEntity)
    // Isso resolve o erro "Expected X arguments but found 1"
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