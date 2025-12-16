package com.example.microservices_project;

import com.example.microservices_project.entity.OutboxEvent;
import com.example.microservices_project.enums.OutboxStatusEnum;
import com.example.microservices_project.repository.OutboxEventRepository;
import com.example.microservices_project.service.OutboxProcessorService;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedKafka(
        topics = "documents-topic",
        partitions = 1
)
@SpringBootTest
class OutboxProcessorIntegrationTest {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OutboxProcessorService outboxProcessorService;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Test
    void processor_shouldSendEventToKafka() {
        // given
        OutboxEvent event  = OutboxEvent.builder()
                .id("1")
                .aggregateId(1L)
                .eventType("DOCUMENT_CREATED")
                .payload("{ \"documentId\": 1 }")
                .status(OutboxStatusEnum.NEW)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .aggregateType("DOCUMENT")
                .attempts(0)
                .lastAttemptAt(null)
                .build();
        outboxEventRepository.save(event);

        // when
        outboxProcessorService.publishOutboxEvents();

        // then
        var updatedEvent = outboxEventRepository.findById(event.getId()).orElseThrow();
        assertThat(updatedEvent.getStatus()).isEqualTo(OutboxStatusEnum.SENT);

        // verify Kafka
        try (var consumer = consumerFactory.createConsumer()) {
            consumer.subscribe(List.of("documents-topic"));
            var records = KafkaTestUtils.getRecords(consumer);
            assertThat(records.count()).isEqualTo(1);
        } // consumer закривається автоматично
    }
}

