package com.example.microservices_project;

import com.example.microservices_project.dto.DocumentDto;
import com.example.microservices_project.enums.OutboxStatusEnum;
import com.example.microservices_project.enums.StatusEnum;
import com.example.microservices_project.repository.OutboxEventRepository;
import com.example.microservices_project.service.DocumentService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DocumentOutboxIntegrationTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    void createDocument_shouldCreateOutboxRecord() {
        DocumentDto documentDto = new DocumentDto();
        documentDto.setName("Test document");
        documentDto.setContent("Some content");
        documentDto.setStatus(StatusEnum.NEW.toString());
        DocumentDto dtoSaved = documentService.addDocument(documentDto);
        Long documentId = dtoSaved.getId();

        var outboxEvents = outboxEventRepository.findTop10ByStatusOrderByCreatedAt(OutboxStatusEnum.NEW);

        assertThat(outboxEvents).hasSize(1);

        var event = outboxEvents.get(0);
        assertThat(event.getAggregateType()).isEqualTo("DOCUMENT");
        assertThat(event.getAggregateId()).isEqualTo(documentId);
        assertThat(event.getStatus()).isEqualTo(OutboxStatusEnum.NEW);
    }
}

