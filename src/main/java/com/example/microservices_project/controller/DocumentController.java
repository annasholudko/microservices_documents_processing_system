package com.example.microservices_project.controller;

import com.example.microservices_project.dto.DocumentDto;
import com.example.microservices_project.service.KafkaProducerService;
import com.example.microservices_project.service.DocumentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DocumentController {
    private DocumentService documentService;
    private final KafkaProducerService producerService;
    private ObjectMapper objectMapper;


    public DocumentController(DocumentService documentService, KafkaProducerService producerService, ObjectMapper objectMapper) {
        this.documentService = documentService;
        this.producerService = producerService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/greet")
    public JSONObject greet(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", "Hello from microservice");
        return jsonObject;
    }

    @GetMapping("/documents")
    public List<DocumentDto> getDocuments(){
        return documentService.getAllDocuments();
    }


    @GetMapping("/documents/{id}")
    public DocumentDto getDocumentById(@PathVariable("id") Long id){
        return documentService.getDocumentById(id);
    }

    @GetMapping("/documents/status/{id}")
    public String getDocumentStatusById(@PathVariable("id") Long id){
        return documentService.getDocumentStatusById(id);
    }

    @GetMapping("/send")
    public String send(@RequestParam(name = "msg") String msg) {
        producerService.sendMessage("test-topic", msg);
        return "Message sent: " + msg;
    }

    @PostMapping("/documents/upload")
    public DocumentDto addDocument(@Valid @RequestBody DocumentDto document){
        DocumentDto docNew = documentService.addDocument(document);
        System.out.println(docNew.getCreateAt());
        try {
            producerService.sendMessage("documents-topic", objectMapper.writeValueAsString(docNew));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return docNew;
    }
}
