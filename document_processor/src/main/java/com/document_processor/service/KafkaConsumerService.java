package com.document_processor.service;

import com.document_processor.entity.Document;
import com.document_processor.enums.StatusEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class KafkaConsumerService {
    private DocumentService documentService;
    private static final Logger logger = Logger.getLogger(KafkaConsumerService.class.getName());
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaConsumerService(DocumentService documentService, ObjectMapper objectMapper) {
        this.documentService = documentService;
        this.objectMapper = objectMapper;
    }


    //http://localhost:8080/api/send?msg=hjb
    @KafkaListener(topics = "test-topic", groupId = "test-group")
    public void listen(String message) {
        System.out.println("Received: " + message);
    }

    @KafkaListener(topics = "documents-topic", groupId = "documents-group")
    public void listenDocUploading(String message) {
        logger.info("Document event received: "+ message);
        try {
            Document doc = objectMapper.readValue(message, Document.class);
            logger.info("Processing document: " + doc.getId());
            String res  = documentService.updateStatusById(doc.getId(), StatusEnum.DONE.name());
            if ("OK".equals(res)){
                logger.info("document updated");
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
