package com.example.techexchange.dto.response;

import com.example.techexchange.entity.enums.NotificationType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class NotificationResponse {
    Long id;
    NotificationType type;
    String title;
    String body;
    boolean read;
    Long exchangeRequestId;
    LocalDateTime createdAt;
}
