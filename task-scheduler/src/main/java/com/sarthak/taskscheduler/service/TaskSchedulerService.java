package com.sarthak.taskscheduler.service;

import com.sarthak.taskscheduler.config.RabbitMQProducer;
import com.sarthak.taskscheduler.entity.Task;
import com.sarthak.taskscheduler.entity.TaskStatus;
import com.sarthak.taskscheduler.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskSchedulerService {

    private final TaskRepository taskRepository;
    private final RabbitMQProducer rabbitMQProducer;


    @Scheduled(fixedRate = 60000)
    public void processTasks() {

        log.info("Checking pending tasks...");

        List<Task> tasks =
                taskRepository
                        .findByStatusAndScheduledTimeLessThanEqual(
                                TaskStatus.PENDING,
                                LocalDateTime.now()
                        );
        for (Task task : tasks) {

            log.info(
                    "Sending Task To Queue: {}",
                    task.getTitle()
            );

            rabbitMQProducer.sendMessage(
                    String.valueOf(task.getId())
            );
        }
    }
}