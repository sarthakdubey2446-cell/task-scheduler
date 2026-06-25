package com.sarthak.taskscheduler.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TaskPageResponse {

    private List<TaskResponse> tasks;

    private int currentPage;

    private int totalPages;

    private long totalElements;
}