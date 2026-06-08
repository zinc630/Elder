package com.example.elder.web.controller;

import com.example.elder.domain.DispatchTask;
import com.example.elder.dto.ServiceOrderCreateRequest;
import com.example.elder.dto.ServiceOrderDto;
import com.example.elder.service.dispatch.DispatchService;
import com.example.elder.service.elder.ElderDbService;
import com.example.elder.web.ApiResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ServiceOrderController {

    private final DispatchService dispatchService;
    private final ElderDbService elderDbService;

    public ServiceOrderController(DispatchService dispatchService, ElderDbService elderDbService) {
        this.dispatchService = dispatchService;
        this.elderDbService = elderDbService;
    }

    @PostMapping("/elders/{elderId}/service-orders")
    public ApiResponse<ServiceOrderDto> create(
            @PathVariable("elderId") String elderId,
            @Valid @RequestBody ServiceOrderCreateRequest req
    ) {
        String normalizedElderId = elderId.trim().toUpperCase();
        if (elderDbService.getElderProfile(normalizedElderId).isEmpty()) {
            throw new IllegalArgumentException("invalid_elder_id: 请使用老人系统账号（如 E100001）");
        }
        String role = req.bookedByRole() == null || req.bookedByRole().isBlank() ? "ELDER" : req.bookedByRole().trim().toUpperCase();
        DispatchTask task = dispatchService.createBookingTask(
                normalizedElderId,
                "",
                req.serviceType(),
                Instant.ofEpochMilli(req.appointmentTimeMillis()),
                req.notes(),
                role,
                req.bookedByUserId() == null ? "" : req.bookedByUserId(),
                req.bookedByName() == null ? "" : req.bookedByName()
        );
        return ApiResponse.ok(toOrderDto(task));
    }

    @GetMapping("/elders/{elderId}/service-orders")
    public ApiResponse<List<ServiceOrderDto>> list(@PathVariable("elderId") String elderId) {
        List<ServiceOrderDto> orders = dispatchService.listTasksForElder(elderId, 30).stream()
                .map(ServiceOrderController::toOrderDto)
                .toList();
        return ApiResponse.ok(orders);
    }

    static ServiceOrderDto toOrderDto(DispatchTask t) {
        return new ServiceOrderDto(
                t.getId(),
                t.getElderId(),
                t.getServiceType(),
                serviceTypeLabel(t.getServiceType()),
                t.getStatus(),
                statusLabel(t.getStatus()),
                t.getAppointmentTime(),
                t.getNotes(),
                t.getBookedByRole(),
                t.getBookedByName(),
                t.getCreatedAt()
        );
    }

    private static String serviceTypeLabel(String st) {
        if (st == null) {
            return "";
        }
        return switch (st.toUpperCase()) {
            case "NURSING" -> "助餐（送餐上门）";
            case "HOUSEKEEPING" -> "家政保洁";
            case "ACCOMPANY" -> "陪诊陪护";
            case "BATH" -> "助浴";
            default -> st;
        };
    }

    private static String statusLabel(DispatchTask.Status status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case NEW -> "待派单";
            case ASSIGNED -> "派单中";
            case ARRIVING -> "前往中";
            case IN_PROGRESS -> "服务中";
            case COMPLETED -> "已完成";
            case CANCELLED -> "已取消";
        };
    }
}
