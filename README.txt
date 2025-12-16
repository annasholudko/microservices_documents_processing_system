Microservice 1: Document Processing Microservice (Producer)
Overview
This microservice handles document creation and event publishing using an event-driven architecture. It receives requests to create documents, stores events in the Outbox table, publishes them to Kafka, manages retry and DLQ logic, and exposes custom metrics for observability.

Technologies

Java 17
Spring Boot
Spring Data JPA / Hibernate
MySQL
Kafka
Redis
Micrometer / Spring Actuator
Docker

API Endpoints
Create Document

POST /documents

Request body:
{
  "name": "Test Document5",
  "status": "NEW",
  "content": "This is a test",
  "author": "Anna"
}

Creates a document and writes an event to the Outbox table with status NEW.

DLQ Management

Retry DLQ event:
POST /outbox/dlq/retry/{id}
Delete DLQ event:
DELETE /outbox/dlq/{id}
Outbox Event Flow
Event is created with status NEW
Scheduled processor fetches events in batches (BATCH_SIZE = 20)
Events are published to Kafka

Status transitions:
NEW → PROCESSING → SENT
or FAILED → DLQ
Events exceeding MAX_ATTEMPTS are moved to DLQ

Retry Logic
Failed events are checked every 5 seconds
If attempts < MAX_ATTEMPTS and nextAttemptAt <= now, a retry is scheduled
When max attempts are reached, the event is moved to DLQ
Metrics & Observability
Actuator endpoints:

/actuator/metrics
/actuator/metrics/outbox.events.processing.count
/actuator/metrics/outbox.events.failed.count
/actuator/metrics/outbox.events.dlq.count

Example output:

{
  "outbox.events.processing.count": 15,
  "outbox.events.failed.count": 2,
  "outbox.events.dlq.count": 1
}

Custom metrics dynamically count events directly from the database.

Project structure
src/main/java
config        - application and infrastructure configuration
controller    - REST controllers
dto           - request/response DTOs
entity        - JPA entities
enums         - status enums
metrics       - custom Micrometer metrics
repository   - JPA repositories
service       - business logic and outbox processing

Microservice 2: Document Processor Microservice (Consumer)
Overview
This microservice is a Kafka consumer responsible for asynchronous document processing. It listens to document lifecycle events published by the Document Processing Microservice and processes them independently.
The service is intentionally separated to demonstrate loose coupling and event-driven communication between microservices.

Technologies

Java 17
Spring Boot
Spring Kafka
Kafka Consumer API
Retry / Backoff
Dead Letter Queue (DLQ)
Docker (conceptual for local and production setup)

Event Consumption Flow
DocumentCreatedEvent is published to Kafka
Document Processor consumes the event
Event is processed asynchronously
On success → processing completed

On failure:
Retry with backoff is applied
After exceeding max retries → message is sent to DLQ

Kafka Topics
document.events – main topic
document.events.dlq – dead letter topic

Example Consumed Event
{
"eventId": "c3f2a9e1",
"documentId": 42,
"title": "Test Document",
"author": "Anna",
"createdAt": "2025-12-11T11:10:00"
}

Project Structure
src/main/java
config      - Kafka configuration
consumer    - Kafka listeners
dto         - event DTOs
entity      - JPA entities
repository   - JPA repositories
service     - processing logic

Docker and Local Run (Conceptual)
Both microservices and infrastructure components (MySQL, Redis, Kafka) can be containerized.
Build services:
mvn clean package

Start with Docker Compose (conceptual):
docker-compose up --build


Note: Docker setup is shown conceptually for demonstration purposes; local execution may require a modern Docker environment.

This project demonstrates:
Real-world Kafka-based microservices
Transactional Outbox pattern
Retry & DLQ handling on producer and consumer sides
Parallel batch processing
Observability with custom metrics
Production-style Docker setup


Author
Anna Sholudko
Middle Java Developer (4.7 years experience)