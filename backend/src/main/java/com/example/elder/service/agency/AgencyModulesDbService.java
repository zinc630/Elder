package com.example.elder.service.agency;

import com.example.elder.domain.AlarmEvent;
import com.example.elder.domain.portal.PortalActivity;
import com.example.elder.dto.*;
import com.example.elder.store.PortalContentStore;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AgencyModulesDbService {

    private final JdbcTemplate jdbcTemplate;
    private final AgencyDbService agencyDbService;
    private final com.example.elder.service.alarm.AlarmDbService alarmDbService;
    private final PortalContentStore portalContentStore;

    public AgencyModulesDbService(
            JdbcTemplate jdbcTemplate,
            AgencyDbService agencyDbService,
            com.example.elder.service.alarm.AlarmDbService alarmDbService,
            PortalContentStore portalContentStore
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.agencyDbService = agencyDbService;
        this.alarmDbService = alarmDbService;
        this.portalContentStore = portalContentStore;
    }

    public void seedDemoDataIfEmpty() {
        if (count("agency_worker_schedule") == 0) {
            insertSchedule("S001", "周一", 9, "王阿姨", "morning");
            insertSchedule("S002", "周二", 14, "李师傅", "afternoon");
        }
        if (count("agency_service_evaluation") == 0) {
            seedDemoEvaluations();
        }
        migrateLegacyAgencyActivitiesToPortal();
        if (count("agency_finance_record") == 0) {
            insertFinance("F001", "张大爷", "NURSING", new BigDecimal("25.00"), "paid", parseIso("2026-05-18T12:00:00"));
            insertFinance("F002", "李奶奶", "HOUSEKEEPING", new BigDecimal("120.00"), "pending", null);
        }
        if (count("agency_notification") == 0) {
            insertNotification("N001", "系统升级通知", "系统将于5月20日凌晨2:00-5:00进行升级维护。",
                    "published", "管理员", 156, parseIso("2026-05-18T10:00:00"));
        }
        if (count("agency_care_device") == 0) {
            insertDevice("D001", "智能手环 - 客厅", "智能手环", null, "张大爷", 85, "online", Instant.now());
            insertDevice("D002", "跌倒检测器", "跌倒检测器", null, null, 23, "low", Instant.now());
        }
    }

    private long count(String table) {
        Long n = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
        return n == null ? 0L : n;
    }

    // ---------- Schedules ----------

    public List<AgencyScheduleDto> listSchedules() {
        return jdbcTemplate.query(
                """
                SELECT id, day_label, hour_slot, worker_name, shift_type
                FROM agency_worker_schedule
                ORDER BY day_label, hour_slot
                """,
                (rs, i) -> new AgencyScheduleDto(
                        rs.getString("id"),
                        rs.getString("day_label"),
                        rs.getInt("hour_slot"),
                        rs.getString("worker_name"),
                        rs.getString("shift_type")
                )
        );
    }

    public String createSchedule(String day, int hour, String workerName, String shiftType) {
        String id = "SCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        insertSchedule(id, day, hour, workerName, shiftType);
        return id;
    }

    public void deleteSchedule(String id) {
        jdbcTemplate.update("DELETE FROM agency_worker_schedule WHERE id = ?", id);
    }

    private void insertSchedule(String id, String day, int hour, String workerName, String shiftType) {
        jdbcTemplate.update(
                """
                INSERT INTO agency_worker_schedule(id, day_label, hour_slot, worker_name, shift_type)
                VALUES (?, ?, ?, ?, ?)
                """,
                id, day, hour, workerName, shiftType
        );
    }

    // ---------- Evaluations ----------

    public List<AgencyEvaluationDto> listEvaluations() {
        return jdbcTemplate.query(
                """
                SELECT id, elder_name, worker_name, service_type, rating, 
                       attitude_rating, skill_rating, response_rating, 
                       punctuality_rating, communication_rating,
                       comment_text, tags, is_anonymous, 
                       service_duration_minutes, task_id, images,
                       created_at
                FROM agency_service_evaluation
                ORDER BY created_at DESC
                """,
                (rs, i) -> new AgencyEvaluationDto(
                        rs.getString("id"),
                        rs.getString("elder_name"),
                        rs.getString("worker_name"),
                        rs.getString("service_type"),
                        rs.getInt("rating"),
                        (Integer) rs.getObject("attitude_rating"),
                        (Integer) rs.getObject("skill_rating"),
                        (Integer) rs.getObject("response_rating"),
                        (Integer) rs.getObject("punctuality_rating"),
                        (Integer) rs.getObject("communication_rating"),
                        rs.getString("comment_text"),
                        parseTags(rs.getString("tags")),
                        readAnonymous(rs),
                        (Integer) rs.getObject("service_duration_minutes"),
                        rs.getString("task_id"),
                        null,
                        parseImages(rs.getString("images")),
                        rs.getTimestamp("created_at").toInstant()
                )
        );
    }

    private List<String> parseTags(String tags) {
        if (tags == null || tags.isEmpty()) return List.of();
        return List.of(tags.split(","));
    }

    private List<String> parseImages(String images) {
        if (images == null || images.isEmpty()) return List.of();
        return List.of(images.split(","));
    }

    private boolean readAnonymous(java.sql.ResultSet rs) throws java.sql.SQLException {
        Object v = rs.getObject("is_anonymous");
        if (v == null) {
            return false;
        }
        if (v instanceof Boolean b) {
            return b;
        }
        return ((Number) v).intValue() == 1;
    }

    private void seedDemoEvaluations() {
        insertEvaluationDetailed(
                "E001", null, "张大爷", "王阿姨", "NURSING", 5,
                5, 5, 5, 5, 5,
                "送餐准时，菜品温热可口，王阿姨还会主动询问老人饮食禁忌，服务非常周到，老人很满意。",
                "细心负责,热情周到,准时到达",
                false, 45, "TASK-DEMO-001", null
        );
        insertEvaluationDetailed(
                "E002", null, "李奶奶", "李师傅", "HOUSEKEEPING", 4,
                4, 5, 4, 4, 4,
                "全屋清洁较彻底，厨房油污处理到位，希望下次能提前沟通重点清洁区域。",
                "清洁彻底,专业技能好",
                false, 90, "TASK-DEMO-002", null
        );
        insertEvaluationDetailed(
                "E003", null, "王爷爷", "张护士", "ACCOMPANY", 3,
                3, 4, 3, 2, 4,
                "陪诊过程基本顺利，但候诊时间较长，到达略晚，沟通态度较好。",
                "态度友好,等待偏久",
                false, 180, "TASK-DEMO-003", null
        );
        insertEvaluationDetailed(
                "E004", null, "匿名用户", "赵师傅", "BATH", 2,
                2, 3, 2, 2, 2,
                "助浴设备准备不足，服务时间偏短，希望加强专业技能培训。",
                "设备不足,响应偏慢",
                true, 35, "TASK-DEMO-004", null
        );
    }

    private void insertEvaluationDetailed(
            String id,
            String elderUserId,
            String elderName,
            String workerName,
            String serviceType,
            int rating,
            Integer attitudeRating,
            Integer skillRating,
            Integer responseRating,
            Integer punctualityRating,
            Integer communicationRating,
            String comment,
            String tags,
            boolean anonymous,
            Integer serviceDurationMinutes,
            String taskId,
            String images
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO agency_service_evaluation(id, elder_user_id, elder_name, worker_name,
                    service_type, rating, comment_text, attitude_rating, skill_rating,
                    response_rating, punctuality_rating, communication_rating,
                    tags, is_anonymous, service_duration_minutes, task_id, images)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id, elderUserId, elderName, workerName, serviceType, rating, comment,
                attitudeRating, skillRating, responseRating, punctualityRating, communicationRating,
                tags, anonymous ? 1 : 0, serviceDurationMinutes, taskId, images
        );
    }

    // ---------- Activities（与门户 portal_activity / 首页文娱活动共用） ----------

    public List<AgencyActivityDto> listActivities() {
        return portalContentStore.listActivities().stream()
                .map(AgencyPortalActivityMapper::toAgencyDto)
                .sorted((a, b) -> b.startTime().compareTo(a.startTime()))
                .toList();
    }

    public String createActivity(
            String title,
            Instant start,
            Instant end,
            String location,
            int maxParticipants,
            String description,
            String tag,
            String icon
    ) {
        String id = portalContentStore.newActivityId();
        String status = AgencyPortalActivityMapper.deriveStatus(start, end, true);
        PortalActivity created = AgencyPortalActivityMapper.toPortalActivity(
                id, title, start, end, location, maxParticipants, 0, status, description, tag, icon, true);
        portalContentStore.saveActivity(created);
        return id;
    }

    public void incrementActivityRegistration(String id) {
        portalContentStore.incrementActivityEnrolled(id);
    }

    /** 将旧版 agency_activity 表数据一次性迁入 portal_activity */
    public void migrateLegacyAgencyActivitiesToPortal() {
        try {
            jdbcTemplate.query(
                    """
                    SELECT id, title, start_time, end_time, location, max_participants,
                           registered_count, status, description
                    FROM agency_activity
                    """,
                    (rs, i) -> {
                        String id = rs.getString("id");
                        if (portalContentStore.getActivity(id).isPresent()) {
                            return null;
                        }
                        Instant start = rs.getTimestamp("start_time").toInstant();
                        Instant end = rs.getTimestamp("end_time").toInstant();
                        String status = rs.getString("status");
                        PortalActivity migrated = AgencyPortalActivityMapper.toPortalActivity(
                                id,
                                rs.getString("title"),
                                start,
                                end,
                                rs.getString("location"),
                                rs.getInt("max_participants"),
                                rs.getInt("registered_count"),
                                status,
                                rs.getString("description"),
                                "文娱活动",
                                "🎨",
                                !"ended".equals(status));
                        portalContentStore.saveActivity(migrated);
                        return null;
                    });
        } catch (Exception ignored) {
            // 旧表不存在时忽略
        }
    }

    // ---------- Finance ----------

    public List<AgencyFinanceDto> listFinance() {
        return jdbcTemplate.query(
                """
                SELECT id, elder_name, service_type, amount, status, paid_at
                FROM agency_finance_record
                ORDER BY created_at DESC
                """,
                (rs, i) -> new AgencyFinanceDto(
                        rs.getString("id"),
                        rs.getString("elder_name"),
                        rs.getString("service_type"),
                        rs.getBigDecimal("amount"),
                        rs.getString("status"),
                        rs.getTimestamp("paid_at") == null ? null : rs.getTimestamp("paid_at").toInstant()
                )
        );
    }

    public String createFinance(String elderName, String serviceType, BigDecimal amount, String status) {
        String id = "FIN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Instant paidAt = "paid".equals(status) ? Instant.now() : null;
        jdbcTemplate.update(
                """
                INSERT INTO agency_finance_record(id, elder_name, service_type, amount, status, paid_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                id, elderName, serviceType, amount, status, paidAt == null ? null : Timestamp.from(paidAt)
        );
        return id;
    }

    public void markFinancePaid(String id) {
        jdbcTemplate.update(
                "UPDATE agency_finance_record SET status = 'paid', paid_at = ? WHERE id = ?",
                Timestamp.from(Instant.now()), id
        );
    }

    private void insertFinance(String id, String elderName, String serviceType, BigDecimal amount,
                               String status, Instant paidAt) {
        jdbcTemplate.update(
                """
                INSERT INTO agency_finance_record(id, elder_name, service_type, amount, status, paid_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                id, elderName, serviceType, amount, status, paidAt == null ? null : Timestamp.from(paidAt)
        );
    }

    // ---------- Notifications ----------

    public List<AgencyNotificationDto> listNotifications() {
        return jdbcTemplate.query(
                """
                SELECT id, title, content, status, created_at, author, view_count
                FROM agency_notification
                ORDER BY created_at DESC
                """,
                (rs, i) -> new AgencyNotificationDto(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at").toInstant(),
                        rs.getString("author"),
                        rs.getInt("view_count")
                )
        );
    }

    public String createNotification(String title, String content, String status, String author) {
        String id = "NTF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        insertNotification(id, title, content, status, author, 0, Instant.now());
        return id;
    }

    public void updateNotificationStatus(String id, String status) {
        jdbcTemplate.update("UPDATE agency_notification SET status = ? WHERE id = ?", status, id);
    }

    public void patchNotification(String id, String title, String content, String status) {
        jdbcTemplate.update(
                """
                UPDATE agency_notification
                SET title = COALESCE(?, title),
                    content = COALESCE(?, content),
                    status = COALESCE(?, status)
                WHERE id = ?
                """,
                blankToNull(title), blankToNull(content), blankToNull(status), id
        );
    }

    public void patchDeviceElder(String id, String elderName) {
        jdbcTemplate.update(
                "UPDATE agency_care_device SET elder_name = ? WHERE id = ?",
                elderName, id
        );
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) return null;
        return s.trim();
    }

    public void deleteNotification(String id) {
        jdbcTemplate.update("DELETE FROM agency_notification WHERE id = ?", id);
    }

    private void insertNotification(String id, String title, String content, String status,
                                    String author, int views, Instant createdAt) {
        jdbcTemplate.update(
                """
                INSERT INTO agency_notification(id, title, content, status, author, view_count, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                id, title, content, status, author, views, Timestamp.from(createdAt)
        );
    }

    // ---------- Devices ----------

    public List<AgencyDeviceDto> listDevices() {
        return jdbcTemplate.query(
                """
                SELECT id, device_name, device_type, elder_name, battery_percent, status, last_online_at
                FROM agency_care_device
                ORDER BY created_at DESC
                """,
                (rs, i) -> new AgencyDeviceDto(
                        rs.getString("id"),
                        rs.getString("device_name"),
                        rs.getString("device_type"),
                        rs.getString("elder_name"),
                        rs.getInt("battery_percent"),
                        rs.getString("status"),
                        rs.getTimestamp("last_online_at") == null ? null : rs.getTimestamp("last_online_at").toInstant()
                )
        );
    }

    public String createDevice(String name, String type, String elderName, int battery, String status) {
        String id = "DEV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        insertDevice(id, name, type, null, elderName, battery, status, Instant.now());
        return id;
    }

    public void deleteDevice(String id) {
        jdbcTemplate.update("DELETE FROM agency_care_device WHERE id = ?", id);
    }

    private void insertDevice(String id, String name, String type, String elderUserId, String elderName,
                              int battery, String status, Instant lastOnline) {
        jdbcTemplate.update(
                """
                INSERT INTO agency_care_device(id, device_name, device_type, elder_user_id, elder_name, battery_percent, status, last_online_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id, name, type, elderUserId, elderName, battery, status,
                lastOnline == null ? null : Timestamp.from(lastOnline)
        );
    }

    // ---------- Health ----------

    public List<AgencyHealthRecordDto> listHealthRecords() {
        return jdbcTemplate.query(
                """
                SELECT u.user_id, u.user_name, u.age, u.gender,
                       v.heart_rate, v.systolic_bp, v.diastolic_bp, v.blood_oxygen, v.recorded_at
                FROM sys_user u
                LEFT JOIN elder_vitals_snapshot v ON v.elder_user_id = u.user_id
                WHERE u.role_code = 'ELDER' AND u.status = 1
                ORDER BY u.user_id
                """,
                (rs, i) -> {
                    Integer hr = (Integer) rs.getObject("heart_rate");
                    Integer sbp = (Integer) rs.getObject("systolic_bp");
                    Integer dbp = (Integer) rs.getObject("diastolic_bp");
                    Integer spo2 = (Integer) rs.getObject("blood_oxygen");
                    String bp = sbp != null && dbp != null ? sbp + "/" + dbp : "—";
                    String health = deriveHealthStatus(hr, spo2, sbp);
                    Timestamp recorded = rs.getTimestamp("recorded_at");
                    return new AgencyHealthRecordDto(
                            rs.getString("user_id"),
                            rs.getString("user_name"),
                            (Integer) rs.getObject("age"),
                            rs.getString("gender") == null ? "—" : rs.getString("gender"),
                            health,
                            hr,
                            bp,
                            spo2,
                            recorded == null ? null : recorded.toInstant()
                    );
                }
        );
    }

    public void upsertVitalsSnapshot(String elderUserId, Integer heartRate, Integer systolic,
                                     Integer diastolic, Integer bloodOxygen) {
        String id = elderUserId.trim().toUpperCase();
        jdbcTemplate.update(
                """
                INSERT INTO elder_vitals_snapshot(elder_user_id, heart_rate, systolic_bp, diastolic_bp, blood_oxygen, recorded_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                  heart_rate = VALUES(heart_rate),
                  systolic_bp = VALUES(systolic_bp),
                  diastolic_bp = VALUES(diastolic_bp),
                  blood_oxygen = COALESCE(VALUES(blood_oxygen), blood_oxygen),
                  recorded_at = VALUES(recorded_at)
                """,
                id, heartRate, systolic, diastolic, bloodOxygen, Timestamp.from(Instant.now())
        );
    }

    private static String deriveHealthStatus(Integer hr, Integer spo2, Integer sbp) {
        if (hr != null && (hr > 100 || hr < 60)) return "需关注";
        if (spo2 != null && spo2 < 95) return "需关注";
        if (sbp != null && sbp >= 140) return "异常";
        return "正常";
    }

    // ---------- Alerts (alarm_event + anomaly) ----------

    public List<AgencyAlertDto> listAlerts() {
        List<AgencyAlertDto> merged = new ArrayList<>();
        merged.addAll(listAlertsFromAlarms());
        merged.addAll(listAlertsFromAnomalies());
        merged.sort(Comparator.comparing(AgencyAlertDto::triggeredAt).reversed());
        return merged;
    }

    private List<AgencyAlertDto> listAlertsFromAlarms() {
        return jdbcTemplate.query(
                """
                SELECT a.id, COALESCE(u.user_name, a.elder_user_id) AS elder_name,
                       a.alarm_type, a.status, a.triggered_at, a.confirmation_source
                FROM alarm_event a
                LEFT JOIN sys_user u ON u.user_id = a.elder_user_id
                ORDER BY a.triggered_at DESC
                LIMIT 200
                """,
                (rs, i) -> {
                    String alarmType = rs.getString("alarm_type");
                    String uiType = mapAlarmType(alarmType);
                    String uiStatus = mapAlarmStatus(rs.getString("status"));
                    String log = rs.getString("confirmation_source");
                    if (log == null || log.isBlank()) {
                        log = buildAlarmProcessLog(alarmType);
                    }
                    return new AgencyAlertDto(
                            rs.getString("id"),
                            rs.getString("elder_name"),
                            uiType,
                            rs.getTimestamp("triggered_at").toInstant(),
                            uiStatus,
                            log,
                            "ALARM"
                    );
                }
        );
    }

    private List<AgencyAlertDto> listAlertsFromAnomalies() {
        return agencyDbService.listAnomalies().stream()
                .map(a -> new AgencyAlertDto(
                        a.getId(),
                        a.getElderName() == null || a.getElderName().isBlank() ? "—" : a.getElderName(),
                        mapAnomalyType(a.getAnomalyType()),
                        a.getReportedAt(),
                        a.getStatus().name().equals("PENDING") ? "PENDING" : "RESOLVED",
                        a.getDescription() == null ? "" : a.getDescription(),
                        "ANOMALY"
                ))
                .toList();
    }

    public void processAlert(String id, String source) {
        if ("ALARM".equalsIgnoreCase(source)) {
            alarmDbService.updateStatus(id, AlarmEvent.Status.CLOSED);
            return;
        }
        agencyDbService.processAnomaly(id);
    }

    private static String buildAlarmProcessLog(String alarmType) {
        if ("SOS_SUSPECTED".equals(alarmType)) {
            return "老人端一键呼救，已同步子女端与机构，请立即响应";
        }
        if ("FALL_SUSPECTED".equals(alarmType)) {
            return "疑似跌倒告警，请核实老人安全";
        }
        if ("VITALS_LONG_ABNORMAL".equals(alarmType)) {
            return "体征持续异常，请关注";
        }
        return alarmType == null ? "" : alarmType;
    }

    private static String mapAlarmType(String dbType) {
        if (dbType == null) return "VITALS";
        return switch (dbType) {
            case "FALL_SUSPECTED" -> "FALL";
            case "SOS_SUSPECTED" -> "SOS";
            case "VITALS_LONG_ABNORMAL" -> "VITALS";
            default -> "VITALS";
        };
    }

    private static String mapAlarmStatus(String dbStatus) {
        if (dbStatus == null) return "PENDING";
        return switch (dbStatus) {
            case "CLOSED", "CONFIRMED" -> "RESOLVED";
            case "CALL_PENDING" -> "PROCESSING";
            default -> "PENDING";
        };
    }

    private static String mapAnomalyType(String t) {
        if (t == null) return "VITALS";
        String u = t.toUpperCase();
        if (u.contains("跌倒") || u.contains("FALL")) return "FALL";
        if (u.contains("SOS") || u.contains("求助")) return "SOS";
        if (u.contains("体征") || u.contains("VITAL")) return "VITALS";
        if (u.contains("电")) return "BATTERY";
        return "VITALS";
    }

    private static Instant parseIso(String iso) {
        if (iso.endsWith("Z")) {
            return Instant.parse(iso);
        }
        return LocalDateTime.parse(iso).atZone(ZoneId.systemDefault()).toInstant();
    }
}
