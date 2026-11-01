# # Task Scheduler System

A full-stack task scheduling application built with **Java 21, Spring Boot, React, MySQL, and RabbitMQ**.

TaskFlow allows users to schedule tasks for future execution, automate email delivery, and track task execution status through a dashboard.

The backend uses asynchronous message processing, retry handling, and a Dead Letter Queue (DLQ) to manage task execution failures.

## Features

### Authentication and Security
- User registration and login
- JWT-based authentication
- Password encryption using BCrypt
- Protected REST APIs
- Request validation
- Rate limiting using Bucket4j

### Task Management
- Create, update, and delete tasks
- Schedule tasks for future execution
- Cancel scheduled tasks
- Track task execution status
- Pagination, sorting, and filtering

### Asynchronous Processing
- RabbitMQ-based message processing
- Producer-consumer architecture
- Scheduled task execution
- Automatic retry mechanism
- Dead Letter Queue (DLQ) handling
- Maximum of 3 retries

### Email Notifications
- Automated email delivery
- Email scheduling
- Email failure handling

### Dashboard and Monitoring
- React-based frontend dashboard
- Task status visualization
- Task search and filtering
- Automatic status refresh using polling
- Spring Boot Actuator
- Swagger / OpenAPI documentation
- Global exception handling
- JPA auditing (`createdAt`, `updatedAt`)

---

## Technology Stack

| Category | Technologies |
|---|---|
| Language | Java 21 |
| Backend | Spring Boot |
| Security | Spring Security, JWT, BCrypt |
| ORM | Spring Data JPA, Hibernate |
| Database | MySQL |
| Message Broker | RabbitMQ |
| Frontend | React, JavaScript, HTML, CSS |
| Build Tool | Maven |
| API Testing | Postman |
| API Documentation | Swagger / OpenAPI |
| Monitoring | Spring Boot Actuator |
| Rate Limiting | Bucket4j |
| Testing | JUnit, Mockito, Spring Boot Test |

---

## System Architecture

TaskFlow follows a layered backend architecture with asynchronous task processing.

```text
              React Frontend
                    |
                    | REST API
                    v
             Spring Boot Backend
                    |
          +---------+---------+
          |                   |
          v                   v
    Spring Security       Task Service
       (JWT)                   |
                              v
                         MySQL Database
                              |
                              v
                       Task Scheduler
                              |
                              v
                         RabbitMQ
                              |
                              v
                       Message Consumer
                              |
                              v
                         Email Service
                              |
                              v
                       Task Status Update
```

---

## Application Workflows

### 1. Authentication Flow

1. User registers an account.
2. Password is encrypted using BCrypt.
3. User logs in with their credentials.
4. Backend validates the credentials.
5. JWT token is generated.
6. The token is used to access protected APIs.

### 2. Task Scheduling Flow

1. User creates a task with its scheduled execution time.
2. Task is saved in MySQL with `PENDING` status.
3. The scheduler periodically checks pending tasks.
4. Due tasks are published to RabbitMQ.
5. A RabbitMQ consumer receives the message.
6. The email service processes the task.
7. Task status is updated to `COMPLETED` after successful execution.

### 3. Failure and Retry Flow

When task execution fails:

1. The failure is handled by the consumer.
2. Retry count is incremented.
3. The task is published again for retry.
4. The task gets up to 3 retries.
5. After retries are exhausted, the task is marked `FAILED`.
6. The failed message is routed to the Dead Letter Queue (DLQ).

### 4. Task Status Management

The application uses the following task statuses:

- `PENDING`
- `PROCESSING`
- `COMPLETED`
- `FAILED`
- `CANCELLED`

The frontend periodically refreshes task data to display updated statuses.

---

## REST API Endpoints

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Register a user |
| POST | `/auth/login` | Authenticate a user |

### Task Management

| Method | Endpoint | Description |
|---|---|---|
| POST | `/tasks` | Create a task |
| GET | `/tasks` | Retrieve tasks |
| GET | `/tasks/{id}` | Retrieve a task by ID |
| PUT | `/tasks/{taskId}` | Update a task |
| DELETE | `/tasks/{taskId}` | Delete a task |
| PATCH | `/tasks/{taskId}/cancel` | Cancel a task |

Additional pagination endpoints are also available.

---

## Rate Limiting

Bucket4j is used to protect the login API against excessive requests.

- Configured limit: 5 requests per second.
- Requests exceeding the configured limit receive HTTP `429 Too Many Requests`.

---

## Testing

The backend includes unit and integration tests.

Testing covers:

- Authentication and JWT flow
- Task creation and retrieval
- Task updates and deletion
- User authorization and task ownership
- Request validation
- RabbitMQ consumer success and failure handling
- Retry and DLQ scenarios
- Task cancellation

Run the complete test suite:

```bash
mvn clean test
```

**Latest recorded test result:**
- Tests: 11
- Failures: 0
- Errors: 0
- Skipped: 0

---

## API Documentation

After starting the backend, open:

http://localhost:8080/swagger-ui/index.html

---

## Monitoring

Spring Boot Actuator provides application monitoring endpoints.

Available endpoints include:

- `/actuator/health`
- `/actuator/metrics`

---

## Local Setup

### Prerequisites

Install the following:

- Java 21
- Maven
- MySQL
- RabbitMQ
- Node.js and npm

### 1. Clone the Repository

```bash
git clone https://github.com/sarthakdubey2446-cell/task-scheduler.git
cd task-scheduler
```

### 2. Configure the Backend

Configure your database, email, and RabbitMQ credentials in:

```text
src/main/resources/application.properties
```

Do not commit passwords, email app passwords, JWT secrets, or other sensitive credentials to GitHub.

### 3. Start MySQL and RabbitMQ

Make sure both services are running and the configured credentials and connection details are correct.

### 4. Run the Backend

```bash
mvn spring-boot:run
```

Backend default address:

```text
http://localhost:8080
```

### 5. Run the Frontend

Open the frontend project in a separate terminal:

```bash
npm install
npm run dev
```

Open the local address displayed by Vite in your terminal.

---

## Future Improvements

- Redis-based caching
- Support for additional notification channels
- Push notifications
- Improved production monitoring
- Cloud deployment and scalability improvements

---

## Author

**Sarthak Dubey**

GitHub: https://github.com/sarthakdubey2446-cell
