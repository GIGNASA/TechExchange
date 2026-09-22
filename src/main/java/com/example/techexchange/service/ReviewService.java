package com.example.techexchange.service;

import com.example.techexchange.dto.request.ReviewCreateRequest;
import com.example.techexchange.dto.response.ReviewResponse;
import com.example.techexchange.entity.ExchangeRequest;
import com.example.techexchange.entity.Review;
import com.example.techexchange.entity.User;
import com.example.techexchange.entity.enums.ExchangeStatus;
import com.example.techexchange.entity.enums.NotificationType;
import com.example.techexchange.exception.ConflictException;
import com.example.techexchange.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final ExchangeRequestService exchangeRequestService;
    private final NotificationService notificationService;

    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request) {
        User current = userService.currentUser();
        ExchangeRequest exchangeRequest = exchangeRequestService.getAuthorizedRequest(request.getExchangeRequestId());

        if (exchangeRequest.getStatus() != ExchangeStatus.RETURNED) {
            throw new IllegalArgumentException("Відгук можна залишити лише після завершення обміну");
        }

        if (reviewRepository.existsByExchangeRequestAndFromUser(exchangeRequest, current)) {
            throw new ConflictException("Ви вже залишили відгук по цій угоді");
        }

        User toUser = exchangeRequest.getRequester().getId().equals(current.getId())
                ? exchangeRequest.getOwner()
                : exchangeRequest.getRequester();

        Review review = Review.builder()
                .exchangeRequest(exchangeRequest)
                .fromUser(current)
                .toUser(toUser)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);

        notificationService.createForUser(
                toUser,
                exchangeRequest,
                NotificationType.NEW_REVIEW,
                "Новий відгук",
                displayName(current) + " залишив(ла) вам відгук"
        );

        log.info("Review created: reviewId={}, exchangeRequestId={}, fromUserId={}, toUserId={}, rating={}",
                saved.getId(), exchangeRequest.getId(), current.getId(), toUser.getId(), saved.getRating());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> reviewsForUser(Long userId) {
        User user = userService.findUser(userId);
        return reviewRepository.findByToUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> myReviews() {
        User current = userService.currentUser();
        return reviewRepository.findByToUserOrderByCreatedAtDesc(current).stream()
                .map(this::toResponse)
                .toList();
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .exchangeRequestId(review.getExchangeRequest().getId())
                .fromUserId(review.getFromUser().getId())
                .fromUserName(displayName(review.getFromUser()))
                .toUserId(review.getToUser().getId())
                .toUserName(displayName(review.getToUser()))
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    private String displayName(User user) {
        return user.getFullName() == null || user.getFullName().isBlank() ? user.getEmail() : user.getFullName();
    }
}
