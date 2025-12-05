package com.example.microservices_project.controller;


import com.example.microservices_project.entity.Document;
import com.example.microservices_project.service.KafkaProducerService;
import com.example.microservices_project.service.DocumentService;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DocumentController {
    private DocumentService documentService;
    private final KafkaProducerService producerService;

    public DocumentController(DocumentService documentService, KafkaProducerService producerService) {
        this.documentService = documentService;
        this.producerService = producerService;
    }

    @GetMapping("/greet")
    public JSONObject greet(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", "Hello from microservice");
        return jsonObject;
    }

    @GetMapping("/documents/{id}")
    public Document getDocumentById(@PathVariable("id") String id){
        return documentService.getDocumentById(id);
    }

    @GetMapping("/documents/status/{id}")
    public String getDocumentStatusById(@PathVariable("id") String id){
        return documentService.getDocumentStatusById(id);
    }

    @GetMapping("/send")
    public String send(@RequestParam(name = "msg") String msg) {
        producerService.sendMessage("test-topic", msg);
        return "Message sent: " + msg;
    }

    @PostMapping("/documents/upload")
    public Document addDocument(@RequestBody Document document){
        Document docNew = documentService.addDocument(document);
        producerService.sendMessage("documents-topic", docNew.getId());
        return docNew;
    }
}
