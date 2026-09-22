package com.example.techexchange.service;

import com.example.techexchange.dto.response.NotificationResponse;
import com.example.techexchange.entity.ExchangeRequest;
import com.example.techexchange.entity.Notification;
import com.example.techexchange.entity.User;
import com.example.techexchange.entity.enums.NotificationType;
import com.example.techexchange.exception.ResourceNotFoundException;
import com.example.techexchange.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<NotificationResponse> myNotifications() {
        User current = userService.currentUser();
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(current).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(Long id) {
        User current = userService.currentUser();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Сповіщення не знайдено"));

        if (!notification.getRecipient().getId().equals(current.getId())) {
            throw new AccessDeniedException("Немає доступу до цього сповіщення");
        }

        notification.setRead(true);
        return toResponse(notification);
    }

    @Transactional
    public void createForUser(User recipient, ExchangeRequest exchangeRequest, NotificationType type, String title, String body) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .exchangeRequest(exchangeRequest)
                .type(type)
                .title(title)
                .body(body)
                .build();

        notificationRepository.save(notification);
        log.info("Notification created: recipientId={}, type={}, exchangeRequestId={}",
                recipient.getId(), type, exchangeRequest == null ? null : exchangeRequest.getId());
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .read(notification.isRead())
                .exchangeRequestId(notification.getExchangeRequest() == null ? null : notification.getExchangeRequest().getId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
