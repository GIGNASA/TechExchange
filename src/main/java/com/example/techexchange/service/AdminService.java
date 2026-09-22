package com.example.techexchange.service;

import com.example.techexchange.dto.response.AdminUserResponse;
import com.example.techexchange.dto.response.DeviceResponse;
import com.example.techexchange.entity.Device;
import com.example.techexchange.entity.User;
import com.example.techexchange.entity.enums.DeviceStatus;
import com.example.techexchange.entity.enums.UserRole;
import com.example.techexchange.exception.ResourceNotFoundException;
import com.example.techexchange.repository.DeviceRepository;
import com.example.techexchange.repository.ReviewRepository;
import com.example.techexchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final DeviceService deviceService;

    @Transactional(readOnly = true)
    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toAdminUserResponse)
                .toList();
    }

    @Transactional
    public AdminUserResponse updateUserRole(Long userId, UserRole role) {
        User user = findUser(userId);
        user.setRole(role);
        log.info("Admin updated role: userId={}, role={}", userId, role);
        return toAdminUserResponse(user);
    }

    @Transactional
    public AdminUserResponse updateUserActive(Long userId, boolean active) {
        User current = userService.currentUser();
        if (current.getId().equals(userId) && !active) {
            throw new AccessDeniedException("Неможливо деактивувати власний акаунт");
        }

        User user = findUser(userId);
        user.setActive(active);
        log.info("Admin updated user active flag: userId={}, active={}", userId, active);
        return toAdminUserResponse(user);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> listDevices() {
        User admin = userService.currentUser();
        return deviceRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(device -> deviceService.toResponse(device, admin))
                .toList();
    }

    @Transactional
    public DeviceResponse updateDeviceActive(Long deviceId, boolean active) {
        User admin = userService.currentUser();
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Оголошення не знайдено"));

        device.setActive(active);
        if (!active && device.getStatus() == DeviceStatus.AVAILABLE) {
            device.setStatus(DeviceStatus.RESERVED);
        }
        if (active && device.getStatus() == DeviceStatus.RESERVED) {
            device.setStatus(DeviceStatus.AVAILABLE);
        }

        log.info("Admin updated device active flag: deviceId={}, active={}", deviceId, active);
        return deviceService.toResponse(device, admin);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Користувача не знайдено"));
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        long reviewsCount = reviewRepository.countByToUser(user);
        double averageRating = reviewsCount == 0 ? 0.0 : roundRating(reviewRepository.averageRatingFor(user));
        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .active(user.isActive())
                .averageRating(averageRating)
                .reviewsCount(reviewsCount)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private double roundRating(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
