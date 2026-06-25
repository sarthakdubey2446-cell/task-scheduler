package com.sarthak.taskscheduler.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record CreateTaskRequest(

        String title,

        String description,

        LocalDateTime scheduledTime,

        String recipientEmail,

        String emailSubject,

        String emailBody

) {
}