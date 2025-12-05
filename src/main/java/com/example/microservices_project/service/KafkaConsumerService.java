package com.example.microservices_project.service;

import com.example.microservices_project.enums.StatusEnum;
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
    public void listenDocUploading(String id) {
        try {
            logger.info("Processing document: " + id);
            Thread.sleep(1000);
            String res  = documentService.updateStatusById(id, StatusEnum.DONE.toString());
            if ("OK".equals(res)){
                logger.info("Status was updated");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
