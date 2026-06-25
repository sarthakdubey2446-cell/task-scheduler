package com.sarthak.taskscheduler.controller;

import com.sarthak.taskscheduler.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class MailTestController {

    private final EmailService emailService;

    @GetMapping("/mail")
    public String testMail() {

        emailService.sendEmail(
                "sarthakdubey2446@gmail.com",
                "Spring Boot Test",
                "Mail Working!"
        );

        return "Mail Sent";
    }
}