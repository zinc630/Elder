package com.example.elder.service.alarm;

import com.example.elder.domain.AlarmEvent;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlarmDbService {

    private final JdbcTemplate jdbcTemplate;

    public AlarmDbService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AlarmEvent insert(AlarmEvent event) {
        String elderId = event.getElderId().trim().toUpperCase();
        jdbcTemplate.update(
                """
                INSERT INTO alarm_event(
                  id, elder_user_id, alarm_type, status, triggered_at,
                  window_start_at, window_end_at, risk_score,
                  abnormal_point_count, abnormal_point_threshold, confirmation_source
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                event.getId(),
                elderId,
                event.getType().name(),
                event.getStatus().name(),
                Timestamp.from(event.getTriggeredAt()),
                event.getWindowStartAt() == null ? null : Timestamp.from(event.getWindowStartAt()),
                event.getWindowEndAt() == null ? null : Timestamp.from(event.getWindowEndAt()),
                event.getRiskScore(),
                event.getAbnormalPointCount(),
                event.getAbnormalPointThreshold(),
                blankToNull(event.getConfirmationSource())
        );
        return event;
    }

    public Optional<AlarmEvent> findById(String alarmId) {
        var rows = jdbcTemplate.query(
                selectSql() + " WHERE id = ?",
                (rs, rowNum) -> mapRow(rs),
                alarmId
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public List<AlarmEvent> listByElder(String elderId, int limit) {
        return jdbcTemplate.query(
                selectSql() + " WHERE elder_user_id = ? ORDER BY triggered_at DESC LIMIT ?",
                (rs, rowNum) -> mapRow(rs),
                elderId.trim().toUpperCase(),
                limit
        );
    }

    public List<AlarmEvent> listRecent(int limit) {
        return jdbcTemplate.query(
                selectSql() + " ORDER BY triggered_at DESC LIMIT ?",
                (rs, rowNum) -> mapRow(rs),
                limit
        );
    }

    public List<AlarmEvent> listByStatuses(Collection<AlarmEvent.Status> statuses, int limit) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", statuses.stream().map(s -> "?").toList());
        List<Object> args = new ArrayList<>();
        for (AlarmEvent.Status st : statuses) {
            args.add(st.name());
        }
        args.add(limit);
        return jdbcTemplate.query(
                selectSql() + " WHERE status IN (" + placeholders + ") ORDER BY triggered_at DESC LIMIT ?",
                (rs, rowNum) -> mapRow(rs),
                args.toArray()
        );
    }

    public long countBetween(Instant startInclusive, Instant endExclusive) {
        Long n = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM alarm_event
                WHERE triggered_at >= ? AND triggered_at < ?
                """,
                Long.class,
                Timestamp.from(startInclusive),
                Timestamp.from(endExclusive)
        );
        return n == null ? 0L : n;
    }

    public long countPending() {
        Long n = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM alarm_event
                WHERE status IN ('PENDING_CONFIRM', 'CHILD_NOTIFIED', 'CALL_PENDING')
                """,
                Long.class
        );
        return n == null ? 0L : n;
    }

    public List<java.util.Map<String, Object>> countGroupByTypeSince(Instant sinceInclusive) {
        return jdbcTemplate.queryForList(
                """
                SELECT alarm_type, COUNT(*) AS cnt
                FROM alarm_event
                WHERE triggered_at >= ?
                GROUP BY alarm_type
                ORDER BY cnt DESC
                """,
                Timestamp.from(sinceInclusive)
        );
    }

    public long countByTypeBetween(Instant startInclusive, Instant endExclusive, String alarmType) {
        Long n = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM alarm_event
                WHERE triggered_at >= ? AND triggered_at < ? AND alarm_type = ?
                """,
                Long.class,
                Timestamp.from(startInclusive),
                Timestamp.from(endExclusive),
                alarmType
        );
        return n == null ? 0L : n;
    }

    public List<Integer> countsLastDays(int days, ZoneId zone) {
        List<Integer> res = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now(zone).minusDays(i);
            Instant start = d.atStartOfDay(zone).toInstant();
            Instant end = d.plusDays(1).atStartOfDay(zone).toInstant();
            res.add((int) countBetween(start, end));
        }
        return res;
    }

    public void confirm(String alarmId, String confirmationSource) {
        jdbcTemplate.update(
                """
                UPDATE alarm_event
                SET status = ?, confirmation_source = ?, updated_at = ?
                WHERE id = ?
                """,
                AlarmEvent.Status.CONFIRMED.name(),
                blankToNull(confirmationSource),
                Timestamp.from(Instant.now()),
                alarmId
        );
    }

    public void updateStatus(String alarmId, AlarmEvent.Status status) {
        jdbcTemplate.update(
                "UPDATE alarm_event SET status = ?, updated_at = ? WHERE id = ?",
                status.name(),
                Timestamp.from(Instant.now()),
                alarmId
        );
    }

    private static String selectSql() {
        return """
                SELECT id, elder_user_id, alarm_type, status, triggered_at,
                       window_start_at, window_end_at, risk_score,
                       abnormal_point_count, abnormal_point_threshold, confirmation_source
                FROM alarm_event
                """;
    }

    private static AlarmEvent mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp triggered = rs.getTimestamp("triggered_at");
        Timestamp ws = rs.getTimestamp("window_start_at");
        Timestamp we = rs.getTimestamp("window_end_at");
        Double risk = rs.getObject("risk_score") == null ? 0.0 : rs.getDouble("risk_score");
        return new AlarmEvent(
                rs.getString("id"),
                rs.getString("elder_user_id"),
                AlarmEvent.Type.valueOf(rs.getString("alarm_type")),
                triggered == null ? Instant.now() : triggered.toInstant(),
                AlarmEvent.Status.valueOf(rs.getString("status")),
                ws == null ? null : ws.toInstant(),
                we == null ? null : we.toInstant(),
                risk,
                (Integer) rs.getObject("abnormal_point_count"),
                (Integer) rs.getObject("abnormal_point_threshold"),
                rs.getString("confirmation_source")
        );
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }
}
