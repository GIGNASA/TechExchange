package com.example.techexchange.controller;

import com.example.techexchange.dto.request.ExchangeRequestCreateRequest;
import com.example.techexchange.dto.request.ExchangeStatusUpdateRequest;
import com.example.techexchange.dto.response.ExchangeRequestResponse;
import com.example.techexchange.service.ExchangeRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchange-requests")
@RequiredArgsConstructor
@Tag(name = "Пропозиції обміну")
public class ExchangeRequestController {

    private final ExchangeRequestService exchangeRequestService;

    @GetMapping
    @Operation(summary = "Мої пропозиції обміну")
    public List<ExchangeRequestResponse> listMyRequests() {
        return exchangeRequestService.listMyRequests();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Створення пропозиції обміну")
    public ExchangeRequestResponse createRequest(@Valid @RequestBody ExchangeRequestCreateRequest request) {
        return exchangeRequestService.createRequest(request);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Зміна статусу пропозиції")
    public ExchangeRequestResponse updateStatus(@PathVariable Long id,
                                                @Valid @RequestBody ExchangeStatusUpdateRequest request) {
        return exchangeRequestService.updateStatus(id, request);
    }
}
