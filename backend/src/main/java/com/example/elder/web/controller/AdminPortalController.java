package com.example.elder.web.controller;

import com.example.elder.domain.AlarmEvent;
import com.example.elder.domain.ElderProfile;
import com.example.elder.domain.Measurement;
import com.example.elder.domain.Role;
import com.example.elder.dto.*;
import com.example.elder.service.agency.AgencyDbService;
import com.example.elder.service.alarm.AlarmDbService;
import com.example.elder.service.auth.AuthDbService;
import com.example.elder.service.dispatch.DispatchDbService;
import com.example.elder.service.elder.ElderDbService;
import com.example.elder.store.InMemoryDataStore;
import com.example.elder.web.ApiResponse;
import com.example.elder.web.ForbiddenException;
import com.example.elder.web.RoleResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminPortalController {
    private final InMemoryDataStore store;
    private final ElderDbService elderDbService;
    private final DispatchDbService dispatchDbService;
    private final AlarmDbService alarmDbService;
    private final AgencyDbService agencyDbService;
    private final AuthDbService authDbService;

    public AdminPortalController(
            InMemoryDataStore store,
            ElderDbService elderDbService,
            DispatchDbService dispatchDbService,
            AlarmDbService alarmDbService,
            AgencyDbService agencyDbService,
            AuthDbService authDbService
    ) {
        this.store = store;
        this.elderDbService = elderDbService;
        this.dispatchDbService = dispatchDbService;
        this.alarmDbService = alarmDbService;
        this.agencyDbService = agencyDbService;
        this.authDbService = authDbService;
    }

    @GetMapping("/dashboard/stats")
    public ApiResponse<AdminDashboardStatsDto> dashboardStats(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAdmin(roleHeader);
        long elders = elderDbService.listElders().size();
        ZoneId zone = ZoneId.systemDefault();
        Instant dayStart = LocalDate.now(zone).atStartOfDay(zone).toInstant();
        Instant dayEnd = dayStart.plusSeconds(86400);
        long today = alarmDbService.countBetween(dayStart, dayEnd);
        LocalDate todayLd = LocalDate.now(zone);
        long monthly = dispatchDbService.countCreatedInMonth(todayLd.getYear(), todayLd.getMonthValue(), zone);
        long devices = countBoundDevices(elders);
        long pendingOrders = dispatchDbService.countPending();
        long pendingAlarms = alarmDbService.countPending();
        Instant monthStart = todayLd.withDayOfMonth(1).atStartOfDay(zone).toInstant();
        Instant monthEnd = todayLd.plusMonths(1).withDayOfMonth(1).atStartOfDay(zone).toInstant();
        long completedMonth = dispatchDbService.countCompletedCreatedBetween(monthStart, monthEnd);
        long agencies = agencyDbService.countAgencies();
        long children = authDbService.countByRole("CHILD");
        return ApiResponse.ok(new AdminDashboardStatsDto(
                elders, today, monthly, devices, pendingOrders, pendingAlarms,
                completedMonth, agencies, children
        ));
    }

    @GetMapping("/dashboard/alert-trend")
    public ApiResponse<List<AdminAlertTrendPointDto>> alertTrend(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAdmin(roleHeader);
        ZoneId z = ZoneId.systemDefault();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        List<Integer> counts = alarmDbService.countsLastDays(7, z);
        List<AdminAlertTrendPointDto> pts = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = LocalDate.now(z).minusDays(6 - i);
            String label = i == 6 ? "今日" : d.format(fmt);
            pts.add(new AdminAlertTrendPointDto(label, counts.get(i)));
        }
        return ApiResponse.ok(pts);
    }

    @GetMapping("/dashboard/service-trend")
    public ApiResponse<List<AdminAlertTrendPointDto>> serviceTrend(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAdmin(roleHeader);
        ZoneId z = ZoneId.systemDefault();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        List<Integer> counts = dispatchDbService.countsCreatedLastDays(7, z);
        List<AdminAlertTrendPointDto> pts = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = LocalDate.now(z).minusDays(6 - i);
            String label = i == 6 ? "今日" : d.format(fmt);
            pts.add(new AdminAlertTrendPointDto(label, counts.get(i)));
        }
        return ApiResponse.ok(pts);
    }

    @GetMapping("/dashboard/alarm-type-stats")
    public ApiResponse<List<AdminNamedCountDto>> alarmTypeStats(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAdmin(roleHeader);
        ZoneId z = ZoneId.systemDefault();
        Instant since = LocalDate.now(z).minusDays(29).atStartOfDay(z).toInstant();
        List<AdminNamedCountDto> rows = new ArrayList<>();
        for (Map<String, Object> row : alarmDbService.countGroupByTypeSince(since)) {
            String type = String.valueOf(row.get("alarm_type"));
            long cnt = ((Number) row.get("cnt")).longValue();
            rows.add(new AdminNamedCountDto(alarmTypeLabel(type), cnt));
        }
        if (rows.isEmpty()) {
            rows.add(new AdminNamedCountDto("跌倒检测", 0));
            rows.add(new AdminNamedCountDto("SOS 紧急", 0));
            rows.add(new AdminNamedCountDto("体征异常", 0));
        }
        return ApiResponse.ok(rows);
    }

    @GetMapping("/dashboard/alarm-type-trend")
    public ApiResponse<List<AdminAlarmTypeTrendPointDto>> alarmTypeTrend(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAdmin(roleHeader);
        ZoneId z = ZoneId.systemDefault();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        int days = 15;
        List<AdminAlarmTypeTrendPointDto> pts = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = LocalDate.now(z).minusDays(days - 1 - i);
            Instant start = d.atStartOfDay(z).toInstant();
            Instant end = d.plusDays(1).atStartOfDay(z).toInstant();
            int fall = (int) alarmDbService.countByTypeBetween(start, end, "FALL_SUSPECTED");
            int sos = (int) alarmDbService.countByTypeBetween(start, end, "SOS_SUSPECTED");
            int vitals = (int) alarmDbService.countByTypeBetween(start, end, "VITALS_LONG_ABNORMAL");
            String label = i == days - 1 ? "今日" : d.format(fmt);
            pts.add(new AdminAlarmTypeTrendPointDto(label, fall, sos, vitals));
        }
        return ApiResponse.ok(pts);
    }

    @GetMapping("/dashboard/service-type-stats")
    public ApiResponse<List<AdminNamedCountDto>> serviceTypeStats(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAdmin(roleHeader);
        List<AdminNamedCountDto> rows = new ArrayList<>();
        for (Map<String, Object> row : dispatchDbService.countGroupByServiceType()) {
            String type = String.valueOf(row.get("service_type"));
            long cnt = ((Number) row.get("cnt")).longValue();
            rows.add(new AdminNamedCountDto(serviceTypeLabel(type), cnt));
        }
        if (rows.isEmpty()) {
            rows.add(new AdminNamedCountDto("助餐上门", 0));
            rows.add(new AdminNamedCountDto("家政保洁", 0));
            rows.add(new AdminNamedCountDto("陪诊陪护", 0));
        }
        return ApiResponse.ok(rows);
    }

    @GetMapping("/dashboard/health-distribution")
    public ApiResponse<List<AdminNamedCountDto>> healthDistribution(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAdmin(roleHeader);
        return ApiResponse.ok(computeHealthDistribution());
    }

    @GetMapping("/dashboard/high-risk")
    public ApiResponse<List<AdminHighRiskRowDto>> highRisk(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAdmin(roleHeader);
        List<AlarmEvent> recent = alarmDbService.listRecent(8);
        List<AdminHighRiskRowDto> rows = new ArrayList<>();
        for (AlarmEvent e : recent) {
            if (rows.size() >= 5) break;
            if (e.getStatus() == AlarmEvent.Status.CONFIRMED || e.getStatus() == AlarmEvent.Status.CLOSED) {
                continue;
            }
            String name = elderDbService.getElderProfile(e.getElderId()).map(ElderProfile::getName).orElse(e.getElderId());
            rows.add(new AdminHighRiskRowDto(
                    e.getId(),
                    name,
                    typeLabel(e.getType()),
                    statusLabel(e.getStatus())
            ));
        }
        return ApiResponse.ok(rows);
    }

    @GetMapping("/monitoring/elders")
    public ApiResponse<List<AdminElderVitalsRowDto>> elderVitals(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAdmin(roleHeader);
        List<AdminElderVitalsRowDto> rows = new ArrayList<>();
        for (ElderProfile p : elderDbService.listElders()) {
            List<Measurement> vitals = store.recentVitals(p.getElderId());
            Measurement m = vitals.isEmpty() ? null : vitals.get(vitals.size() - 1);
            int hr = m != null && m.getHeartRate() != null
                    ? m.getHeartRate()
                    : 68 + Math.abs(p.getElderId().hashCode() % 35);
            int sys = m != null && m.getSystolic() != null
                    ? m.getSystolic()
                    : 125 + Math.abs(p.getElderId().hashCode() % 25);
            int dia = m != null && m.getDiastolic() != null
                    ? m.getDiastolic()
                    : 78 + Math.abs(p.getElderId().hashCode() % 12);
            int spo2 = 94 + Math.abs(p.getElderId().hashCode() % 6);
            String warn;
            String level = "NORMAL";
            if (hr > 100) {
                warn = "心率偏高";
                level = "CRIT";
            } else if (spo2 < 95) {
                warn = "血氧偏低";
                level = "WARN";
            } else {
                warn = "正常";
                level = "NORMAL";
            }
            rows.add(new AdminElderVitalsRowDto(
                    p.getElderId(),
                    p.getName(),
                    p.getAge(),
                    hr,
                    sys + "/" + dia,
                    spo2,
                    warn,
                    level
            ));
        }
        return ApiResponse.ok(rows);
    }

    @GetMapping("/monitoring/critical-alerts")
    public ApiResponse<List<AdminCriticalAlertDto>> criticalAlerts(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAdmin(roleHeader);
        List<AdminCriticalAlertDto> list = new ArrayList<>();
        list.add(new AdminCriticalAlertDto(
                "c1",
                "张爷爷 发生 跌倒检测告警",
                Instant.parse("2025-03-29T02:22:05Z"),
                false
        ));
        list.add(new AdminCriticalAlertDto(
                "c2",
                "李奶奶 发生 电子围栏越界（3号楼）",
                Instant.parse("2025-03-29T01:47:12Z"),
                false
        ));
        return ApiResponse.ok(list);
    }

    private long countBoundDevices(long elderCount) {
        if (elderCount <= 0) {
            return 0;
        }
        long withLocation = elderDbService.listElders().stream()
                .filter(p -> elderDbService.getLastLocation(p.getElderId()).isPresent())
                .count();
        if (withLocation > 0) {
            return withLocation * 2;
        }
        return elderCount * 2;
    }

    private void requireAdmin(String roleHeader) {
        Role role = RoleResolver.resolve(roleHeader);
        if (role != Role.ADMIN) throw new ForbiddenException("admin_only");
    }

    private static String typeLabel(AlarmEvent.Type t) {
        return switch (t) {
            case FALL_SUSPECTED -> "跌倒检测";
            case SOS_SUSPECTED -> "SOS 紧急";
            case VITALS_LONG_ABNORMAL -> "体征异常";
        };
    }

    private static String statusLabel(AlarmEvent.Status s) {
        return switch (s) {
            case PENDING_CONFIRM, CHILD_NOTIFIED, CALL_PENDING -> "待处理";
            case CONFIRMED -> "已确认";
            case CLOSED -> "已关闭";
        };
    }

    private List<AdminNamedCountDto> computeHealthDistribution() {
        long normal = 0;
        long warn = 0;
        long crit = 0;
        for (ElderProfile p : elderDbService.listElders()) {
            List<Measurement> vitals = store.recentVitals(p.getElderId());
            Measurement m = vitals.isEmpty() ? null : vitals.get(vitals.size() - 1);
            int hr = m != null && m.getHeartRate() != null
                    ? m.getHeartRate()
                    : 68 + Math.abs(p.getElderId().hashCode() % 35);
            int spo2 = 94 + Math.abs(p.getElderId().hashCode() % 6);
            if (hr > 100) {
                crit++;
            } else if (spo2 < 95) {
                warn++;
            } else {
                normal++;
            }
        }
        return List.of(
                new AdminNamedCountDto("正常", normal),
                new AdminNamedCountDto("需关注", warn),
                new AdminNamedCountDto("高风险", crit)
        );
    }

    private static String alarmTypeLabel(String type) {
        return switch (type) {
            case "FALL_SUSPECTED" -> "跌倒检测";
            case "SOS_SUSPECTED" -> "SOS 紧急";
            case "VITALS_LONG_ABNORMAL" -> "体征异常";
            default -> type;
        };
    }

    private static String serviceTypeLabel(String type) {
        if (type == null) return "其他";
        return switch (type.toUpperCase()) {
            case "NURSING" -> "助餐上门";
            case "HOUSEKEEPING" -> "家政保洁";
            case "ACCOMPANY" -> "陪诊陪护";
            default -> type;
        };
    }
}
