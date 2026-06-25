# Task Scheduler System

A backend system built with Spring Boot that allows users to schedule tasks (emails/notifications) for future execution with retry handling, queue-based processing, and real-time event updates.

## Features

* User Authentication with JWT
* Task Scheduling for future execution
* Email sending at scheduled time
* RabbitMQ for asynchronous task processing
* Retry mechanism for failed tasks
* Dead Letter Queue (DLQ) handling
* Rate Limiting using Bucket4j
* Real-time task status notifications using WebSocket
* Swagger API Documentation
* Spring Boot Actuator monitoring
* Global Exception Handling
* Pagination, Sorting, and Filtering
* Task Cancellation
* MySQL Database integration
* JPA Auditing (CreatedAt, UpdatedAt)

---

## Tech Stack

### Backend

* Java 21
* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate

### Database

* MySQL

### Messaging Queue

* RabbitMQ

### Authentication

* JWT Authentication

### Monitoring

* Spring Boot Actuator

### API Documentation

* Swagger / OpenAPI

### Real-Time Communication

* WebSocket

### Rate Limiting

* Bucket4j

---

## Project Flow

### Authentication Flow

1. User registers
2. User logs in
3. JWT token is generated
4. Token is used for protected APIs

---

### Task Scheduling Flow

1. User creates a task
2. Task is stored in database with `PENDING` status
3. Scheduler checks pending tasks
4. When time matches:

   * Task is sent to RabbitMQ queue
5. Consumer receives task
6. Email is sent
7. Task status updates to `COMPLETED`

---

### Failure Flow

If task execution fails:

1. Retry count increases
2. Task is requeued
3. Maximum 3 retries
4. If still fails:

   * Task marked as `FAILED`
   * Moved to DLQ

---

### Real-Time Notification Flow

When task status changes:

* WebSocket event is triggered
* Frontend can subscribe to:

`/topic/task-status`

Events:

* Task Completed
* Task Failed
* Task Retried
* Task Cancelled

---

## API Endpoints

### Auth APIs

* POST `/auth/register`
* POST `/auth/login`

### Task APIs

* POST `/tasks/create`
* GET `/tasks`
* GET `/tasks/{id}`
* PUT `/tasks/{id}`
* DELETE `/tasks/{id}`
* PATCH `/tasks/{id}/cancel`

---

## Rate Limiting

Login API protected using Bucket4j:

* Max 5 requests per minute
* Exceeding limit returns:

429 Too Many Requests

---

## Monitoring

Actuator endpoints:

* `/actuator/health`
* `/actuator/metrics`

---

## Swagger Documentation

Run project and open:

http://localhost:8080/swagger-ui/index.html

---

## Setup Instructions

### Clone repository

```bash
git clone https://github.com/sarthakdubey2446-cell/task-scheduler.git
```

### Configure application

Rename:

application-example.properties → application.properties

Add:

* Database credentials
* Mail credentials
* RabbitMQ configuration

### Run project

```bash
mvn spring-boot:run
```

---

## Future Improvements

* WhatsApp message scheduling
* Push notifications
* Frontend dashboard
* Redis integration
* Kafka integration
* Multi-channel notification support

---

## Author

Sarthak Dubey
