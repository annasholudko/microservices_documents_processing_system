package com.document_processor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DocumentDto {
    @JsonProperty("id")
    private Long id; // якщо в Entity ти змінив на AUTO_INCREMENT

    @JsonProperty("name")
    @NotNull
    private String name;

    @JsonProperty("status")
    private String status; // NEW, PROCESSING, DONE

    @JsonProperty("content")
    private String content; // base64 або локальний шлях

    @JsonProperty("author")
    private String author;

    @JsonProperty("create_at")
    private Date createAt;

    public DocumentDto() {
    }

    public DocumentDto(Long id, String name, String status, String content, String author, Date createAt) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.content = content;
        this.author = author;
        this.createAt = createAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }
}
