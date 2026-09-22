package com.example.techexchange.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email обов'язковий")
    @Email(message = "Невірний формат email")
    private String email;

    @NotBlank(message = "Пароль обов'язковий")
    @Size(min = 6, message = "Пароль не менше 6 символів")
    private String password;

    @NotBlank(message = "Ім'я обов'язкове")
    private String fullName;
}
