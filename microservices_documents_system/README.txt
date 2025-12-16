Document Processing Microservice
Overview

This microservice handles document processing using an event-driven architecture.
It receives requests to create documents, stores events in the Outbox, publishes them to Kafka, manages retry and DLQ (Dead Letter Queue), and exposes custom metrics for observability.

Technologies:

Java 17, Spring Boot, Spring Data JPA

MySQL

Kafka

Micrometer / Actuator

API Endpoints
1. Create Document
POST /documents
Content-Type: application/json


Request Body:

{
  "title": "Test Document",
  "content": "This is a test",
  "author": "Anna"
}


Creates a document and adds an event to Outbox with status NEW.

2. DLQ Management

Retry DLQ event

POST /outbox/dlq/retry/{id}


Delete DLQ event

DELETE /outbox/dlq/{id}

Outbox Event Flow

Event is created with status NEW.

Scheduled Processor fetches a batch of events (BATCH_SIZE = 20) and publishes them to Kafka.

Event status is updated: PROCESSING → SENT or FAILED.

Events exceeding MAX_ATTEMPTS are moved to DLQ.

Retry Logic

Failed events are checked every 5 seconds.

If attempts < MAX_ATTEMPTS and nextAttemptAt <= now, a retry is scheduled.

If max attempts are reached → event is moved to DLQ.

Metrics / Observability

Actuator Endpoints:

/actuator/metrics
/actuator/metrics/outbox.events.processing.count
/actuator/metrics/outbox.events.failed.count
/actuator/metrics/outbox.events.dlq.count


Example Output:

{
  "outbox.events.processing.count": 15,
  "outbox.events.failed.count": 2,
  "outbox.events.dlq.count": 1
}


Custom metrics automatically count events in the database.

Outbox Table Example
id	status	attempts	nextAttemptAt	lastAttemptAt
1	PROCESSING	1	2025-12-11 11:12:00	2025-12-11 11:11:55
2	FAILED	2	2025-12-11 11:11:00	2025-12-11 11:10:55
DLQ Table Example
id	originalEventId	payload	reason	failedAt
1	2	{...json...}	Max attempts reached	2025-12-11 11:12:05
Mini Load Test

50 documents were created via Postman → all events processed successfully.

Metrics show correct counts for pending, failed, and DLQ events.

Events are processed in batches and parallel threads to simulate production load.

Project Structure
src/main/java
config		 - configuration
controller       - REST Controllers
dto		 - dto objects
entity           - JPA Entities
enums            - Status enums
metrics          - Custom Micrometer metrics
repository       - JPA Repositories
service          - Business logic + Outbox processing


Future Improvements

Add AWS S3 to store large documents.

Introduce second microservice (e.g., Document Analytics) to demonstrate event-driven communication between services.

This project demonstrates practical skills for Middle Java Developer, including:

Event-driven architecture with Kafka

Retry and DLQ handling

Parallel batch processing

Observability with custom metrics