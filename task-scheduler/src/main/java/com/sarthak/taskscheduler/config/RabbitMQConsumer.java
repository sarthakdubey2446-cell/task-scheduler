package com.sarthak.taskscheduler.config;

import com.sarthak.taskscheduler.entity.Task;
import com.sarthak.taskscheduler.entity.TaskStatus;
import com.sarthak.taskscheduler.repository.TaskRepository;
import com.sarthak.taskscheduler.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final TaskRepository taskRepository;
    private final EmailService emailService;
    private final RabbitMQProducer rabbitMQProducer;
    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage(String taskId) {

        Task task = taskRepository.findById(Long.parseLong(taskId))
                .orElseThrow();

        try {
            emailService.sendEmail(
                    task.getRecipientEmail(),
                    task.getEmailSubject(),
                    task.getEmailBody()
            );

            task.setStatus(TaskStatus.COMPLETED);
            taskRepository.save(task);

            messagingTemplate.convertAndSend(
                    "/topic/task-status",
                    "Task " + task.getId() + " completed successfully"
            );

            System.out.println("Task Completed: " + task.getId());

        } catch (Exception e) {

            Integer retries = task.getRetryCount() == null ? 0 : task.getRetryCount();

            if (retries < 3) {
                task.setRetryCount(retries + 1);
                taskRepository.save(task);

                messagingTemplate.convertAndSend(
                        "/topic/task-status",
                        "Task " + task.getId() + " failed. Retry attempt " + (retries + 1)
                );

                rabbitMQProducer.sendMessage(String.valueOf(task.getId()));

                System.out.println("Retrying Task: " + task.getId() +
                        " Attempt: " + (retries + 1));

            } else {
                task.setStatus(TaskStatus.FAILED);
                taskRepository.save(task);
                messagingTemplate.convertAndSend(
                        "/topic/task-status",
                        "Task " + task.getId() + " failed after 3 retries"
                );

                System.out.println("Task Failed After 3 Retries: " + task.getId());

                throw new RuntimeException("Moving to DLQ");

            }
        }
    }
}