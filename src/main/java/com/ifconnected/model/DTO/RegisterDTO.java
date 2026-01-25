package com.ifconnected.model.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterDTO(
        @Schema(example = "Paulo")
        @NotBlank
        String username,

        @Schema(example = "paulo@email.com")
        @Email @NotBlank
        String email,

        @Schema(example = "123456", minLength = 6)
        @NotBlank
        String password,

        @Schema(example = "1")
        @NotNull
        Long campusId
) {}