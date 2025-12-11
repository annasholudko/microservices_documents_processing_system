package com.example.microservices_project.service;

import com.example.microservices_project.dto.OutboxDLQDto;
import com.example.microservices_project.entity.OutboxDLQ;
import com.example.microservices_project.entity.OutboxEvent;
import com.example.microservices_project.enums.OutboxStatusEnum;
import com.example.microservices_project.repository.OutboxDLQRepository;
import com.example.microservices_project.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class OutboxProcessorService {
    private OutboxEventRepository outboxEventRepository;
    private KafkaProducerService kafkaProducerService;
    private final Integer MAX_ATTEMPTS = 2;//5
    private final Integer BATCH_SIZE = 2;//5

    @Qualifier("outboxExecutor")
    private ThreadPoolTaskExecutor executor;
    @Qualifier("jacksonObjectMapper")
    private ObjectMapper objectMapper;
    private OutboxDLQRepository outboxDLQRepository;

    public OutboxProcessorService(OutboxEventRepository outboxEventRepository, KafkaProducerService kafkaProducerService, ThreadPoolTaskExecutor executor, ObjectMapper objectMapper, OutboxDLQRepository outboxDLQRepository) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.executor = executor;
        this.objectMapper = objectMapper;
        this.outboxDLQRepository = outboxDLQRepository;
    }


    @Scheduled(fixedDelay = 5000)
    public void publishOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatus(OutboxStatusEnum.NEW);

        // розбиваємо на батчі
        for (int i = 0; i < pendingEvents.size(); i += BATCH_SIZE) {
            List<OutboxEvent> batch = pendingEvents.subList(i, Math.min(i + BATCH_SIZE, pendingEvents.size()));
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    processBatch(batch);
                }
            });
        }
        System.err.println("end publish");
    }

    private void processBatch(List<OutboxEvent> batch) {
        for (OutboxEvent event : batch) {
            scheduleRetry(event);
        }
    }



    @Scheduled(fixedDelay = 5000)
    public void handleFailedEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatus(OutboxStatusEnum.FAILED);

        // розбиваємо на батчі
        for (int i = 0; i < pendingEvents.size(); i += BATCH_SIZE) {
            List<OutboxEvent> batch = pendingEvents.subList(i, Math.min(i + BATCH_SIZE, pendingEvents.size()));
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    for (OutboxEvent event : batch) {
                        if (event.getAttempts() < MAX_ATTEMPTS
                                && (event.getNextAttemptAt() == null || new Timestamp(System.currentTimeMillis()).after(event.getNextAttemptAt()))) {//nextAttemptAt <= now
                            // ще можна робити retry → залишаємо статус FAILED і ставимо nextAttemptAt
                            scheduleRetry(event);
                        } else {
                            // спроби вичерпані → переносимо в DLQ
                            moveToDLQ(event);
                        }
                    }
                }
            });
        }
        System.err.println("end failed");
    }

    private void scheduleRetry(OutboxEvent event){
        try {
            event.setStatus(OutboxStatusEnum.PROCESSING);
            outboxEventRepository.save(event);
            kafkaProducerService.sendMessage("documents-topic", event.getPayload());
            event.setStatus(OutboxStatusEnum.SENT);
        } catch (Exception ex) {
            event.setStatus(OutboxStatusEnum.FAILED);
        }
        event.setLastAttemptAt(new Timestamp(System.currentTimeMillis()));
        event.setNextAttemptAt(new Timestamp(System.currentTimeMillis() + event.getAttempts() * 5_000L));
        event.setAttempts(event.getAttempts() + 1);
        outboxEventRepository.save(event);
        System.out.println("saved "+event.getStatus());
    }

    private void moveToDLQ(OutboxEvent outboxEvent){
        try {

            String payloadJson = objectMapper.writeValueAsString(outboxEvent);
            OutboxDLQ outboxDLQ = OutboxDLQ.builder()
                    .originalEventId(outboxEvent.getId())
                    .payload(payloadJson)
                    .reason("Max attempts reached")
                    .failedAt(new Timestamp(System.currentTimeMillis()))
                    .build();
            outboxDLQRepository.save(outboxDLQ);

            outboxEvent.setStatus(OutboxStatusEnum.DLQ);
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<OutboxDLQDto> getAllDLQ() {
        return outboxDLQRepository.findAll()
                .stream()
                .map(dlq -> new OutboxDLQDto(
                        dlq.getId(),
                        dlq.getOriginalEventId(),
                        dlq.getPayload(),
                        dlq.getReason(),
                        dlq.getFailedAt()
                ))
                .toList();
    }

    @Transactional
    public void retryEvent(Integer id){
        //outbox_events.id = outbox_dlq.original_event_id
        //Integer id = outbox_dlq.id
        OutboxDLQ outboxDLQ = outboxDLQRepository.findById(id).orElseThrow();
        String eventId = outboxDLQ.getOriginalEventId();
        OutboxEvent outboxEvent = outboxEventRepository.findById(eventId) .orElseThrow(() -> new RuntimeException("DLQ event not found: " + id));
        outboxEvent.setStatus(OutboxStatusEnum.NEW);
        outboxEvent.setAttempts(0);
        outboxEvent.setLastAttemptAt((new Timestamp(System.currentTimeMillis())));
        outboxEvent.setNextAttemptAt(new Timestamp(System.currentTimeMillis() +  5_000L));

        outboxEventRepository.save(outboxEvent);
        outboxDLQRepository.delete(outboxDLQ);
    }
    @Transactional
    public void deleteDlqById(Integer id) {
        if (!outboxDLQRepository.existsById(id)) {
            throw new RuntimeException("DLQ event not found: " + id);
        }
        outboxDLQRepository.deleteById(id);
    }
}
