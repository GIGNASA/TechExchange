package com.example.techexchange.controller;

import com.example.techexchange.dto.request.AdminDeviceActiveUpdateRequest;
import com.example.techexchange.dto.request.AdminUserActiveUpdateRequest;
import com.example.techexchange.dto.request.AdminUserRoleUpdateRequest;
import com.example.techexchange.dto.response.AdminUserResponse;
import com.example.techexchange.dto.response.DeviceResponse;
import com.example.techexchange.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Адміністрування")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "Список користувачів")
    public List<AdminUserResponse> users() {
        return adminService.listUsers();
    }

    @PutMapping("/users/{id}/role")
    @Operation(summary = "Оновити роль користувача")
    public AdminUserResponse updateUserRole(@PathVariable Long id,
                                            @Valid @RequestBody AdminUserRoleUpdateRequest request) {
        return adminService.updateUserRole(id, request.getRole());
    }

    @PutMapping("/users/{id}/active")
    @Operation(summary = "Деактивувати або активувати користувача")
    public AdminUserResponse updateUserActive(@PathVariable Long id,
                                              @Valid @RequestBody AdminUserActiveUpdateRequest request) {
        return adminService.updateUserActive(id, request.getActive());
    }

    @GetMapping("/devices")
    @Operation(summary = "Список оголошень")
    public List<DeviceResponse> devices() {
        return adminService.listDevices();
    }

    @PutMapping("/devices/{id}/active")
    @Operation(summary = "Деактивувати або активувати оголошення")
    public DeviceResponse updateDeviceActive(@PathVariable Long id,
                                             @Valid @RequestBody AdminDeviceActiveUpdateRequest request) {
        return adminService.updateDeviceActive(id, request.getActive());
    }
}
