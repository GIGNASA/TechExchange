package com.example.techexchange.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class MessageResponse {
    Long id;
    Long exchangeRequestId;
    Long senderId;
    String senderName;
    boolean mine;
    String text;
    LocalDateTime createdAt;
}
