
package com.sarthak.taskscheduler.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void invalidTaskRequestsShouldBeRejected() throws Exception {

        // 1. Register a test user
        String email = "validation-" + UUID.randomUUID() + "@example.com";
        String password = "Password@123";

        String registerRequest = """
                {
                    "name": "Validation Test User",
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

        // 3. Missing title
        String missingTitleRequest = """
                {
                    "description": "Testing missing title",
                    "scheduledTime": "2099-01-01T10:00:00",
                    "recipientEmail": "receiver@example.com",
                    "emailSubject": "Test Subject",
                    "emailBody": "Test Body"
                }
                """;

        mockMvc.perform(post("/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingTitleRequest))
                .andExpect(status().isBadRequest());

        // 4. Invalid recipient email
        String invalidEmailRequest = """
                {
                    "title": "Invalid Email Task",
                    "description": "Testing invalid email",
                    "scheduledTime": "2099-01-01T10:00:00",
                    "recipientEmail": "invalid-email",
                    "emailSubject": "Test Subject",
                    "emailBody": "Test Body"
                }
                """;

        mockMvc.perform(post("/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEmailRequest))
                .andExpect(status().isBadRequest());

        // 5. Missing scheduled time
        String missingTimeRequest = """
                {
                    "title": "Missing Time Task",
                    "description": "Testing missing scheduled time",
                    "recipientEmail": "receiver@example.com",
                    "emailSubject": "Test Subject",
                    "emailBody": "Test Body"
                }
                """;

        mockMvc.perform(post("/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingTimeRequest))
                .andExpect(status().isBadRequest());

        // 6. Scheduled time in the past
        String pastTimeRequest = """
                {
                    "title": "Past Time Task",
                    "description": "Testing past scheduled time",
                    "scheduledTime": "2020-01-01T10:00:00",
                    "recipientEmail": "receiver@example.com",
                    "emailSubject": "Test Subject",
                    "emailBody": "Test Body"
                }
                """;

        mockMvc.perform(post("/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pastTimeRequest))
                .andExpect(status().isBadRequest());

        // 7. Empty title
        String emptyTitleRequest = """
                {
                    "title": "   ",
                    "description": "Testing empty title",
                    "scheduledTime": "2099-01-01T10:00:00",
                    "recipientEmail": "receiver@example.com",
                    "emailSubject": "Test Subject",
                    "emailBody": "Test Body"
                }
                """;

        mockMvc.perform(post("/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyTitleRequest))
                .andExpect(status().isBadRequest());
    }
}
