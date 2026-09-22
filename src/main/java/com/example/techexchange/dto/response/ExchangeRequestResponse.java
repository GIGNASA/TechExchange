package com.example.techexchange.dto.response;

import com.example.techexchange.entity.enums.ExchangeStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ExchangeRequestResponse {
    Long id;
    DeviceResponse targetDevice;
    DeviceResponse offeredDevice;
    Long requesterId;
    String requesterName;
    Long ownerId;
    String ownerName;
    ExchangeStatus status;
    String message;
    String direction;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
