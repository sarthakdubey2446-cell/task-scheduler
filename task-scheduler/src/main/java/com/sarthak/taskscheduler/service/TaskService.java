package com.sarthak.taskscheduler.service;

import com.sarthak.taskscheduler.dto.CreateTaskRequest;
import com.sarthak.taskscheduler.dto.TaskPageResponse;
import com.sarthak.taskscheduler.dto.TaskResponse;
import com.sarthak.taskscheduler.dto.UpdateTaskRequest;
import com.sarthak.taskscheduler.entity.Task;
import com.sarthak.taskscheduler.entity.TaskStatus;

import java.util.List;

public interface TaskService {


    void createTask(CreateTaskRequest request);
    List<TaskResponse> getMyTasks(
            TaskStatus status
    );
    TaskResponse getTaskById(Long id);
    void updateTask(Long taskId,
                    UpdateTaskRequest request);
    void deleteTask(Long taskId);
    TaskPageResponse getMyTasks(
            int page,
            int size,
            String sortBy
    );
    Task cancelTask(Long taskId);
}