package com.example.techexchange.service;

import com.example.techexchange.dto.request.LoginRequest;
import com.example.techexchange.dto.request.RegisterRequest;
import com.example.techexchange.dto.response.AuthResponse;
import com.example.techexchange.entity.User;
import com.example.techexchange.entity.enums.UserRole;
import com.example.techexchange.exception.ConflictException;
import com.example.techexchange.repository.UserRepository;
import com.example.techexchange.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider tokenProvider;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks
    AuthService authService;

    @Test
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");
        req.setPassword("password123");
        req.setFullName("Test User");

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(req.getPassword())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tokenProvider.generateToken(req.getEmail())).thenReturn("jwt-token");

        AuthResponse response = authService.register(req);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throws() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("dup@example.com");
        req.setPassword("pass");
        req.setFullName("Dup");

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("pass");

        User user = User.builder()
                .email("user@example.com")
                .passwordHash("hashed")
                .fullName("User")
                .role(UserRole.USER)
                .build();

        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(tokenProvider.generateToken(req.getEmail())).thenReturn("jwt-token");

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
