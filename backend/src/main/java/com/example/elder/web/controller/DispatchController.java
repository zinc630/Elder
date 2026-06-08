package com.example.elder.web.controller;

import com.example.elder.domain.DispatchTask;
import com.example.elder.dto.DispatchCreateRequest;
import com.example.elder.dto.DispatchStatusUpdateRequest;
import com.example.elder.service.dispatch.DispatchService;
import com.example.elder.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1")
public class DispatchController {
    private final DispatchService dispatchService;

    public DispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @PostMapping("/dispatch/tasks")
    public ApiResponse<Object> create(@Valid @RequestBody DispatchCreateRequest req) {
        DispatchTask task = dispatchService.createTask(
                req.elderId(),
                req.alarmEventId(),
                req.serviceType(),
                Instant.now(),
                ""
        );
        return ApiResponse.ok(task.getId());
    }

    @PatchMapping("/dispatch/tasks/{taskId}/status")
    public ApiResponse<Object> updateStatus(@PathVariable("taskId") String taskId,
                                              @Valid @RequestBody DispatchStatusUpdateRequest req) {
        DispatchTask task = dispatchService.updateStatus(taskId, req.toStatus());
        return ApiResponse.ok(task.getStatus().name());
    }
}

