package com.sarthak.taskscheduler.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateTaskRequest {

    private String title;
    private String description;
    private LocalDateTime scheduledTime;
}