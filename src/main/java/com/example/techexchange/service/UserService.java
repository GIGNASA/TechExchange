package com.example.techexchange.service;

import com.example.techexchange.dto.request.UserProfileUpdateRequest;
import com.example.techexchange.dto.response.UserResponse;
import com.example.techexchange.entity.User;
import com.example.techexchange.exception.ResourceNotFoundException;
import com.example.techexchange.repository.ReviewRepository;
import com.example.techexchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Користувача не знайдено"));

        if (!user.isActive()) {
            throw new AccessDeniedException("Обліковий запис деактивовано");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Користувача не знайдено"));
    }

    @Transactional(readOnly = true)
    public UserResponse currentUserResponse() {
        return toResponse(currentUser());
    }

    @Transactional
    public UserResponse updateCurrentUser(UserProfileUpdateRequest request) {
        User user = currentUser();
        user.setFullName(request.getFullName().trim());
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse userByIdResponse(Long id) {
        return toResponse(findUser(id));
    }

    public UserResponse toResponse(User user) {
        long reviewsCount = reviewRepository.countByToUser(user);
        double averageRating = reviewsCount == 0 ? 0.0 : roundRating(reviewRepository.averageRatingFor(user));

        return UserResponse.builder()
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
