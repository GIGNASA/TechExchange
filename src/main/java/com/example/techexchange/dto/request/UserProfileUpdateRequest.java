package com.example.techexchange.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileUpdateRequest {

    @NotBlank(message = "Ім'я обов'язкове")
    @Size(max = 255, message = "Ім'я занадто довге")
    private String fullName;
}
