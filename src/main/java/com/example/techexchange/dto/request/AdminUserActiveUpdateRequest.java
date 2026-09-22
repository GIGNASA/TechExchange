package com.example.techexchange.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserActiveUpdateRequest {

    @NotNull(message = "Параметр active обов'язковий")
    private Boolean active;
}
