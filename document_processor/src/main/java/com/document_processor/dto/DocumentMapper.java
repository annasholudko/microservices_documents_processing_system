package com.document_processor.dto;

import com.document_processor.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {
    public Document toEntity(DocumentDto dto) {
        Document entity = new Document();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setStatus(dto.getStatus());
        entity.setContent(dto.getContent());
        entity.setAuthor(dto.getAuthor());
        entity.setCreateAt(dto.getCreateAt());
        return entity;
    }

    public DocumentDto toDto(Document entity) {
        DocumentDto dto = new DocumentDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setStatus(entity.getStatus());
        dto.setContent(entity.getContent());
        dto.setAuthor(entity.getAuthor());
        dto.setCreateAt(entity.getCreateAt());
        return dto;
    }
}
