package com.example.techexchange.service;

import com.example.techexchange.dto.request.DeviceRequest;
import com.example.techexchange.dto.response.DeviceResponse;
import com.example.techexchange.dto.response.MarketplaceStatsResponse;
import com.example.techexchange.entity.Device;
import com.example.techexchange.entity.User;
import com.example.techexchange.entity.enums.DeviceCategory;
import com.example.techexchange.entity.enums.DeviceCondition;
import com.example.techexchange.entity.enums.DeviceStatus;
import com.example.techexchange.exception.ResourceNotFoundException;
import com.example.techexchange.repository.DeviceRepository;
import com.example.techexchange.repository.ExchangeRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<DeviceResponse> listDevices(String query, DeviceCategory category,
                                            DeviceCondition condition, String city, Boolean mine) {
        User current = userService.currentUser();
        List<Device> devices = deviceRepository.findAllByOrderByUpdatedAtDesc();

        String normalizedQuery = normalize(query);
        String normalizedCity = normalize(city);

        return devices.stream()
                .filter(device -> Boolean.TRUE.equals(mine)
                        ? device.getOwner().getId().equals(current.getId())
                        : device.isActive())
                .filter(device -> category == null || device.getCategory() == category)
                .filter(device -> condition == null || device.getCondition() == condition)
                .filter(device -> normalizedCity.isBlank() || normalize(device.getCity()).contains(normalizedCity))
                .filter(device -> normalizedQuery.isBlank() || matchesQuery(device, normalizedQuery))
                .map(device -> toResponse(device, current))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> myDevices() {
        User current = userService.currentUser();
        return deviceRepository.findByOwnerOrderByUpdatedAtDesc(current).stream()
                .map(device -> toResponse(device, current))
                .toList();
    }

    @Transactional(readOnly = true)
    public DeviceResponse getDevice(Long id) {
        User current = userService.currentUser();
        Device device = findDevice(id);

        if (!device.isActive() && !device.getOwner().getId().equals(current.getId()) && !isAdmin(current)) {
            throw new AccessDeniedException("Оголошення недоступне");
        }

        return toResponse(device, current);
    }

    @Transactional
    public DeviceResponse createDevice(DeviceRequest request) {
        User current = userService.currentUser();
        Device device = Device.builder()
                .owner(current)
                .title(request.getTitle())
                .category(request.getCategory())
                .condition(request.getCondition())
                .status(request.getStatus() == null ? DeviceStatus.AVAILABLE : request.getStatus())
                .brand(request.getBrand())
                .model(request.getModel())
                .city(request.getCity())
                .desiredExchange(request.getDesiredExchange())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .active(true)
                .build();

        return toResponse(deviceRepository.save(device), current);
    }

    @Transactional
    public DeviceResponse updateDevice(Long id, DeviceRequest request) {
        User current = userService.currentUser();
        Device device = findDevice(id);
        requireOwnerOrAdmin(device, current);

        device.setTitle(request.getTitle());
        device.setCategory(request.getCategory());
        device.setCondition(request.getCondition());
        device.setStatus(request.getStatus() == null ? device.getStatus() : request.getStatus());
        device.setBrand(request.getBrand());
        device.setModel(request.getModel());
        device.setCity(request.getCity());
        device.setDesiredExchange(request.getDesiredExchange());
        device.setDescription(request.getDescription());
        device.setImageUrl(request.getImageUrl());

        return toResponse(device, current);
    }

    @Transactional
    public void deleteDevice(Long id) {
        User current = userService.currentUser();
        Device device = findDevice(id);
        requireOwnerOrAdmin(device, current);
        device.setActive(false);
        if (device.getStatus() == DeviceStatus.AVAILABLE) {
            device.setStatus(DeviceStatus.RESERVED);
        }
    }

    @Transactional(readOnly = true)
    public MarketplaceStatsResponse stats() {
        User current = userService.currentUser();

        long myDevices = deviceRepository.findByOwnerOrderByUpdatedAtDesc(current).stream()
                .filter(Device::isActive)
                .count();

        return MarketplaceStatsResponse.builder()
                .totalDevices(deviceRepository.countByActiveTrue())
                .myDevices(myDevices)
                .availableDevices(deviceRepository.countByStatusAndActiveTrue(DeviceStatus.AVAILABLE))
                .activeRequests(exchangeRequestRepository.countByOwnerOrRequester(current, current))
                .smartphones(deviceRepository.countByCategoryAndActiveTrue(DeviceCategory.SMARTPHONE))
                .laptops(deviceRepository.countByCategoryAndActiveTrue(DeviceCategory.LAPTOP))
                .gaming(deviceRepository.countByCategoryAndActiveTrue(DeviceCategory.GAMING))
                .build();
    }

    @Transactional(readOnly = true)
    public Device findDevice(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Оголошення не знайдено"));
    }

    public DeviceResponse toResponse(Device device, User current) {
        return DeviceResponse.builder()
                .id(device.getId())
                .ownerId(device.getOwner().getId())
                .ownerName(displayName(device.getOwner()))
                .ownerEmail(device.getOwner().getEmail())
                .title(device.getTitle())
                .category(device.getCategory())
                .condition(device.getCondition())
                .status(device.getStatus())
                .active(device.isActive())
                .brand(device.getBrand())
                .model(device.getModel())
                .city(device.getCity())
                .desiredExchange(device.getDesiredExchange())
                .description(device.getDescription())
                .imageUrl(device.getImageUrl())
                .ownDevice(current != null && device.getOwner().getId().equals(current.getId()))
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }

    private boolean matchesQuery(Device device, String query) {
        return normalize(device.getTitle()).contains(query)
                || normalize(device.getBrand()).contains(query)
                || normalize(device.getModel()).contains(query)
                || normalize(device.getDescription()).contains(query);
    }

    private void requireOwnerOrAdmin(Device device, User current) {
        if (!device.getOwner().getId().equals(current.getId()) && !isAdmin(current)) {
            throw new AccessDeniedException("Можна змінювати лише власні оголошення");
        }
    }

    private boolean isAdmin(User user) {
        return "ADMIN".equals(user.getRole().name());
    }

    private String displayName(User user) {
        return user.getFullName() == null || user.getFullName().isBlank() ? user.getEmail() : user.getFullName();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
