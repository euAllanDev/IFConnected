package com.ifconnected.model.DTO;

public record UpdateUserDTO(
        String username,
        String bio,
        Long campusId
) {}