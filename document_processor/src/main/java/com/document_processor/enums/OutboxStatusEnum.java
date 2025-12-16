package com.document_processor.enums;

public enum OutboxStatusEnum {
    NEW,
    PROCESSING,
    SENT,
    DLQ,
    FAILED
}
