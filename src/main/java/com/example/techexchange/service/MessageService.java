package com.example.techexchange.service;

import com.example.techexchange.dto.request.MessageRequest;
import com.example.techexchange.dto.response.MessageResponse;
import com.example.techexchange.entity.ExchangeRequest;
import com.example.techexchange.entity.Message;
import com.example.techexchange.entity.User;
import com.example.techexchange.entity.enums.NotificationType;
import com.example.techexchange.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final ExchangeRequestService exchangeRequestService;
    private final UserService userService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<MessageResponse> listMessages(Long exchangeRequestId) {
        User current = userService.currentUser();
        ExchangeRequest exchangeRequest = exchangeRequestService.getAuthorizedRequest(exchangeRequestId);
        return messageRepository.findByExchangeRequestOrderByCreatedAtAsc(exchangeRequest).stream()
                .map(message -> toResponse(message, current))
                .toList();
    }

    @Transactional
    public MessageResponse createMessage(Long exchangeRequestId, MessageRequest request) {
        User current = userService.currentUser();
        ExchangeRequest exchangeRequest = exchangeRequestService.getAuthorizedRequest(exchangeRequestId);
        Message message = Message.builder()
                .exchangeRequest(exchangeRequest)
                .sender(current)
                .text(request.getText())
                .build();

        Message saved = messageRepository.save(message);

        User recipient = exchangeRequest.getRequester().getId().equals(current.getId())
                ? exchangeRequest.getOwner()
                : exchangeRequest.getRequester();

        notificationService.createForUser(
                recipient,
                exchangeRequest,
                NotificationType.NEW_MESSAGE,
                "Нове повідомлення",
                displayName(current) + " надіслав(ла) повідомлення у пропозиції #" + exchangeRequest.getId()
        );

        log.info("Message created: messageId={}, requestId={}, senderId={}",
                saved.getId(), exchangeRequest.getId(), current.getId());

        return toResponse(saved, current);
    }

    private MessageResponse toResponse(Message message, User current) {
        return MessageResponse.builder()
                .id(message.getId())
                .exchangeRequestId(message.getExchangeRequest().getId())
                .senderId(message.getSender().getId())
                .senderName(displayName(message.getSender()))
                .mine(message.getSender().getId().equals(current.getId()))
                .text(message.getText())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private String displayName(User user) {
        return user.getFullName() == null || user.getFullName().isBlank() ? user.getEmail() : user.getFullName();
    }
}
