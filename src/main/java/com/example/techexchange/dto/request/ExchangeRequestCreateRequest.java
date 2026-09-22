package com.example.techexchange.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExchangeRequestCreateRequest {

    @NotNull(message = "Оголошення для обміну обов'язкове")
    private Long targetDeviceId;

    private Long offeredDeviceId;

    @Size(max = 1200)
    private String message;
}
