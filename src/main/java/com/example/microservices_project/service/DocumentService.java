package com.example.microservices_project.service;

import com.example.microservices_project.dto.DocumentDto;
import com.example.microservices_project.dto.DocumentMapper;
import com.example.microservices_project.entity.Document;
import com.example.microservices_project.entity.OutboxEvent;
import com.example.microservices_project.enums.OutboxStatusEnum;
import com.example.microservices_project.repository.DocumentRepository;
import com.example.microservices_project.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DocumentService {
    private RedisTemplate<String, Object> redisTemplate;
    private DocumentRepository documentRepository;
    private DocumentMapper documentMapper;
    private OutboxEventRepository outboxEventRepository;
    private ObjectMapper objectMapper;
    private KafkaProducerService kafkaProducerService;

    public DocumentService(RedisTemplate<String, Object> redisTemplate, DocumentRepository documentRepository, DocumentMapper documentMapper, OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper, KafkaProducerService kafkaProducerService) {
        this.redisTemplate = redisTemplate;
        this.documentRepository = documentRepository;
        this.documentMapper = documentMapper;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.kafkaProducerService = kafkaProducerService;
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

    public String getDocumentStatusById(Long id){
        DocumentDto document = getDocumentById(id);
        return document == null ? null : document.getStatus();
    }

    @Transactional
    public DocumentDto addDocument(DocumentDto documentDto) {
        documentDto.setCreateAt(new Timestamp(System.currentTimeMillis()));
        Document saved = documentRepository.save(documentMapper.toEntity(documentDto));
        OutboxEvent event = null;
        String uuid = UUID.randomUUID().toString();
        try {
            event = OutboxEvent.builder()
                    .id(uuid)
                    .aggregateId(saved.getId())
                    .eventType("DOCUMENT_CREATED")
                    .payload(objectMapper.writeValueAsString(saved))
                    .status(OutboxStatusEnum.NEW)
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .aggregateType("DOCUMENT")
                    .attempts(0)
                    .lastAttemptAt(null)
                    .build();
            outboxEventRepository.save(event);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return documentMapper.toDto(saved);
    }


    public String updateStatusById(Long id, String statusNew){
        DocumentDto document = getDocumentById(id);
        Document doc = documentMapper.toEntity(document);
        doc.setStatus(statusNew);
        documentRepository.save(doc);
        return "OK";
    }

    public List<DocumentDto> getAllDocuments() {
        String key = "documentsList:";
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (List<DocumentDto>) cached;
        }

        List<DocumentDto> documentList = documentRepository.findAll().stream()
                .map(documentMapper::toDto)
                .collect(Collectors.toList());
        if (documentList == null || documentList.isEmpty()) {
            return documentList;
        }

        redisTemplate.opsForValue().set(key, documentList, 10, TimeUnit.MINUTES);
        return documentList;
    }
}
