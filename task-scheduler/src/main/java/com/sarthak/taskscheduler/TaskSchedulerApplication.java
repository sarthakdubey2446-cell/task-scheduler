package com.sarthak.taskscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication(scanBasePackages = "com.sarthak.taskscheduler")
@EnableScheduling
public class TaskSchedulerApplication {
	public static void main(String[] args) {
		SpringApplication.run(TaskSchedulerApplication.class, args);
	}

}

