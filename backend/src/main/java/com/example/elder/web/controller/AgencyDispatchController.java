package com.example.elder.web.controller;

import com.example.elder.domain.DispatchTask;
import com.example.elder.domain.Role;
import com.example.elder.dto.*;
import com.example.elder.service.dispatch.DispatchService;
import com.example.elder.web.ApiResponse;
import com.example.elder.web.ForbiddenException;
import com.example.elder.web.RoleResolver;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/agency")
public class AgencyDispatchController {
    private final DispatchService dispatchService;

    public AgencyDispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @GetMapping("/dispatch/tasks")
    public ApiResponse<List<DispatchTaskDto>> listTasks(@RequestParam(value = "status", required = false) String status,
                                                           @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        Role role = RoleResolver.resolve(roleHeader);
        if (!Role.agencyOrAdmin(role)) throw new ForbiddenException("agency_only");

        List<DispatchTask> tasks;
        if (status == null || status.isBlank()) {
            tasks = dispatchService.listTasks(200);
        } else {
            DispatchTask.Status st = DispatchTask.Status.valueOf(status.trim().toUpperCase());
            tasks = dispatchService.listTasksByStatus(st);
        }
        List<DispatchTaskDto> dtos = tasks.stream().map(this::toDto).toList();
        return ApiResponse.ok(dtos);
    }

    @PostMapping("/dispatch/tasks")
    public ApiResponse<Object> createTask(@Valid @RequestBody AgencyDispatchCreateRequest req,
                                           @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        Role role = RoleResolver.resolve(roleHeader);
        if (!Role.agencyOrAdmin(role)) throw new ForbiddenException("agency_only");

        Instant appointmentTime = Instant.ofEpochMilli(req.appointmentTimeMillis());
        DispatchTask task = dispatchService.createTask(req.elderId(), "", req.serviceType(), appointmentTime, req.notes());
        return ApiResponse.ok(task.getId());
    }

    @PostMapping("/dispatch/tasks/{taskId}/assign")
    public ApiResponse<Object> assign(@PathVariable("taskId") String taskId,
                                       @Valid @RequestBody AssignWorkerRequest req,
                                       @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        Role role = RoleResolver.resolve(roleHeader);
        if (!Role.agencyOrAdmin(role)) throw new ForbiddenException("agency_only");

        DispatchTask task = dispatchService.assignWorker(taskId, req.workerId());
        return ApiResponse.ok(toDto(task));
    }

    @PatchMapping("/dispatch/tasks/{taskId}/status")
    public ApiResponse<Object> updateStatus(@PathVariable("taskId") String taskId,
                                               @RequestBody DispatchStatusUpdateRequest req,
                                               @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        Role role = RoleResolver.resolve(roleHeader);
        if (!Role.agencyOrAdmin(role)) throw new ForbiddenException("agency_only");
        DispatchTask task = dispatchService.updateStatus(taskId, req.toStatus());
        return ApiResponse.ok(toDto(task));
    }

    private DispatchTaskDto toDto(DispatchTask t) {
        return new DispatchTaskDto(
                t.getId(),
                t.getElderId(),
                t.getServiceType(),
                t.getStatus(),
                t.getAppointmentTime(),
                t.getNotes(),
                t.getAssignedWorkerId(),
                t.getCreatedAt(),
                t.getUpdatedAt(),
                t.getBookedByRole(),
                t.getBookedByUserId(),
                t.getBookedByName()
        );
    }
}

