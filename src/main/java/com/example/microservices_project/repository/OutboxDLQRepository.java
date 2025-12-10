package com.example.microservices_project.repository;

import com.example.microservices_project.entity.OutboxDLQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxDLQRepository extends JpaRepository<OutboxDLQ, Integer> {
}
