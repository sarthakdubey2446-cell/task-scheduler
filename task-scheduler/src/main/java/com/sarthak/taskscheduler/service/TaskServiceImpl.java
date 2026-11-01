package com.sarthak.taskscheduler.service;

import com.sarthak.taskscheduler.dto.CreateTaskRequest;
import com.sarthak.taskscheduler.dto.TaskPageResponse;
import com.sarthak.taskscheduler.dto.TaskResponse;
import com.sarthak.taskscheduler.dto.UpdateTaskRequest;
import com.sarthak.taskscheduler.entity.Task;
import com.sarthak.taskscheduler.entity.TaskStatus;
import com.sarthak.taskscheduler.entity.User;
import com.sarthak.taskscheduler.exception.TaskNotFoundException;
import com.sarthak.taskscheduler.repository.TaskRepository;
import com.sarthak.taskscheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void createTask(CreateTaskRequest request) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .scheduledTime(request.scheduledTime())
                .recipientEmail(request.recipientEmail())
                .emailSubject(request.emailSubject())
                .emailBody(request.emailBody())
                .status(TaskStatus.PENDING)
                .user(user)
                .build();

        taskRepository.save(task);
    }

    @Override
    public List<TaskResponse> getMyTasks(TaskStatus status) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        List<Task> tasks;

        if (status != null) {
            tasks = taskRepository.findByUserAndStatus(user, status);
        } else {
            tasks = taskRepository.findByUser(user);
        }

        return tasks.stream()
                .map(task -> TaskResponse.builder()
                        .id(task.getId())
                        .title(task.getTitle())
                        .description(task.getDescription())
                        .status(task.getStatus())
                        .scheduledTime(task.getScheduledTime())
                        .build())
                .toList();
    }

    @Override
    public TaskResponse getTaskById(Long id) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        Task task = taskRepository.findById(id)
                .orElseThrow(
                        () -> new TaskNotFoundException("Task not found")
                );

        if (!task.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to access this task"
            );
        }

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .scheduledTime(task.getScheduledTime())
                .status(task.getStatus())
                .build();
    }

    @Override
    public void updateTask(Long taskId, UpdateTaskRequest request) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        Task task = taskRepository.findById(taskId)
                .orElseThrow();

        if (!task.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot update this task"
            );
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setScheduledTime(request.getScheduledTime());

        taskRepository.save(task);
    }

    @Override
    public void deleteTask(Long taskId) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        Task task = taskRepository.findById(taskId)
                .orElseThrow();

        if (!task.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot delete this task"
            );
        }

        taskRepository.delete(task);
    }

    @Override
    public TaskPageResponse getMyTasks(
            int page,
            int size,
            String sortBy
    ) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortBy)
        );

        Page<Task> taskPage = taskRepository.findByUser(
                user,
                pageable
        );

        List<TaskResponse> tasks = taskPage.getContent()
                .stream()
                .map(task -> TaskResponse.builder()
                        .id(task.getId())
                        .title(task.getTitle())
                        .description(task.getDescription())
                        .status(task.getStatus())
                        .scheduledTime(task.getScheduledTime())
                        .build())
                .toList();

        return TaskPageResponse.builder()
                .tasks(tasks)
                .currentPage(taskPage.getNumber())
                .totalPages(taskPage.getTotalPages())
                .totalElements(taskPage.getTotalElements())
                .build();
    }

    @Override
    public Task cancelTask(Long taskId) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        Task task = taskRepository.findById(taskId)
                .orElseThrow();

        if (!task.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot cancel this task"
            );
        }

        task.setStatus(TaskStatus.CANCELLED);
        taskRepository.save(task);

        messagingTemplate.convertAndSend(
                "/topic/task-status",
                "Task " + task.getId() + " cancelled"
        );

        return task;
    }
}
