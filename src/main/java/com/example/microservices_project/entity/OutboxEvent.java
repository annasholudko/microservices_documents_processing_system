package com.example.microservices_project.entity;

import com.example.microservices_project.enums.OutboxStatusEnum;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
@Data
@Builder
public class OutboxEvent {

    @Id
    @Column(length = 36)
    private String id;

    private Long aggregateId;

    private String eventType;

    @Lob
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatusEnum status;
    private Timestamp createdAt;
    private String aggregateType;
    private Integer attempts;
    private Timestamp lastAttemptAt;
    private Timestamp nextAttemptAt;


    public OutboxEvent() {
    }

    public OutboxEvent(String id, Long aggregateId, String eventType, String payload, OutboxStatusEnum status, Timestamp createdAt, String aggregateType, Integer attempts, Timestamp lastAttemptAt, Timestamp nextAttemptAt) {
        this.id = id;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = status;
        this.createdAt = createdAt;
        this.aggregateType = aggregateType;
        this.attempts = attempts;
        this.lastAttemptAt = lastAttemptAt;
        this.nextAttemptAt = nextAttemptAt;
    }

/*    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Long aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public OutboxStatusEnum getStatus() {
        return status;
    }

    public void setStatus(OutboxStatusEnum status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }*/

    @Override
    public String toString() {
        return "OutboxEvent{" +
                "id='" + id + '\'' +
                ", aggregateId=" + aggregateId +
                ", eventType='" + eventType + '\'' +
                ", payload='" + payload + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", aggregateType='" + aggregateType + '\'' +
                ", attempts=" + attempts +
                ", lastAttemptAt=" + lastAttemptAt +
                '}';
    }

    /*public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }*/

   /* public LocalDateTime getLastAttemptAt() {
        return lastAttemptAt;
    }

    public void setLastAttemptAt(LocalDateTime lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }*/
}

