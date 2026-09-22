package com.example.techexchange.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ReviewResponse {
    Long id;
    Long exchangeRequestId;
    Long fromUserId;
    String fromUserName;
    Long toUserId;
    String toUserName;
    Integer rating;
    String comment;
    LocalDateTime createdAt;
}
