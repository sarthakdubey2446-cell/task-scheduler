
package com.sarthak.taskscheduler.controller;

import com.jayway.jsonpath.JsonPath;
import com.sarthak.taskscheduler.entity.Task;
import com.sarthak.taskscheduler.entity.User;
import com.sarthak.taskscheduler.repository.TaskRepository;
import com.sarthak.taskscheduler.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;


    // TEST 1: Create and retrieve task

    @Test
    void createAndRetrieveTaskTest() throws Exception {

        String email = "task-" + UUID.randomUUID() + "@example.com";
        String password = "Password@123";

        // 1. Register a user
        String registerRequest = """
                {
                    "name": "Task Integration User",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isOk());

        // 2. Login and obtain JWT
        String loginRequest = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password);

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String loginResponse =
                loginResult.getResponse().getContentAsString();

        String token = JsonPath.read(loginResponse, "$.token");

        // 3. Create a task
        String createTaskRequest = """
                {
                    "title": "Integration Test Task",
                    "description": "Testing task creation",
                    "scheduledTime": "2099-01-01T10:00:00",
                    "recipientEmail": "receiver@example.com",
                    "emailSubject": "Test Subject",
                    "emailBody": "Test email body"
                }
                """;

        mockMvc.perform(post("/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTaskRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("Task Created Successfully"));

        // 4. Retrieve all tasks
        MvcResult taskListResult = mockMvc.perform(get("/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title")
                        .value("Integration Test Task"))
                .andExpect(jsonPath("$[0].status")
                        .value("PENDING"))
                .andReturn();

        String taskListResponse =
                taskListResult.getResponse().getContentAsString();

        Long taskId = ((Number) JsonPath.read(
                taskListResponse, "$[0].id"
        )).longValue();

        // 5. Retrieve task by ID
        mockMvc.perform(get("/tasks/" + taskId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title")
                        .value("Integration Test Task"))
                .andExpect(jsonPath("$.status")
                        .value("PENDING"));
    }


    // TEST 2: Update and delete task

    @Test
    void updateAndDeleteTaskTest() throws Exception {

        String email = "update-" + UUID.randomUUID() + "@example.com";
        String password = "Password@123";

        // 1. Register a user
        String registerRequest = """
                {
                    "name": "Update Test User",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isOk());

        // 2. Login and obtain JWT
        String loginRequest = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password);

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String token = JsonPath.read(
                loginResult.getResponse().getContentAsString(),
                "$.token"
        );

        // 3. Create a task
        String createRequest = """
                {
                    "title": "Original Task",
                    "description": "Original Description",
                    "scheduledTime": "2099-01-01T10:00:00",
                    "recipientEmail": "receiver@example.com",
                    "emailSubject": "Test Subject",
                    "emailBody": "Test Body"
                }
                """;

        mockMvc.perform(post("/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isOk());

        // 4. Find the created task ID
        User user = userRepository.findByEmail(email)
                .orElseThrow();

        List<Task> tasks = taskRepository.findByUser(user);

        assertEquals(1, tasks.size());

        Long taskId = tasks.get(0).getId();

        // 5. Update the task
        String updateRequest = """
                {
                    "title": "Updated Task",
                    "description": "Updated Description",
                    "scheduledTime": "2099-02-01T12:00:00"
                }
                """;

        mockMvc.perform(put("/tasks/" + taskId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("Task Updated Successfully"));

        // 6. Verify updated values in database
        Task updatedTask = taskRepository.findById(taskId)
                .orElseThrow();

        assertEquals("Updated Task", updatedTask.getTitle());
        assertEquals("Updated Description", updatedTask.getDescription());

        // 7. Delete the task
        mockMvc.perform(delete("/tasks/" + taskId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Task Deleted Successfully"));

        // 8. Verify task has been deleted
        assertFalse(taskRepository.existsById(taskId));
    }


    // TEST 3: Verify task ownership and authorization

    @Test
    void userCannotAccessAnotherUsersTaskTest() throws Exception {

        String password = "Password@123";

        String emailA = "userA-" + UUID.randomUUID() + "@example.com";
        String emailB = "userB-" + UUID.randomUUID() + "@example.com";

        // 1. Register User A
        String registerA = """
                {
                    "name": "User A",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(emailA, password);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerA))
                .andExpect(status().isOk());

        // 2. Register User B
        String registerB = """
                {
                    "name": "User B",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(emailB, password);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerB))
                .andExpect(status().isOk());

        // 3. Login User A
        String loginA = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(emailA, password);

        MvcResult loginResultA = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginA))
                .andExpect(status().isOk())
                .andReturn();

        String tokenA = JsonPath.read(
                loginResultA.getResponse().getContentAsString(),
                "$.token"
        );

        // 4. Login User B
        String loginB = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(emailB, password);

        MvcResult loginResultB = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginB))
                .andExpect(status().isOk())
                .andReturn();

        String tokenB = JsonPath.read(
                loginResultB.getResponse().getContentAsString(),
                "$.token"
        );

        // 5. User A creates a task
        String createRequest = """
                {
                    "title": "User A Private Task",
                    "description": "This task belongs to User A",
                    "scheduledTime": "2099-01-01T10:00:00",
                    "recipientEmail": "receiver@example.com",
                    "emailSubject": "Private Task",
                    "emailBody": "Testing task ownership"
                }
                """;

        mockMvc.perform(post("/tasks")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isOk());

        // 6. Find User A's task ID
        User userA = userRepository.findByEmail(emailA)
                .orElseThrow();

        List<Task> tasks = taskRepository.findByUser(userA);

        assertEquals(1, tasks.size());

        Long taskId = tasks.get(0).getId();

        // 7. User B tries to retrieve User A's task
        mockMvc.perform(get("/tasks/" + taskId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());

        // 8. User B tries to update User A's task
        String updateRequest = """
                {
                    "title": "Hacked Task",
                    "description": "User B is trying to modify this task",
                    "scheduledTime": "2099-02-01T12:00:00"
                }
                """;

        mockMvc.perform(put("/tasks/" + taskId)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isForbidden());

        // 9. User B tries to delete User A's task
        mockMvc.perform(delete("/tasks/" + taskId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());

        // 10. Verify User A's task still exists and remains unchanged
        Task unchangedTask = taskRepository.findById(taskId)
                .orElseThrow();

        assertEquals("User A Private Task", unchangedTask.getTitle());
        assertEquals(
                "This task belongs to User A",
                unchangedTask.getDescription()
        );
    }
}
