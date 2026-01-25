package com.ifconnected.model.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="LoginResponse")
public record LoginResponseDTO(

        @Schema(description="JWT Bearer token")
        String token,

        @Schema(example="1")
        Long userId,

        @Schema(example="paulo")
        String username
) {}