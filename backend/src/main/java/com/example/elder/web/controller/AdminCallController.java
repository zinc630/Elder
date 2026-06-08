package com.example.elder.web.controller;

import com.example.elder.domain.AlarmEvent;
import com.example.elder.domain.CallAttempt;
import com.example.elder.domain.Role;
import com.example.elder.dto.CallAttemptResultRequest;
import com.example.elder.service.alarm.AlarmDbService;
import com.example.elder.store.InMemoryDataStore;
import com.example.elder.web.ApiResponse;
import com.example.elder.web.ForbiddenException;
import com.example.elder.web.RoleResolver;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminCallController {
    private final InMemoryDataStore store;
    private final AlarmDbService alarmDbService;

    public AdminCallController(InMemoryDataStore store, AlarmDbService alarmDbService) {
        this.store = store;
        this.alarmDbService = alarmDbService;
    }

    @GetMapping("/alarms/{alarmEventId}/call-attempts")
    public ApiResponse<List<CallAttempt>> listCallAttempts(@PathVariable("alarmEventId") String alarmEventId,
                                                            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        Role role = RoleResolver.resolve(roleHeader);
        if (role != Role.ADMIN) throw new ForbiddenException("admin_only");
        return ApiResponse.ok(store.listCallAttempts(alarmEventId));
    }

    @PostMapping("/call-attempts/{callAttemptId}/result")
    public ApiResponse<Object> setCallResult(@PathVariable("callAttemptId") String callAttemptId,
                                               @RequestHeader(name = "X-Role", required = false) String roleHeader,
                                               @Valid @RequestBody CallAttemptResultRequest req) {
        Role role = RoleResolver.resolve(roleHeader);
        if (role != Role.ADMIN) throw new ForbiddenException("admin_only");
        store.updateCallAttemptResult(callAttemptId, req.result(), req.detail());
        return ApiResponse.ok(null);
    }

    @GetMapping("/alarms/pending")
    public ApiResponse<List<AlarmEvent>> listPending(@RequestHeader(name = "X-Role", required = false) String roleHeader) {
        Role role = RoleResolver.resolve(roleHeader);
        if (role != Role.ADMIN) throw new ForbiddenException("admin_only");
        return ApiResponse.ok(alarmDbService.listByStatuses(
                List.of(AlarmEvent.Status.CHILD_NOTIFIED, AlarmEvent.Status.CALL_PENDING),
                50
        ));
    }
}

