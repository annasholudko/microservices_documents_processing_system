package com.document_processor.dto;

import java.sql.Timestamp;

public record OutboxDLQDto(
        Integer id,
        String originalEventId,
        String payload,
        String reason,
        Timestamp failedAt
) {
}