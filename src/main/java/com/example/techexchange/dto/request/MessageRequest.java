package com.example.techexchange.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequest {

    @NotBlank(message = "Повідомлення не може бути порожнім")
    @Size(max = 1200)
    private String text;
}
