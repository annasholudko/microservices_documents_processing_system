package com.example.microservices_project;

import com.example.microservices_project.entity.Document;
import com.example.microservices_project.enums.StatusEnum;
import com.example.microservices_project.repository.DocumentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.awaitility.Awaitility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import org.junit.jupiter.api.Test;

import javax.print.Doc;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

@EmbeddedKafka(
        topics = "documents-topic",
        partitions = 1
)
@SpringBootTest
class KafkaConsumerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private DocumentRepository documentRepository;

    @Test
    void consumer_shouldUpdateDocumentStatus() throws Exception {
        // given
        Document document = new Document();
        document.setName("Test document");
        document.setContent("Some content");
        document.setStatus(StatusEnum.NEW.toString());
        document = documentRepository.save(document);
        final Document doc = document;
        ObjectMapper objectMapper = new ObjectMapper();
        String eventPayload = objectMapper.writeValueAsString(document);

        // when
        kafkaTemplate.send("documents-topic", eventPayload);

        // then (ждемо async consumer)
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Document updated =
                            documentRepository.findById(doc.getId()).orElseThrow();

                    assertThat(updated.getStatus())
                            .isEqualTo(StatusEnum.DONE.name());
                });
    }
}

