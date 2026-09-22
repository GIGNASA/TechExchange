package com.example.techexchange.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDeviceActiveUpdateRequest {

    @NotNull(message = "Параметр active обов'язковий")
    private Boolean active;
}
