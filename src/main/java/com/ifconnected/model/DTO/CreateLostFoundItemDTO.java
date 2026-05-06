package com.ifconnected.model.DTO;

public record CreateLostFoundItemDTO(
        String title,
        String description,
        String status,
        String imageUrl
) {}