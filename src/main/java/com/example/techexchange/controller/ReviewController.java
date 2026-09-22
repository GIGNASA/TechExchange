package com.example.techexchange.controller;

import com.example.techexchange.dto.request.ReviewCreateRequest;
import com.example.techexchange.dto.response.ReviewResponse;
import com.example.techexchange.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Відгуки")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Створити відгук")
    public ReviewResponse createReview(@Valid @RequestBody ReviewCreateRequest request) {
        return reviewService.createReview(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Мої отримані відгуки")
    public List<ReviewResponse> myReviews() {
        return reviewService.myReviews();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Відгуки користувача")
    public List<ReviewResponse> reviewsForUser(@PathVariable Long userId) {
        return reviewService.reviewsForUser(userId);
    }
}
