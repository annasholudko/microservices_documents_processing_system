package com.document_processor.service;

import com.document_processor.dto.DocumentDto;
import com.document_processor.dto.DocumentMapper;
import com.document_processor.entity.Document;
import com.document_processor.repository.DocumentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class DocumentService {
    private RedisTemplate<String, DocumentDto> redisTemplate;
    private DocumentRepository documentRepository;
    private DocumentMapper documentMapper;
    private ObjectMapper objectMapper;

    public DocumentService(RedisTemplate<String, DocumentDto> redisTemplate, DocumentRepository documentRepository, DocumentMapper documentMapper, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.documentRepository = documentRepository;
        this.documentMapper = documentMapper;
        this.objectMapper = objectMapper;
    }


    public DocumentDto getDocumentById(Long id) {
        String key = "document:" + id;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (DocumentDto) cached;
        }

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        DocumentDto documentDto = documentMapper.toDto(document);
        if (documentDto == null) {
            return documentDto;
        }

        redisTemplate.opsForValue().set(key, documentDto, 10, TimeUnit.MINUTES);
        return documentMapper.toDto(document);
    }


    public String updateStatusById(Long id, String statusNew){
        DocumentDto document = getDocumentById(id);
        Document doc = documentMapper.toEntity(document);
        doc.setStatus(statusNew);
        documentRepository.save(doc);
        return "OK";
    }
}
