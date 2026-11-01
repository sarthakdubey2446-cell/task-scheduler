
package com.sarthak.taskscheduler.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateTaskRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title cannot exceed 100 characters")
        String title,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        @NotNull(message = "Scheduled time is required")
        @Future(message = "Scheduled time must be in the future")
        LocalDateTime scheduledTime,

        @NotBlank(message = "Recipient email is required")
        @Email(message = "Please provide a valid email address")
        String recipientEmail,

        @NotBlank(message = "Email subject is required")
        String emailSubject,

        @NotBlank(message = "Email body is required")
        String emailBody

) {
}
