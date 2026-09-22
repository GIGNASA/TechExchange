package com.example.techexchange.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewCreateRequest {

    @NotNull(message = "ID пропозиції обміну обов'язковий")
    private Long exchangeRequestId;

    @NotNull(message = "Оцінка обов'язкова")
    @Min(value = 1, message = "Оцінка повинна бути від 1 до 5")
    @Max(value = 5, message = "Оцінка повинна бути від 1 до 5")
    private Integer rating;

    @Size(max = 1200, message = "Коментар занадто довгий")
    private String comment;
}
