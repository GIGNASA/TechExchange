package com.example.techexchange.dto.request;

import com.example.techexchange.entity.enums.ExchangeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExchangeStatusUpdateRequest {

    @NotNull(message = "Статус обов'язковий")
    private ExchangeStatus status;
}
