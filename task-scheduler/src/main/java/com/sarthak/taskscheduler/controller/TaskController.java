package com.sarthak.taskscheduler.controller;

import com.sarthak.taskscheduler.dto.CreateTaskRequest;
import com.sarthak.taskscheduler.dto.TaskPageResponse;
import com.sarthak.taskscheduler.dto.TaskResponse;
import com.sarthak.taskscheduler.dto.UpdateTaskRequest;
import com.sarthak.taskscheduler.entity.Task;
import com.sarthak.taskscheduler.entity.TaskStatus;
import com.sarthak.taskscheduler.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<String> createTask(
            @Valid @RequestBody CreateTaskRequest request
    ) {

        taskService.createTask(request);

        return ResponseEntity.ok(
                "Task Created Successfully"
        );
    }
    @GetMapping
    public ResponseEntity<List<TaskResponse>>
    getMyTasks(

            @RequestParam(
                    required = false
            )
            TaskStatus status

    ) {

        return ResponseEntity.ok(

                taskService.getMyTasks(
                        status
                )

        );
    }
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                taskService.getTaskById(id)
        );
    }
    @PutMapping("/{taskId}")
    public ResponseEntity<String> updateTask(
            @PathVariable Long taskId,
            @RequestBody UpdateTaskRequest request
    ) {

        taskService.updateTask(
                taskId,
                request
        );

        return ResponseEntity.ok(
                "Task Updated Successfully"
        );
    }
    @DeleteMapping("/{taskId}")
    public ResponseEntity<String> deleteTask(
            @PathVariable Long taskId
    ) {

        taskService.deleteTask(taskId);

        return ResponseEntity.ok(
                "Task Deleted Successfully"
        );
    }
    @GetMapping("/page")
    public ResponseEntity<TaskPageResponse> getMyTasksPaginated(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "5")
            int size,

            @RequestParam(defaultValue = "scheduledTime")
            String sortBy
    ) {

        return ResponseEntity.ok(
                taskService.getMyTasks(
                        page,
                        size,
                        sortBy
                )
        );
    }
    @PatchMapping("/{taskId}/cancel")
    public ResponseEntity<Task> cancelTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.cancelTask(taskId));
    }
}