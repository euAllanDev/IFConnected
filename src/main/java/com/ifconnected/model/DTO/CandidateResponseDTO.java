package com.ifconnected.model.DTO;

import java.time.LocalDateTime;

public record CandidateResponseDTO(
        Long applicationId,
        Long candidateId,
        String candidateName,
        String candidateEmail,
        String candidatePhoto, // Opcional, para deixar o card mais bonito
        String coverLetter,
        String status,
        LocalDateTime appliedAt
) {}