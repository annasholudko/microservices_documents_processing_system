package com.example.microservices_project.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

@Entity(name = "documents")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("status")
    private String status; // NEW, PROCESSING, DONE
    @JsonProperty("content")
    private String content; // base64 або локальний шлях
    @JsonProperty("author")
    private String author;
    @JsonProperty("create_at")
    private Timestamp createAt;
}
