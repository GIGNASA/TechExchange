package com.example.techexchange.dto.response;

import com.example.techexchange.entity.enums.DeviceCategory;
import com.example.techexchange.entity.enums.DeviceCondition;
import com.example.techexchange.entity.enums.DeviceStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class DeviceResponse {
    Long id;
    Long ownerId;
    String ownerName;
    String ownerEmail;
    String title;
    DeviceCategory category;
    DeviceCondition condition;
    DeviceStatus status;
    boolean active;
    String brand;
    String model;
    String city;
    String desiredExchange;
    String description;
    String imageUrl;
    boolean ownDevice;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
