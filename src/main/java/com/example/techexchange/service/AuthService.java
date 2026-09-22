package com.example.techexchange.service;

import com.example.techexchange.dto.request.LoginRequest;
import com.example.techexchange.dto.request.RegisterRequest;
import com.example.techexchange.dto.response.AuthResponse;
import com.example.techexchange.entity.User;
import com.example.techexchange.exception.ConflictException;
import com.example.techexchange.repository.UserRepository;
import com.example.techexchange.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email вже використовується: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .active(true)
                .build();

        userRepository.save(user);
        log.info("User registered: email={}, userId={}", user.getEmail(), user.getId());

        String token = tokenProvider.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getFullName(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String token = tokenProvider.generateToken(user.getEmail());
        log.info("User login: email={}, userId={}", user.getEmail(), user.getId());
        return new AuthResponse(token, user.getEmail(), user.getFullName(), user.getRole().name());
    }
}
