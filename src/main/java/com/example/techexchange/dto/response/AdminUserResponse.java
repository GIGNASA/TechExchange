package com.example.techexchange.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class AdminUserResponse {
    Long id;
    String email;
    String fullName;
    String role;
    boolean active;
    double averageRating;
    long reviewsCount;
    LocalDateTime createdAt;
}
