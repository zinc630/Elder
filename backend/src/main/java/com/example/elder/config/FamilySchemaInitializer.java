package com.example.elder.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class FamilySchemaInitializer {
    private static final Logger log = LoggerFactory.getLogger(FamilySchemaInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public FamilySchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            jdbcTemplate.execute(
                    """
                    CREATE TABLE IF NOT EXISTS family_chat_message (
                      id VARCHAR(64) PRIMARY KEY,
                      elder_user_id VARCHAR(32) NOT NULL,
                      sender_role VARCHAR(16) NOT NULL,
                      sender_user_id VARCHAR(32) NULL,
                      sender_name VARCHAR(64) NOT NULL,
                      content TEXT NOT NULL,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      KEY idx_family_chat_elder (elder_user_id, created_at)
                    )
                    """
            );
            jdbcTemplate.execute(
                    """
                    CREATE TABLE IF NOT EXISTS family_album_photo (
                      id VARCHAR(64) PRIMARY KEY,
                      elder_user_id VARCHAR(32) NOT NULL,
                      uploaded_by_role VARCHAR(16) NOT NULL,
                      uploaded_by_user_id VARCHAR(32) NULL,
                      file_name VARCHAR(256) NOT NULL,
                      media_url VARCHAR(512) NOT NULL,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      KEY idx_family_album_elder (elder_user_id, created_at)
                    )
                    """
            );
            jdbcTemplate.execute(
                    """
                    CREATE TABLE IF NOT EXISTS family_video_call (
                      call_id VARCHAR(64) PRIMARY KEY,
                      elder_user_id VARCHAR(32) NOT NULL,
                      child_user_id VARCHAR(32) NULL,
                      child_name VARCHAR(64) NOT NULL,
                      status VARCHAR(16) NOT NULL,
                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      KEY idx_family_video_elder (elder_user_id, updated_at)
                    )
                    """
            );
            ensureVideoCallColumns();
        } catch (Exception e) {
            log.warn("亲情互动表初始化跳过: {}", e.getMessage());
        }
    }

    private void ensureVideoCallColumns() {
        ensureColumn("family_video_call", "initiator_role", "VARCHAR(16) NOT NULL DEFAULT 'CHILD'");
        ensureColumn("family_video_call", "elder_display_name", "VARCHAR(64) NULL");
        ensureColumn("family_video_call", "offer_sdp", "TEXT NULL");
        ensureColumn("family_video_call", "answer_sdp", "TEXT NULL");
    }

    private void ensureColumn(String table, String column, String definition) {
        Integer exists = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?
                """,
                Integer.class,
                table,
                column
        );
        if (exists != null && exists > 0) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
    }
}
