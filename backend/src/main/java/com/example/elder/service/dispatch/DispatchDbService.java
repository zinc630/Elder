package com.example.elder.service.dispatch;

import com.example.elder.domain.DispatchTask;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DispatchDbService {

    private final JdbcTemplate jdbcTemplate;

    public DispatchDbService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DispatchTask insert(DispatchTask task) {
        jdbcTemplate.update(
                """
                INSERT INTO dispatch_task(
                  id, elder_user_id, alarm_event_id, service_type, appointment_time, notes,
                  assigned_worker_id, status, booked_by_role, booked_by_user_id, booked_by_name,
                  created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                task.getId(),
                task.getElderId().trim().toUpperCase(),
                blankToNull(task.getAlarmEventId()),
                task.getServiceType(),
                Timestamp.from(task.getAppointmentTime()),
                blankToNull(task.getNotes()),
                blankToNull(task.getAssignedWorkerId()),
                task.getStatus().name(),
                blankToNull(task.getBookedByRole()),
                blankToNull(task.getBookedByUserId()),
                blankToNull(task.getBookedByName()),
                Timestamp.from(task.getCreatedAt()),
                Timestamp.from(task.getUpdatedAt())
        );
        return task;
    }

    public Optional<DispatchTask> findById(String taskId) {
        var rows = jdbcTemplate.query(
                """
                SELECT id, elder_user_id, alarm_event_id, service_type, appointment_time, notes,
                       assigned_worker_id, status, booked_by_role, booked_by_user_id, booked_by_name,
                       created_at, updated_at
                FROM dispatch_task WHERE id = ?
                """,
                (rs, rowNum) -> mapRow(rs),
                taskId
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public List<DispatchTask> listRecent(int limit) {
        return jdbcTemplate.query(
                """
                SELECT id, elder_user_id, alarm_event_id, service_type, appointment_time, notes,
                       assigned_worker_id, status, booked_by_role, booked_by_user_id, booked_by_name,
                       created_at, updated_at
                FROM dispatch_task
                ORDER BY created_at DESC
                LIMIT ?
                """,
                (rs, rowNum) -> mapRow(rs),
                limit
        );
    }

    public List<DispatchTask> listByStatus(DispatchTask.Status status) {
        return jdbcTemplate.query(
                """
                SELECT id, elder_user_id, alarm_event_id, service_type, appointment_time, notes,
                       assigned_worker_id, status, booked_by_role, booked_by_user_id, booked_by_name,
                       created_at, updated_at
                FROM dispatch_task
                WHERE status = ?
                ORDER BY created_at DESC
                """,
                (rs, rowNum) -> mapRow(rs),
                status.name()
        );
    }

    public List<Integer> countsCreatedLastDays(int days, ZoneId zone) {
        List<Integer> res = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now(zone).minusDays(i);
            Instant start = d.atStartOfDay(zone).toInstant();
            Instant end = d.plusDays(1).atStartOfDay(zone).toInstant();
            res.add((int) countCreatedBetween(start, end));
        }
        return res;
    }

    public List<java.util.Map<String, Object>> countGroupByServiceType() {
        return jdbcTemplate.queryForList(
                """
                SELECT service_type, COUNT(*) AS cnt
                FROM dispatch_task
                WHERE status <> 'CANCELLED'
                GROUP BY service_type
                ORDER BY cnt DESC
                """
        );
    }

    public long countCreatedBetween(Instant startInclusive, Instant endExclusive) {
        Long n = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM dispatch_task
                WHERE created_at >= ? AND created_at < ?
                """,
                Long.class,
                Timestamp.from(startInclusive),
                Timestamp.from(endExclusive)
        );
        return n == null ? 0L : n;
    }

    public long countCreatedInMonth(int year, int month, ZoneId zone) {
        LocalDate first = LocalDate.of(year, month, 1);
        Instant start = first.atStartOfDay(zone).toInstant();
        Instant end = first.plusMonths(1).atStartOfDay(zone).toInstant();
        return countCreatedBetween(start, end);
    }

    public long countPending() {
        Long n = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM dispatch_task
                WHERE status IN ('NEW', 'ASSIGNED', 'ARRIVING', 'IN_PROGRESS')
                """,
                Long.class
        );
        return n == null ? 0L : n;
    }

    /** 本月创建且已完成的工单（与完成率分母同一统计口径） */
    public long countCompletedCreatedBetween(Instant startInclusive, Instant endExclusive) {
        Long n = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM dispatch_task
                WHERE status = 'COMPLETED'
                  AND created_at >= ? AND created_at < ?
                """,
                Long.class,
                Timestamp.from(startInclusive),
                Timestamp.from(endExclusive)
        );
        return n == null ? 0L : n;
    }

    public long countCompletedCreatedInMonth(int year, int month, ZoneId zone) {
        LocalDate first = LocalDate.of(year, month, 1);
        Instant start = first.atStartOfDay(zone).toInstant();
        Instant end = first.plusMonths(1).atStartOfDay(zone).toInstant();
        return countCompletedCreatedBetween(start, end);
    }

    public long countDistinctEldersServed() {
        Long n = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT elder_user_id) FROM dispatch_task",
                Long.class
        );
        return n == null ? 0L : n;
    }

    public long countByStatus(DispatchTask.Status status) {
        Long n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dispatch_task WHERE status = ?",
                Long.class,
                status.name()
        );
        return n == null ? 0L : n;
    }

    /** 有效工单总数（不含已取消） */
    public long countActiveOrders() {
        Long n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dispatch_task WHERE status <> 'CANCELLED'",
                Long.class
        );
        return n == null ? 0L : n;
    }

    public List<DispatchTask> listByElder(String elderId, int limit) {
        return jdbcTemplate.query(
                """
                SELECT id, elder_user_id, alarm_event_id, service_type, appointment_time, notes,
                       assigned_worker_id, status, booked_by_role, booked_by_user_id, booked_by_name,
                       created_at, updated_at
                FROM dispatch_task
                WHERE elder_user_id = ?
                ORDER BY created_at DESC
                LIMIT ?
                """,
                (rs, rowNum) -> mapRow(rs),
                elderId.trim().toUpperCase(),
                limit
        );
    }

    public void updateStatus(String taskId, DispatchTask.Status status, Instant updatedAt) {
        jdbcTemplate.update(
                "UPDATE dispatch_task SET status = ?, updated_at = ? WHERE id = ?",
                status.name(),
                Timestamp.from(updatedAt),
                taskId
        );
    }

    public void assignWorker(String taskId, String workerId, DispatchTask.Status status, Instant updatedAt) {
        jdbcTemplate.update(
                """
                UPDATE dispatch_task
                SET assigned_worker_id = ?, status = ?, updated_at = ?
                WHERE id = ?
                """,
                workerId,
                status.name(),
                Timestamp.from(updatedAt),
                taskId
        );
    }

    private static DispatchTask mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp appt = rs.getTimestamp("appointment_time");
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        return new DispatchTask(
                rs.getString("id"),
                rs.getString("elder_user_id"),
                rs.getString("alarm_event_id") == null ? "" : rs.getString("alarm_event_id"),
                rs.getString("service_type"),
                appt == null ? Instant.now() : appt.toInstant(),
                rs.getString("notes") == null ? "" : rs.getString("notes"),
                rs.getString("assigned_worker_id"),
                DispatchTask.Status.valueOf(rs.getString("status")),
                created == null ? Instant.now() : created.toInstant(),
                updated == null ? Instant.now() : updated.toInstant(),
                rs.getString("booked_by_role") == null ? "" : rs.getString("booked_by_role"),
                rs.getString("booked_by_user_id") == null ? "" : rs.getString("booked_by_user_id"),
                rs.getString("booked_by_name") == null ? "" : rs.getString("booked_by_name")
        );
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }
}
