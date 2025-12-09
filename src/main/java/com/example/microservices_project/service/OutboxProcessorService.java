package com.example.microservices_project.service;

import com.example.microservices_project.entity.OutboxEvent;
import com.example.microservices_project.enums.OutboxStatusEnum;
import com.example.microservices_project.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxProcessorService {
    private OutboxEventRepository outboxEventRepository;
    private KafkaProducerService kafkaProducerService;

    public OutboxProcessorService(OutboxEventRepository outboxEventRepository, KafkaProducerService kafkaProducerService) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishOutboxEvents() {
        System.out.println("publishOutboxEvents");
        List<OutboxEvent> events =
                outboxEventRepository.findTop10ByStatusOrderByCreatedAt(OutboxStatusEnum.NEW);

        for (OutboxEvent event : events) {
            try {
                event.setStatus(OutboxStatusEnum.PROCESSING);
                outboxEventRepository.save(event);
                kafkaProducerService.sendMessage("documents-topic", event.getPayload());
                event.setStatus(OutboxStatusEnum.SENT);
            } catch (Exception ex) {
                event.setStatus(OutboxStatusEnum.FAILED);
                event.setLastAttemptAt(LocalDateTime.now());
                event.setAttempts(event.getAttempts() + 1);
            }
            outboxEventRepository.save(event);
            System.out.println("saved");
        }
    }
}
