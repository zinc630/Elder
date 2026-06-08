package com.example.elder.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 确保已有 MySQL 库中存在 alarm_event 表（schema.sql 仅在空库初始化时执行）。
 */
@Component
public class AlarmSchemaInitializer {
    private static final Logger log = LoggerFactory.getLogger(AlarmSchemaInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public AlarmSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureAlarmTable() {
        try {
            jdbcTemplate.execute(
                    """
                    CREATE TABLE IF NOT EXISTS alarm_event (
                      id VARCHAR(64) PRIMARY KEY,
                      elder_user_id VARCHAR(32) NOT NULL,
                      alarm_type VARCHAR(32) NOT NULL,
                      status VARCHAR(32) NOT NULL,
                      triggered_at TIMESTAMP NOT NULL,
                      window_start_at TIMESTAMP NULL,
                      window_end_at TIMESTAMP NULL,
                      risk_score DOUBLE NULL,
                      abnormal_point_count INT NULL,
                      abnormal_point_threshold INT NULL,
                      confirmation_source VARCHAR(32) NULL,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      KEY idx_alarm_elder (elder_user_id),
                      KEY idx_alarm_triggered (triggered_at),
                      KEY idx_alarm_status (status),
                      CONSTRAINT fk_alarm_elder_user FOREIGN KEY (elder_user_id) REFERENCES sys_user(user_id)
                    ) COMMENT='老人安全告警事件'
                    """
            );
        } catch (Exception e) {
            log.warn("alarm_event 表初始化跳过: {}", e.getMessage());
        }
    }
}
