package com.example.techexchange.controller;

import com.example.techexchange.dto.request.UserProfileUpdateRequest;
import com.example.techexchange.dto.response.UserResponse;
import com.example.techexchange.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Користувачі")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Поточний користувач")
    public UserResponse me() {
        return userService.currentUserResponse();
    }

    @PutMapping("/me")
    @Operation(summary = "Оновити профіль поточного користувача")
    public UserResponse updateMe(@Valid @RequestBody UserProfileUpdateRequest request) {
        return userService.updateCurrentUser(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Профіль користувача")
    public UserResponse userById(@PathVariable Long id) {
        return userService.userByIdResponse(id);
    }
}
