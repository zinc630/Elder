package com.example.elder.service.elder;

import com.example.elder.domain.ElderGeofence;
import com.example.elder.domain.ElderLastLocation;
import com.example.elder.domain.ElderProfile;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElderDbService {

    private final JdbcTemplate jdbcTemplate;

    public ElderDbService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<ElderProfile> getElderProfile(String elderId) {
        var rows = jdbcTemplate.query(
                """
                SELECT u.user_id, u.user_name, u.age, u.gender, u.address, ep.key_health_notes
                FROM sys_user u
                LEFT JOIN elder_profile ep ON ep.elder_user_id = u.user_id
                WHERE u.user_id = ? AND u.role_code = 'ELDER'
                LIMIT 1
                """,
                (rs, rowNum) -> new ElderProfile(
                        rs.getString("user_id"),
                        rs.getString("user_name"),
                        (Integer) rs.getObject("age"),
                        rs.getString("gender"),
                        rs.getString("address"),
                        rs.getString("key_health_notes")
                ),
                elderId.trim().toUpperCase()
        );
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(rows.get(0));
    }

    public List<ElderProfile> listElders() {
        return jdbcTemplate.query(
                """
                SELECT u.user_id, u.user_name, u.age, u.gender, u.address, ep.key_health_notes
                FROM sys_user u
                LEFT JOIN elder_profile ep ON ep.elder_user_id = u.user_id
                WHERE u.role_code = 'ELDER'
                ORDER BY u.id ASC
                """,
                (rs, rowNum) -> new ElderProfile(
                        rs.getString("user_id"),
                        rs.getString("user_name"),
                        (Integer) rs.getObject("age"),
                        rs.getString("gender"),
                        rs.getString("address"),
                        rs.getString("key_health_notes")
                )
        );
    }

    public ElderProfile upsertElderProfile(ElderProfile p) {
        String elderId = p.getElderId().trim().toUpperCase();
        assertElderExists(elderId);
        jdbcTemplate.update(
                """
                UPDATE sys_user
                SET user_name = ?, age = ?, gender = ?, address = ?
                WHERE user_id = ?
                """,
                p.getName(), p.getAge(), blankToNull(p.getGender()), blankToNull(p.getAddress()), elderId
        );
        jdbcTemplate.update(
                """
                INSERT INTO elder_profile(elder_user_id, key_health_notes)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE key_health_notes = VALUES(key_health_notes)
                """,
                elderId, blankToNull(p.getKeyHealthNotes())
        );
        return new ElderProfile(elderId, p.getName(), p.getAge(), p.getGender(), p.getAddress(), p.getKeyHealthNotes());
    }

    public Optional<ElderGeofence> getGeofence(String elderId) {
        var rows = jdbcTemplate.query(
                """
                SELECT elder_user_id, center_lat, center_lng, radius_meters, label
                FROM elder_geofence
                WHERE elder_user_id = ?
                LIMIT 1
                """,
                (rs, rowNum) -> new ElderGeofence(
                        rs.getString("elder_user_id"),
                        rs.getDouble("center_lat"),
                        rs.getDouble("center_lng"),
                        rs.getDouble("radius_meters"),
                        rs.getString("label")
                ),
                elderId.trim().toUpperCase()
        );
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(rows.get(0));
    }

    public void upsertGeofence(ElderGeofence geofence) {
        jdbcTemplate.update(
                """
                INSERT INTO elder_geofence(elder_user_id, center_lat, center_lng, radius_meters, label)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                  center_lat = VALUES(center_lat),
                  center_lng = VALUES(center_lng),
                  radius_meters = VALUES(radius_meters),
                  label = VALUES(label)
                """,
                geofence.elderId().trim().toUpperCase(),
                geofence.centerLat(),
                geofence.centerLng(),
                geofence.radiusMeters(),
                blankToNull(geofence.label())
        );
    }

    public Optional<ElderLastLocation> getLastLocation(String elderId) {
        var rows = jdbcTemplate.query(
                """
                SELECT elder_user_id, lat, lng, accuracy_meters, source, updated_at
                FROM elder_last_location
                WHERE elder_user_id = ?
                LIMIT 1
                """,
                (rs, rowNum) -> {
                    Timestamp ts = rs.getTimestamp("updated_at");
                    return new ElderLastLocation(
                            rs.getDouble("lat"),
                            rs.getDouble("lng"),
                            (Double) rs.getObject("accuracy_meters"),
                            rs.getString("source"),
                            ts == null ? Instant.now() : ts.toInstant()
                    );
                },
                elderId.trim().toUpperCase()
        );
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(rows.get(0));
    }

    public void upsertLastLocation(String elderId, ElderLastLocation location) {
        jdbcTemplate.update(
                """
                INSERT INTO elder_last_location(elder_user_id, lat, lng, accuracy_meters, source, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                  lat = VALUES(lat),
                  lng = VALUES(lng),
                  accuracy_meters = VALUES(accuracy_meters),
                  source = VALUES(source),
                  updated_at = VALUES(updated_at)
                """,
                elderId.trim().toUpperCase(),
                location.lat(),
                location.lng(),
                location.accuracyMeters(),
                blankToNull(location.source()),
                Timestamp.from(location.updatedAt())
        );
    }

    private void assertElderExists(String elderId) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sys_user WHERE user_id = ? AND role_code = 'ELDER'",
                Integer.class,
                elderId
        );
        if (exists == null || exists == 0) {
            throw new IllegalArgumentException("elder_not_found: " + elderId);
        }
    }

    private String blankToNull(String s) {
        if (s == null || s.isBlank()) return null;
        return s.trim();
    }
}

