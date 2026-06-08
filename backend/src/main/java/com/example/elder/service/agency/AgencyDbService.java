package com.example.elder.service.agency;

import com.example.elder.domain.AgencyAnomalyReport;
import com.example.elder.domain.AgencyClockInRecord;
import com.example.elder.domain.ServiceWorker;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AgencyDbService {

    private final JdbcTemplate jdbcTemplate;

    public AgencyDbService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ServiceWorker> listWorkers(String keyword, String serviceType) {
        String kw = keyword == null ? "" : keyword.trim();
        String st = serviceType == null ? "" : serviceType.trim().toUpperCase();
        return jdbcTemplate.query(
                """
                SELECT id, name, position, phone, online_status, service_type, created_at
                FROM service_worker
                WHERE (? = '' OR name LIKE CONCAT('%', ?, '%') OR phone LIKE CONCAT('%', ?, '%') OR position LIKE CONCAT('%', ?, '%'))
                  AND (? = '' OR service_type = ?)
                ORDER BY created_at DESC
                """,
                (rs, rowNum) -> new ServiceWorker(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("position"),
                        rs.getString("phone"),
                        parseStatus(rs.getString("online_status")),
                        rs.getString("service_type"),
                        rs.getTimestamp("created_at").toInstant()
                ),
                kw, kw, kw, kw, st, st
        );
    }

    public void addWorker(ServiceWorker w) {
        jdbcTemplate.update(
                """
                INSERT INTO service_worker(id, name, position, phone, online_status, service_type, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                w.getId(), w.getName(), w.getPosition(), w.getPhone(),
                w.getOnlineStatus().name(), w.getServiceType(), Timestamp.from(w.getCreatedAt())
        );
    }

    public ServiceWorker getWorker(String workerId) {
        var rows = jdbcTemplate.query(
                """
                SELECT id, name, position, phone, online_status, service_type, created_at
                FROM service_worker
                WHERE id = ?
                LIMIT 1
                """,
                (rs, rowNum) -> new ServiceWorker(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("position"),
                        rs.getString("phone"),
                        parseStatus(rs.getString("online_status")),
                        rs.getString("service_type"),
                        rs.getTimestamp("created_at").toInstant()
                ),
                workerId
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    public void updateWorker(String workerId, ServiceWorker patch) {
        jdbcTemplate.update(
                """
                UPDATE service_worker
                SET name = ?, position = ?, phone = ?, online_status = ?, service_type = ?
                WHERE id = ?
                """,
                patch.getName(), patch.getPosition(), patch.getPhone(),
                patch.getOnlineStatus().name(), patch.getServiceType(), workerId
        );
    }

    public void deleteWorker(String workerId) {
        jdbcTemplate.update("DELETE FROM service_worker WHERE id = ?", workerId);
    }

    public long countWorkers() {
        Long n = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM service_worker", Long.class);
        return n == null ? 0L : n;
    }

    public long countOnlineWorkers() {
        Long n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM service_worker WHERE online_status = 'ONLINE'",
                Long.class
        );
        return n == null ? 0L : n;
    }

    public long countAgencies() {
        Long n = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM agency_profile", Long.class);
        return n == null ? 0L : n;
    }

    public List<AgencyAnomalyReport> listAnomalies() {
        return jdbcTemplate.query(
                """
                SELECT id, reporter_name, elder_user_id, elder_name, anomaly_type, description, reported_at, status
                FROM agency_anomaly_report
                ORDER BY reported_at DESC
                """,
                (rs, rowNum) -> new AgencyAnomalyReport(
                        rs.getString("id"),
                        rs.getString("reporter_name"),
                        rs.getString("elder_user_id"),
                        rs.getString("elder_name"),
                        rs.getString("anomaly_type"),
                        rs.getString("description"),
                        rs.getTimestamp("reported_at").toInstant(),
                        parseAnomalyStatus(rs.getString("status"))
                )
        );
    }

    public void processAnomaly(String id) {
        int n = jdbcTemplate.update(
                "UPDATE agency_anomaly_report SET status = 'PROCESSED' WHERE id = ?",
                id
        );
        if (n <= 0) throw new IllegalArgumentException("anomaly_not_found");
    }

    public List<AgencyClockInRecord> listClockIns() {
        return jdbcTemplate.query(
                """
                SELECT id, worker_name, elder_name, service_type_label, clock_at, address, status_label
                FROM agency_clock_in_record
                ORDER BY clock_at DESC
                """,
                (rs, rowNum) -> new AgencyClockInRecord(
                        rs.getString("id"),
                        rs.getString("worker_name"),
                        rs.getString("elder_name"),
                        rs.getString("service_type_label"),
                        rs.getTimestamp("clock_at").toInstant(),
                        rs.getString("address"),
                        rs.getString("status_label")
                )
        );
    }

    private ServiceWorker.OnlineStatus parseStatus(String s) {
        return "OFFLINE".equalsIgnoreCase(s) ? ServiceWorker.OnlineStatus.OFFLINE : ServiceWorker.OnlineStatus.ONLINE;
    }

    private AgencyAnomalyReport.Status parseAnomalyStatus(String s) {
        return "PROCESSED".equalsIgnoreCase(s) ? AgencyAnomalyReport.Status.PROCESSED : AgencyAnomalyReport.Status.PENDING;
    }
}

