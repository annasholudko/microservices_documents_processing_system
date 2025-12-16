package com.example.microservices_project.controller;

import com.example.microservices_project.dto.OutboxDLQDto;
import com.example.microservices_project.service.OutboxProcessorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dlq")
public class OutboxEventController {
    OutboxProcessorService outboxProcessorService;

    public OutboxEventController(OutboxProcessorService outboxProcessorService) {
        this.outboxProcessorService = outboxProcessorService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDlq(@PathVariable("id") Integer id) {
        outboxProcessorService.deleteDlqById(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @GetMapping
    public List<OutboxDLQDto> getAllDLQ(){
        return outboxProcessorService.getAllDLQ();
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<Void> retryEvent(@PathVariable("id") Integer id){
        outboxProcessorService.retryEvent(id);
        return ResponseEntity.ok().build();
    }
}
