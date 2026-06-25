package com.sarthak.taskscheduler.service;

import com.sarthak.taskscheduler.dto.LoginRequest;
import com.sarthak.taskscheduler.dto.LoginResponse;
import com.sarthak.taskscheduler.dto.RegisterRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);
}