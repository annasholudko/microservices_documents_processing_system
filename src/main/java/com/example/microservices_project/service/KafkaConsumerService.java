package com.example.microservices_project.service;

import com.example.microservices_project.entity.Document;
import com.example.microservices_project.enums.StatusEnum;
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

    @Autowired
    public KafkaConsumerService(DocumentService documentService) {
        this.documentService = documentService;
    }

    @KafkaListener(topics = "test-topic", groupId = "test-group")
    public void listen(String message) {
        System.out.println("Received: " + message);
    }

    @KafkaListener(topics = "documents-topic", groupId = "documents-group")
    public void listenDocUploading(String message) {
        logger.info("Document event received: "+ message);
        System.err.println("received ");
/*        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Document doc = objectMapper.readValue(message, Document.class);
            logger.info("Processing document: " + doc.getId());
            Thread.sleep(1000);
            String res  = documentService.updateStatusById(Long.valueOf(doc.getId()), StatusEnum.DONE.toString());
            if ("OK".equals(res)){
                logger.info("Status was updated");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }*/
    }
}
