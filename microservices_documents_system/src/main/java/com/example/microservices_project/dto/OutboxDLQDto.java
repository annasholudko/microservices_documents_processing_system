package com.example.microservices_project.dto;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

public record OutboxDLQDto(
        Integer id,
        String originalEventId,
        String payload,
        String reason,
        Timestamp failedAt
) {
}