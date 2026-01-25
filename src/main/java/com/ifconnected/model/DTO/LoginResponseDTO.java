package com.ifconnected.model.DTO;

public record LoginResponseDTO(String token, Long userId, String username) {
}