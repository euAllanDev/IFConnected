package com.ifconnected.model.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name="LoginRequest")
public record LoginDTO(
        @Schema(example="paulo@ifpb.edu.br")
        @NotBlank @Email
        String email,

        @Schema(example="123456")
        @NotBlank
        String password
) {}