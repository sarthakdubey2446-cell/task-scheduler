package com.sarthak.taskscheduler.dto;

import com.sarthak.taskscheduler.entity.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
public class TaskResponse  {

    private Long id;

    private String title;

    private String description;

    private LocalDateTime scheduledTime;

    private TaskStatus status;
}