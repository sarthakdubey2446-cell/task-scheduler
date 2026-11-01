
package com.sarthak.taskscheduler.service;

import com.sarthak.taskscheduler.entity.Task;
import com.sarthak.taskscheduler.entity.TaskStatus;
import com.sarthak.taskscheduler.entity.User;
import com.sarthak.taskscheduler.repository.TaskRepository;
import com.sarthak.taskscheduler.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User user;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("password")
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "test@example.com",
                        "password"
                )
        );

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void cancelTask_shouldCancelTaskSuccessfully() {

        Task task = Task.builder()
                .id(1L)
                .title("Test Task")
                .status(TaskStatus.PENDING)
                .user(user)
                .build();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        Task result = taskService.cancelTask(1L);

        assertEquals(TaskStatus.CANCELLED, result.getStatus());

        verify(taskRepository).save(task);

        verify(messagingTemplate).convertAndSend(
                "/topic/task-status",
                "Task 1 cancelled"
        );
    }

    @Test
    void cancelTask_shouldThrowExceptionWhenTaskNotFound() {

        when(taskRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> taskService.cancelTask(99L)
        );

        verify(taskRepository, never()).save(any());

        verify(messagingTemplate, never())
                .convertAndSend(anyString(), anyString());
    }
}
