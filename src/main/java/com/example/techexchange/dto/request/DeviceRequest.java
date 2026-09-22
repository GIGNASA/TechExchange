package com.example.techexchange.dto.request;

import com.example.techexchange.entity.enums.DeviceCategory;
import com.example.techexchange.entity.enums.DeviceCondition;
import com.example.techexchange.entity.enums.DeviceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceRequest {

    @NotBlank(message = "Назва техніки обов'язкова")
    @Size(max = 180)
    private String title;

    @NotNull(message = "Категорія обов'язкова")
    private DeviceCategory category;

    @NotNull(message = "Стан техніки обов'язковий")
    private DeviceCondition condition;

    private DeviceStatus status;

    @Size(max = 120)
    private String brand;

    @Size(max = 120)
    private String model;

    @Size(max = 120)
    private String city;

    @Size(max = 280)
    private String desiredExchange;

    @Size(max = 1400)
    private String description;

    @Size(max = 700)
    private String imageUrl;
}
