package com.example.microservices_project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_dlq")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutboxDLQ {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String originalEventId;
    @Lob
    private String payload;
    private String reason;
    private Timestamp failedAt;
}
