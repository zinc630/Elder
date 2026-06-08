package com.example.elder.web.controller;

import com.example.elder.domain.Role;
import com.example.elder.dto.*;
import com.example.elder.service.agency.AgencyDashboardService;
import com.example.elder.service.agency.AgencyModulesDbService;
import com.example.elder.web.ApiResponse;
import com.example.elder.web.ForbiddenException;
import com.example.elder.web.RoleResolver;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agency")
public class AgencyModulesController {

    private final AgencyModulesDbService modulesDbService;
    private final AgencyDashboardService dashboardService;

    public AgencyModulesController(
            AgencyModulesDbService modulesDbService,
            AgencyDashboardService dashboardService
    ) {
        this.modulesDbService = modulesDbService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard/stats")
    public ApiResponse<AgencyDashboardStatsDto> dashboardStats(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        return ApiResponse.ok(dashboardService.getStats());
    }

    @GetMapping("/alerts")
    public ApiResponse<List<AgencyAlertDto>> listAlerts(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        return ApiResponse.ok(modulesDbService.listAlerts());
    }

    @PostMapping("/alerts/{alertId}/process")
    public ApiResponse<Object> processAlert(
            @PathVariable String alertId,
            @RequestParam(name = "source", defaultValue = "ANOMALY") String source,
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        modulesDbService.processAlert(alertId, source);
        return ApiResponse.ok(null);
    }

    @GetMapping("/health-records")
    public ApiResponse<List<AgencyHealthRecordDto>> listHealthRecords(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        return ApiResponse.ok(modulesDbService.listHealthRecords());
    }

    @GetMapping("/schedules")
    public ApiResponse<List<AgencyScheduleDto>> listSchedules(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        return ApiResponse.ok(modulesDbService.listSchedules());
    }

    @PostMapping("/schedules")
    public ApiResponse<Object> createSchedule(
            @RequestBody Map<String, Object> body,
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        String id = modulesDbService.createSchedule(
                str(body, "day"),
                intVal(body, "hour"),
                str(body, "workerName"),
                str(body, "type", "morning")
        );
        return ApiResponse.ok(id);
    }

    @DeleteMapping("/schedules/{scheduleId}")
    public ApiResponse<Object> deleteSchedule(
            @PathVariable String scheduleId,
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        modulesDbService.deleteSchedule(scheduleId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/evaluations")
    public ApiResponse<List<AgencyEvaluationDto>> listEvaluations(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        return ApiResponse.ok(modulesDbService.listEvaluations());
    }

    @GetMapping("/activities")
    public ApiResponse<List<AgencyActivityDto>> listActivities(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        return ApiResponse.ok(modulesDbService.listActivities());
    }

    @PostMapping("/activities")
    public ApiResponse<Object> createActivity(
            @RequestBody Map<String, Object> body,
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        String id = modulesDbService.createActivity(
                str(body, "title"),
                Instant.parse(str(body, "startTime")),
                Instant.parse(str(body, "endTime")),
                str(body, "location", ""),
                intVal(body, "maxParticipants", 30),
                str(body, "description", ""),
                optionalStr(body, "tag"),
                optionalStr(body, "icon")
        );
        return ApiResponse.ok(id);
    }

    @PostMapping("/activities/{activityId}/checkin")
    public ApiResponse<Object> activityCheckin(
            @PathVariable String activityId,
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        modulesDbService.incrementActivityRegistration(activityId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/finance")
    public ApiResponse<List<AgencyFinanceDto>> listFinance(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        return ApiResponse.ok(modulesDbService.listFinance());
    }

    @PostMapping("/finance")
    public ApiResponse<Object> createFinance(
            @RequestBody Map<String, Object> body,
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        String id = modulesDbService.createFinance(
                str(body, "elderName"),
                str(body, "serviceType", "NURSING"),
                new BigDecimal(str(body, "amount", "0")),
                str(body, "status", "pending")
        );
        return ApiResponse.ok(id);
    }

    @PostMapping("/finance/{financeId}/pay")
    public ApiResponse<Object> markFinancePaid(
            @PathVariable String financeId,
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        modulesDbService.markFinancePaid(financeId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/notifications")
    public ApiResponse<List<AgencyNotificationDto>> listNotifications(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        return ApiResponse.ok(modulesDbService.listNotifications());
    }

    @PostMapping("/notifications")
    public ApiResponse<Object> createNotification(
            @RequestBody Map<String, Object> body,
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        String id = modulesDbService.createNotification(
                str(body, "title"),
                str(body, "content"),
                str(body, "status", "draft"),
                str(body, "author", "机构管理员")
        );
        return ApiResponse.ok(id);
    }

    @PatchMapping("/notifications/{notificationId}")
    public ApiResponse<Object> updateNotification(
            @PathVariable String notificationId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        String status = optionalStr(body, "status");
        if (status != null) {
            modulesDbService.updateNotificationStatus(notificationId, status);
        }
        modulesDbService.patchNotification(
                notificationId,
                optionalStr(body, "title"),
                optionalStr(body, "content"),
                null
        );
        return ApiResponse.ok(null);
    }

    @PatchMapping("/devices/{deviceId}")
    public ApiResponse<Object> patchDevice(
            @PathVariable String deviceId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        if (body.containsKey("elderName")) {
            Object v = body.get("elderName");
            String name = v == null ? null : String.valueOf(v).trim();
            if (name != null && name.isEmpty()) name = null;
            modulesDbService.patchDeviceElder(deviceId, name);
        }
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/notifications/{notificationId}")
    public ApiResponse<Object> deleteNotification(
            @PathVariable String notificationId,
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        modulesDbService.deleteNotification(notificationId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/devices")
    public ApiResponse<List<AgencyDeviceDto>> listDevices(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        return ApiResponse.ok(modulesDbService.listDevices());
    }

    @PostMapping("/devices")
    public ApiResponse<Object> createDevice(
            @RequestBody Map<String, Object> body,
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        String id = modulesDbService.createDevice(
                str(body, "name"),
                str(body, "type", "设备"),
                optionalStr(body, "elderName"),
                intVal(body, "battery", 100),
                str(body, "status", "offline")
        );
        return ApiResponse.ok(id);
    }

    @DeleteMapping("/devices/{deviceId}")
    public ApiResponse<Object> deleteDevice(
            @PathVariable String deviceId,
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        modulesDbService.deleteDevice(deviceId);
        return ApiResponse.ok(null);
    }

    private void requireAgency(String roleHeader) {
        Role role = RoleResolver.resolve(roleHeader);
        if (!Role.agencyOrAdmin(role)) throw new ForbiddenException("agency_only");
    }

    private static String str(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? "" : String.valueOf(v).trim();
    }

    private static String str(Map<String, Object> body, String key, String def) {
        String s = str(body, key);
        return s.isEmpty() ? def : s;
    }

    private static String optionalStr(Map<String, Object> body, String key) {
        if (!body.containsKey(key)) return null;
        Object v = body.get(key);
        if (v == null) return null;
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }

    private static int intVal(Map<String, Object> body, String key) {
        return intVal(body, key, 0);
    }

    private static int intVal(Map<String, Object> body, String key, int def) {
        Object v = body.get(key);
        if (v == null) return def;
        if (v instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
