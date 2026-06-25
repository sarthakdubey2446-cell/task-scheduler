package com.sarthak.taskscheduler.service;

public interface EmailService {

    void sendEmail(
            String to,
            String subject,
            String body
    );
}