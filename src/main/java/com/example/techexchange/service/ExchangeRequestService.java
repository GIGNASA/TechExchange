package com.example.techexchange.service;

import com.example.techexchange.dto.request.ExchangeRequestCreateRequest;
import com.example.techexchange.dto.request.ExchangeStatusUpdateRequest;
import com.example.techexchange.dto.response.ExchangeRequestResponse;
import com.example.techexchange.entity.Device;
import com.example.techexchange.entity.ExchangeRequest;
import com.example.techexchange.entity.User;
import com.example.techexchange.entity.enums.DeviceStatus;
import com.example.techexchange.entity.enums.ExchangeStatus;
import com.example.techexchange.entity.enums.NotificationType;
import com.example.techexchange.exception.ResourceNotFoundException;
import com.example.techexchange.repository.ExchangeRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRequestService {

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final DeviceService deviceService;
    private final UserService userService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<ExchangeRequestResponse> listMyRequests() {
        User current = userService.currentUser();
        return exchangeRequestRepository.findByRequesterOrOwnerOrderByUpdatedAtDesc(current, current).stream()
                .map(request -> toResponse(request, current))
                .toList();
    }

    @Transactional(readOnly = true)
    public ExchangeRequest getAuthorizedRequest(Long id) {
        User current = userService.currentUser();
        ExchangeRequest request = findRequest(id);
        requireParticipant(request, current);
        return request;
    }

    @Transactional
    public ExchangeRequestResponse createRequest(ExchangeRequestCreateRequest request) {
        User current = userService.currentUser();
        Device target = deviceService.findDevice(request.getTargetDeviceId());

        if (!target.isActive()) {
            throw new IllegalArgumentException("Це оголошення деактивоване");
        }
        if (target.getOwner().getId().equals(current.getId())) {
            throw new IllegalArgumentException("Не можна створити пропозицію на власне оголошення");
        }
        if (target.getStatus() != DeviceStatus.AVAILABLE) {
            throw new IllegalArgumentException("Ця техніка вже недоступна для обміну");
        }

        Device offered = null;
        if (request.getOfferedDeviceId() != null) {
            offered = deviceService.findDevice(request.getOfferedDeviceId());
            if (!offered.isActive()) {
                throw new IllegalArgumentException("Запропонована техніка деактивована");
            }
            if (!offered.getOwner().getId().equals(current.getId())) {
                throw new IllegalArgumentException("Запропонувати можна лише власну техніку");
            }
            if (offered.getStatus() != DeviceStatus.AVAILABLE) {
                throw new IllegalArgumentException("Запропонована техніка вже недоступна");
            }
        }

        ExchangeRequest exchangeRequest = ExchangeRequest.builder()
                .targetDevice(target)
                .offeredDevice(offered)
                .requester(current)
                .owner(target.getOwner())
                .message(request.getMessage())
                .status(ExchangeStatus.PENDING)
                .build();

        ExchangeRequest saved = exchangeRequestRepository.save(exchangeRequest);
        notificationService.createForUser(
                saved.getOwner(),
                saved,
                NotificationType.EXCHANGE_REQUEST_CREATED,
                "Нова пропозиція обміну",
                displayName(saved.getRequester()) + " надіслав(ла) пропозицію по оголошенню \"" + saved.getTargetDevice().getTitle() + "\""
        );

        log.info("Exchange request created: requestId={}, requesterId={}, ownerId={}, targetDeviceId={}",
                saved.getId(), saved.getRequester().getId(), saved.getOwner().getId(), saved.getTargetDevice().getId());

        return toResponse(saved, current);
    }

    @Transactional
    public ExchangeRequestResponse updateStatus(Long id, ExchangeStatusUpdateRequest request) {
        User current = userService.currentUser();
        ExchangeRequest exchangeRequest = findRequest(id);
        requireParticipant(exchangeRequest, current);

        ExchangeStatus currentStatus = exchangeRequest.getStatus();
        ExchangeStatus nextStatus = request.getStatus();

        if (nextStatus == currentStatus) {
            return toResponse(exchangeRequest, current);
        }

        validateTransition(exchangeRequest, current, currentStatus, nextStatus);

        if (nextStatus == ExchangeStatus.ACCEPTED) {
            reserveDevices(exchangeRequest);
            declineCompetingRequests(exchangeRequest);
        }

        if (nextStatus == ExchangeStatus.CANCELED && currentStatus == ExchangeStatus.ACCEPTED) {
            releaseDevices(exchangeRequest);
        }

        if (nextStatus == ExchangeStatus.RETURNED) {
            completeExchange(exchangeRequest);
        }

        exchangeRequest.setStatus(nextStatus);
        notifyCounterparty(exchangeRequest, current, nextStatus);

        log.info("Exchange status updated: requestId={}, from={}, to={}, actorId={}",
                exchangeRequest.getId(), currentStatus, nextStatus, current.getId());

        return toResponse(exchangeRequest, current);
    }

    public ExchangeRequestResponse toResponse(ExchangeRequest request, User current) {
        String direction = request.getRequester().getId().equals(current.getId()) ? "OUTGOING" : "INCOMING";
        return ExchangeRequestResponse.builder()
                .id(request.getId())
                .targetDevice(deviceService.toResponse(request.getTargetDevice(), current))
                .offeredDevice(request.getOfferedDevice() == null ? null : deviceService.toResponse(request.getOfferedDevice(), current))
                .requesterId(request.getRequester().getId())
                .requesterName(displayName(request.getRequester()))
                .ownerId(request.getOwner().getId())
                .ownerName(displayName(request.getOwner()))
                .status(request.getStatus())
                .message(request.getMessage())
                .direction(direction)
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }

    private ExchangeRequest findRequest(Long id) {
        return exchangeRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пропозицію обміну не знайдено"));
    }

    private void requireParticipant(ExchangeRequest request, User current) {
        boolean participant = request.getRequester().getId().equals(current.getId())
                || request.getOwner().getId().equals(current.getId());
        if (!participant) {
            throw new AccessDeniedException("Немає доступу до цієї пропозиції");
        }
    }

    private void validateTransition(ExchangeRequest request, User actor, ExchangeStatus currentStatus, ExchangeStatus nextStatus) {
        switch (nextStatus) {
            case ACCEPTED -> {
                requireOwner(request, actor);
                requireStatus(currentStatus, ExchangeStatus.PENDING, "Пропозицію можна прийняти лише зі статусу PENDING");
            }
            case DECLINED -> {
                requireOwner(request, actor);
                requireStatus(currentStatus, ExchangeStatus.PENDING, "Пропозицію можна відхилити лише зі статусу PENDING");
            }
            case CANCELED -> {
                requireRequester(request, actor);
                if (currentStatus != ExchangeStatus.PENDING && currentStatus != ExchangeStatus.ACCEPTED) {
                    throw new IllegalArgumentException("Скасувати можна лише PENDING або ACCEPTED пропозицію");
                }
            }
            case ISSUED -> {
                requireOwner(request, actor);
                requireStatus(currentStatus, ExchangeStatus.ACCEPTED, "Передачу можна зафіксувати лише після ACCEPTED");
            }
            case RETURNED -> {
                requireStatus(currentStatus, ExchangeStatus.ISSUED, "Завершити обмін можна лише після ISSUED");
            }
            case PENDING -> throw new IllegalArgumentException("Повернути статус до PENDING неможливо");
        }
    }

    private void declineCompetingRequests(ExchangeRequest acceptedRequest) {
        List<ExchangeRequest> pending = exchangeRequestRepository.findByTargetDeviceAndStatus(
                acceptedRequest.getTargetDevice(), ExchangeStatus.PENDING
        );

        for (ExchangeRequest item : pending) {
            if (item.getId().equals(acceptedRequest.getId())) {
                continue;
            }
            item.setStatus(ExchangeStatus.DECLINED);
            notificationService.createForUser(
                    item.getRequester(),
                    item,
                    NotificationType.EXCHANGE_STATUS_CHANGED,
                    "Пропозицію відхилено",
                    "Іншу пропозицію по оголошенню \"" + item.getTargetDevice().getTitle() + "\" вже прийнято"
            );
        }
    }

    private void reserveDevices(ExchangeRequest request) {
        request.getTargetDevice().setStatus(DeviceStatus.RESERVED);
        if (request.getOfferedDevice() != null) {
            request.getOfferedDevice().setStatus(DeviceStatus.RESERVED);
        }
    }

    private void releaseDevices(ExchangeRequest request) {
        request.getTargetDevice().setStatus(DeviceStatus.AVAILABLE);
        if (request.getOfferedDevice() != null) {
            request.getOfferedDevice().setStatus(DeviceStatus.AVAILABLE);
        }
    }

    private void completeExchange(ExchangeRequest request) {
        request.getTargetDevice().setStatus(DeviceStatus.EXCHANGED);
        if (request.getOfferedDevice() != null) {
            request.getOfferedDevice().setStatus(DeviceStatus.EXCHANGED);
        }
    }

    private void notifyCounterparty(ExchangeRequest request, User actor, ExchangeStatus newStatus) {
        User receiver = request.getRequester().getId().equals(actor.getId()) ? request.getOwner() : request.getRequester();
        notificationService.createForUser(
                receiver,
                request,
                NotificationType.EXCHANGE_STATUS_CHANGED,
                "Оновлення статусу обміну",
                displayName(actor) + " змінив(ла) статус пропозиції на " + newStatus
        );
    }

    private void requireOwner(ExchangeRequest request, User actor) {
        if (!request.getOwner().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Цю дію може виконати лише власник оголошення");
        }
    }

    private void requireRequester(ExchangeRequest request, User actor) {
        if (!request.getRequester().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Цю дію може виконати лише автор пропозиції");
        }
    }

    private void requireStatus(ExchangeStatus currentStatus, ExchangeStatus expected, String message) {
        if (currentStatus != expected) {
            throw new IllegalArgumentException(message);
        }
    }

    private String displayName(User user) {
        return user.getFullName() == null || user.getFullName().isBlank() ? user.getEmail() : user.getFullName();
    }
}
