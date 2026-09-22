package com.example.techexchange.controller;

import com.example.techexchange.dto.request.MessageRequest;
import com.example.techexchange.dto.response.MessageResponse;
import com.example.techexchange.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Повідомлення")
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/request/{requestId}")
    @Operation(summary = "Повідомлення за пропозицією")
    public List<MessageResponse> listMessages(@PathVariable Long requestId) {
        return messageService.listMessages(requestId);
    }

    @PostMapping("/request/{requestId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Надіслати повідомлення")
    public MessageResponse createMessage(@PathVariable Long requestId, @Valid @RequestBody MessageRequest request) {
        return messageService.createMessage(requestId, request);
    }
}
