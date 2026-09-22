package com.example.techexchange.controller;

import com.example.techexchange.dto.request.DeviceRequest;
import com.example.techexchange.dto.response.DeviceResponse;
import com.example.techexchange.dto.response.MarketplaceStatsResponse;
import com.example.techexchange.entity.enums.DeviceCategory;
import com.example.techexchange.entity.enums.DeviceCondition;
import com.example.techexchange.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Tag(name = "Техніка")
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    @Operation(summary = "Каталог техніки")
    public List<DeviceResponse> listDevices(@RequestParam(required = false) String query,
                                            @RequestParam(required = false) DeviceCategory category,
                                            @RequestParam(required = false) DeviceCondition condition,
                                            @RequestParam(required = false) String city,
                                            @RequestParam(required = false) Boolean mine) {
        return deviceService.listDevices(query, category, condition, city, mine);
    }

    @GetMapping("/my")
    @Operation(summary = "Мої оголошення")
    public List<DeviceResponse> myDevices() {
        return deviceService.myDevices();
    }

    @GetMapping("/stats")
    @Operation(summary = "Статистика маркетплейсу")
    public MarketplaceStatsResponse stats() {
        return deviceService.stats();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Деталі техніки")
    public DeviceResponse getDevice(@PathVariable Long id) {
        return deviceService.getDevice(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Створення оголошення")
    public DeviceResponse createDevice(@Valid @RequestBody DeviceRequest request) {
        return deviceService.createDevice(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Редагування оголошення")
    public DeviceResponse updateDevice(@PathVariable Long id, @Valid @RequestBody DeviceRequest request) {
        return deviceService.updateDevice(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Видалення оголошення")
    public void deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
    }
}
