
package com.sarthak.taskscheduler.config;

import com.sarthak.taskscheduler.entity.Task;
import com.sarthak.taskscheduler.entity.TaskStatus;
import com.sarthak.taskscheduler.repository.TaskRepository;
import com.sarthak.taskscheduler.service.EmailService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitMQConsumerTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private RabbitMQProducer rabbitMQProducer;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private RabbitMQConsumer rabbitMQConsumer;


    // TEST 1: Email sent successfully

    @Test
    void receiveMessage_shouldCompleteTaskWhenEmailSucceeds() {

        Task task = Task.builder()
                .id(1L)
                .recipientEmail("test@gmail.com")
                .emailSubject("Test Email")
                .emailBody("Hello from TaskFlow")
                .retryCount(0)
                .status(TaskStatus.PENDING)
                .build();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        rabbitMQConsumer.receiveMessage("1");

        verify(emailService).sendEmail(
                "test@gmail.com",
                "Test Email",
                "Hello from TaskFlow"
        );

        assertEquals(TaskStatus.COMPLETED, task.getStatus());

        verify(taskRepository).save(task);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/task-status"),
                contains("completed successfully")
        );

        verifyNoInteractions(rabbitMQProducer);
    }


    // TEST 2: Email fails and retry is triggered

    @Test
    void receiveMessage_shouldRetryWhenEmailFails() {

        Task task = Task.builder()
                .id(2L)
                .recipientEmail("wrong@gmail.com")
                .emailSubject("Retry Test")
                .emailBody("Testing retry mechanism")
                .retryCount(0)
                .status(TaskStatus.PENDING)
                .build();

        when(taskRepository.findById(2L))
                .thenReturn(Optional.of(task));

        doThrow(new RuntimeException("Email sending failed"))
                .when(emailService)
                .sendEmail(
                        "wrong@gmail.com",
                        "Retry Test",
                        "Testing retry mechanism"
                );

        rabbitMQConsumer.receiveMessage("2");

        assertEquals(1, task.getRetryCount());

        verify(taskRepository).save(task);

        verify(rabbitMQProducer).sendMessage("2");

        verify(messagingTemplate).convertAndSend(
                eq("/topic/task-status"),
                contains("Retry attempt 1")
        );
    }


    // TEST 3: All retries exhausted and task moves to DLQ

    @Test
    void receiveMessage_shouldFailTaskAfterRetriesAreExhausted() {

        Task task = Task.builder()
                .id(3L)
                .recipientEmail("wrong@gmail.com")
                .emailSubject("DLQ Test")
                .emailBody("Testing dead letter queue")
                .retryCount(3)
                .status(TaskStatus.PENDING)
                .build();

        when(taskRepository.findById(3L))
                .thenReturn(Optional.of(task));

        doThrow(new RuntimeException("Email sending failed"))
                .when(emailService)
                .sendEmail(
                        "wrong@gmail.com",
                        "DLQ Test",
                        "Testing dead letter queue"
                );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> rabbitMQConsumer.receiveMessage("3")
        );

        assertEquals("Moving to DLQ", exception.getMessage());

        assertEquals(TaskStatus.FAILED, task.getStatus());

        verify(taskRepository).save(task);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/task-status"),
                contains("failed after 3 retries")
        );

        verifyNoInteractions(rabbitMQProducer);
    }
}
