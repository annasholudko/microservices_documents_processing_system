package com.example.microservices_project.entity;

public class Document {
    private String id;
    private String name;
    private String status; // NEW, PROCESSING, DONE
    private String content; // base64 або локальний шлях

    public Document(String id, String name, String status, String content) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.content = content;
    }

    public Document() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    @Override
    public String toString() {
        return "Document{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
