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
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class OutboxProcessorService {
    private OutboxEventRepository outboxEventRepository;
    private KafkaProducerService kafkaProducerService;
    private final Integer MAX_ATTEMPTS = 2;//5
    @Qualifier("jacksonObjectMapper")
    private ObjectMapper objectMapper;
    private OutboxDLQRepository outboxDLQRepository;

    public OutboxProcessorService(OutboxEventRepository outboxEventRepository, KafkaProducerService kafkaProducerService, OutboxDLQRepository outboxDLQRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.outboxDLQRepository = outboxDLQRepository;
        this.objectMapper = objectMapper;
    }


    @Scheduled(fixedDelay = 5000)
    public void publishOutboxEvents() {
        System.out.println("publishOutboxEvents");
        List<OutboxEvent> events =
                outboxEventRepository.findTop10ByStatusOrderByCreatedAt(OutboxStatusEnum.NEW);

        for (OutboxEvent event : events) {
            scheduleRetry(event);
      /*      try {
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
            System.out.println("saved");*/
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void handleFailedEvents() {
        System.out.println("publishOutboxEvents");
        List<OutboxEvent> events =
                outboxEventRepository.findTop10ByStatusOrderByCreatedAt(OutboxStatusEnum.FAILED);

        for (OutboxEvent event : events) {
            if (event.getAttempts() < MAX_ATTEMPTS && new Timestamp(System.currentTimeMillis()).after(event.getNextAttemptAt())) {//nextAttemptAt <= now
                // ще можна робити retry → залишаємо статус FAILED і ставимо nextAttemptAt
                scheduleRetry(event);
            } else {
                // спроби вичерпані → переносимо в DLQ
                moveToDLQ(event);
            }
        }
    }

    private void scheduleRetry(OutboxEvent event){
        System.out.println("scheduleRetry");
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
        System.out.println("moveToDLQ");
        try {
           /* ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);*/

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
            System.out.println("saved ");
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
