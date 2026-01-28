package com.ifconnected.model.DTO;

public record CreateLostFoundItemDTO(
        String title,
        String description,
        String status,     // "LOST" ou "FOUND"
        String imageUrl    // opcional (ou você faz upload MinIO depois)
) {}