package com.sarthak.taskscheduler.service;

import com.sarthak.taskscheduler.dto.LoginRequest;
import com.sarthak.taskscheduler.dto.LoginResponse;
import com.sarthak.taskscheduler.dto.RegisterRequest;
import com.sarthak.taskscheduler.entity.Role;
import com.sarthak.taskscheduler.entity.User;
import com.sarthak.taskscheduler.exception.ResourceAlreadyExistsException;
import com.sarthak.taskscheduler.repository.UserRepository;
import com.sarthak.taskscheduler.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(RegisterRequest request) {

        if(userRepository.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException(
                    "Email already exists"
            );
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }
    @Override
    public LoginResponse login(
            LoginRequest request
    ) {

        User user = userRepository
                .findByEmail(request.email())
                .orElseThrow(() ->
                        new RuntimeException("Invalid credentials")
                );

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {

            throw new RuntimeException(
                    "Invalid credentials"
            );
        }

        String token =
                jwtService.generateToken(
                        user.getEmail()
                );

        return new LoginResponse(token);
    }
}