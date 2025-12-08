package com.example.microservices_project.repository;

import com.example.microservices_project.entity.OutboxEvent;
import com.example.microservices_project.enums.OutboxStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findTop10ByStatusOrderByCreatedAt(OutboxStatusEnum status);
}
