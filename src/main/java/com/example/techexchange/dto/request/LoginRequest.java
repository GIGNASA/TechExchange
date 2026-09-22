package com.example.techexchange.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email обов'язковий")
    @Email(message = "Невірний формат email")
    private String email;

    @NotBlank(message = "Пароль обов'язковий")
    private String password;
}
