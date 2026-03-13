package com.ifconnected.model.DTO;

import java.time.LocalDateTime;

public record MyApplicationDTO(
        Long applicationId,
        Long jobId,
        String jobTitle,
        String companyName,
        String status,
        LocalDateTime appliedAt
) {}