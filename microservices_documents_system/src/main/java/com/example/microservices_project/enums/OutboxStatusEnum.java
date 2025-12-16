package com.example.microservices_project.enums;

public enum OutboxStatusEnum {
    NEW,
    PROCESSING,
    SENT,
    DLQ,
    FAILED
}
