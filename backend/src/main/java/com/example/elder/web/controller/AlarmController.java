package com.example.elder.web.controller;

import com.example.elder.domain.Role;
import com.example.elder.dto.AlarmConfirmRequest;
import com.example.elder.dto.AlarmDto;
import com.example.elder.service.AlarmService;
import com.example.elder.web.ApiResponse;
import com.example.elder.web.ForbiddenException;
import com.example.elder.web.RoleResolver;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AlarmController {
    private final AlarmService alarmService;

    public AlarmController(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    @GetMapping("/elders/{elderId}/alarms")
    public ApiResponse<List<AlarmDto>> list(@PathVariable("elderId") String elderId,
                                             @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(alarmService.listAlarmsByElder(elderId, limit));
    }

    @PostMapping("/elders/{elderId}/alarms/sos")
    public ApiResponse<AlarmDto> triggerSos(@PathVariable("elderId") String elderId,
                                           @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        Role role = RoleResolver.resolve(roleHeader);
        if (role != Role.ELDER && role != Role.CHILD && role != Role.ADMIN) {
            throw new ForbiddenException("role_not_allowed_to_trigger_sos");
        }
        return ApiResponse.ok(alarmService.triggerSos(elderId));
    }

    @GetMapping("/alarms/{alarmEventId}")
    public ApiResponse<AlarmDto> get(@PathVariable("alarmEventId") String alarmEventId) {
        return ApiResponse.ok(alarmService.getAlarm(alarmEventId));
    }

    @PostMapping("/alarms/{alarmEventId}/confirm")
    public ApiResponse<Object> confirm(@PathVariable("alarmEventId") String alarmEventId,
                                        @RequestHeader(name = "X-Role", required = false) String roleHeader,
                                        @Valid @RequestBody AlarmConfirmRequest req) {
        Role role = RoleResolver.resolve(roleHeader);
        if (role != Role.ELDER && role != Role.CHILD && role != Role.ADMIN) {
            throw new ForbiddenException("role_not_allowed_to_confirm");
        }
        alarmService.confirmAlarm(alarmEventId, req);
        return ApiResponse.ok(null);
    }
}

