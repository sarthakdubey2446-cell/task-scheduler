package com.sarthak.taskscheduler.repository;

import com.sarthak.taskscheduler.entity.Task;
import com.sarthak.taskscheduler.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sarthak.taskscheduler.entity.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository
        extends JpaRepository<Task, Long> {
    List<Task> findByStatusAndScheduledTimeLessThanEqual(
            TaskStatus status,
            LocalDateTime time
    );
    List<Task> findByUser(User user);
    List<Task> findByUserAndStatus(
            User user,
            TaskStatus status
    );
    Page<Task> findByUser(
            User user,
            Pageable pageable
    );
}