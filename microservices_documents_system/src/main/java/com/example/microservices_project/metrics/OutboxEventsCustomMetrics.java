package com.example.microservices_project.metrics;

import com.example.microservices_project.enums.OutboxStatusEnum;
import com.example.microservices_project.repository.OutboxDLQRepository;
import com.example.microservices_project.repository.OutboxEventRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;


@Component
public class OutboxEventsCustomMetrics {

    private final MeterRegistry meterRegistry;

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxDLQRepository outboxDLQRepository;

    public OutboxEventsCustomMetrics(MeterRegistry meterRegistry, OutboxEventRepository outboxEventRepository, OutboxDLQRepository outboxDLQRepository) {
        this.meterRegistry = meterRegistry;
        this.outboxEventRepository = outboxEventRepository;
        this.outboxDLQRepository = outboxDLQRepository;
    }


    @PostConstruct
    public void registerMetrics() {
        //http://localhost:8080/actuator/metrics/outbox.events.processing.count
        Gauge.builder("outbox.events.processing.count",
                        outboxEventRepository,   // об'єкт, на якому викликаємо метод
                        repo -> repo.countByStatus(OutboxStatusEnum.PROCESSING)) // lambda
                .description("Number of processing outbox events")
                .register(meterRegistry);

        Gauge.builder("example.events.failed.count",
                        outboxEventRepository,   // об'єкт, на якому викликаємо метод
                        repo -> repo.countByStatus(OutboxStatusEnum.FAILED))
                .description("Number of failed example events")
                .register(meterRegistry);

        //http://localhost:8080/actuator/metrics/example.events.dlq.count
        Gauge.builder("example.events.dlq.count",
                        outboxDLQRepository,   // об'єкт, на якому викликаємо метод
                        repo -> repo.count())
                .description("Number of failed example events")
                .register(meterRegistry);
    }

}

