package com.example.techexchange.controller;

import com.example.techexchange.dto.response.NotificationResponse;
import com.example.techexchange.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Сповіщення")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Мої сповіщення")
    public List<NotificationResponse> myNotifications() {
        return notificationService.myNotifications();
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Позначити сповіщення прочитаним")
    public NotificationResponse markAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id);
    }
}
