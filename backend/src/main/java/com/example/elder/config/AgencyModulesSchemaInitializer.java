package com.example.elder.config;

import com.example.elder.service.agency.AgencyModulesDbService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 确保已有 MySQL 库中存在机构端扩展表，并注入演示数据。
 */
@Component
public class AgencyModulesSchemaInitializer {
    private static final Logger log = LoggerFactory.getLogger(AgencyModulesSchemaInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final AgencyModulesDbService modulesDbService;

    public AgencyModulesSchemaInitializer(JdbcTemplate jdbcTemplate, AgencyModulesDbService modulesDbService) {
        this.jdbcTemplate = jdbcTemplate;
        this.modulesDbService = modulesDbService;
    }

    @PostConstruct
    public void init() {
        try {
            jdbcTemplate.execute(
                    """
                    CREATE TABLE IF NOT EXISTS agency_worker_schedule (
                      id VARCHAR(64) PRIMARY KEY,
                      day_label VARCHAR(16) NOT NULL,
                      hour_slot INT NOT NULL,
                      worker_name VARCHAR(64) NOT NULL,
                      shift_type VARCHAR(16) NOT NULL DEFAULT 'morning',
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      KEY idx_schedule_day_hour (day_label, hour_slot)
                    )
                    """
            );
            jdbcTemplate.execute(
                    """
                    CREATE TABLE IF NOT EXISTS agency_service_evaluation (
                      id VARCHAR(64) PRIMARY KEY,
                      elder_user_id VARCHAR(32) NULL,
                      elder_name VARCHAR(64) NOT NULL,
                      worker_name VARCHAR(64) NOT NULL,
                      service_type VARCHAR(32) NOT NULL,
                      rating INT NOT NULL,
                      comment_text VARCHAR(512) NULL,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """
            );
            ensureEvaluationColumns();
            jdbcTemplate.execute(
                    """
                    CREATE TABLE IF NOT EXISTS agency_activity (
                      id VARCHAR(64) PRIMARY KEY,
                      title VARCHAR(128) NOT NULL,
                      start_time TIMESTAMP NOT NULL,
                      end_time TIMESTAMP NOT NULL,
                      location VARCHAR(128) NULL,
                      max_participants INT NOT NULL DEFAULT 30,
                      registered_count INT NOT NULL DEFAULT 0,
                      status VARCHAR(16) NOT NULL DEFAULT 'upcoming',
                      description VARCHAR(1024) NULL,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """
            );
            jdbcTemplate.execute(
                    """
                    CREATE TABLE IF NOT EXISTS agency_finance_record (
                      id VARCHAR(64) PRIMARY KEY,
                      elder_user_id VARCHAR(32) NULL,
                      elder_name VARCHAR(64) NOT NULL,
                      service_type VARCHAR(32) NOT NULL,
                      amount DECIMAL(10,2) NOT NULL,
                      status VARCHAR(16) NOT NULL DEFAULT 'pending',
                      paid_at TIMESTAMP NULL,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """
            );
            jdbcTemplate.execute(
                    """
                    CREATE TABLE IF NOT EXISTS agency_notification (
                      id VARCHAR(64) PRIMARY KEY,
                      title VARCHAR(128) NOT NULL,
                      content TEXT NOT NULL,
                      status VARCHAR(16) NOT NULL DEFAULT 'draft',
                      author VARCHAR(64) NULL,
                      view_count INT NOT NULL DEFAULT 0,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """
            );
            jdbcTemplate.execute(
                    """
                    CREATE TABLE IF NOT EXISTS agency_care_device (
                      id VARCHAR(64) PRIMARY KEY,
                      device_name VARCHAR(128) NOT NULL,
                      device_type VARCHAR(64) NOT NULL,
                      elder_user_id VARCHAR(32) NULL,
                      elder_name VARCHAR(64) NULL,
                      battery_percent INT NOT NULL DEFAULT 100,
                      status VARCHAR(16) NOT NULL DEFAULT 'offline',
                      last_online_at TIMESTAMP NULL,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """
            );
            jdbcTemplate.execute(
                    """
                    CREATE TABLE IF NOT EXISTS elder_vitals_snapshot (
                      elder_user_id VARCHAR(32) PRIMARY KEY,
                      heart_rate INT NULL,
                      systolic_bp INT NULL,
                      diastolic_bp INT NULL,
                      blood_oxygen INT NULL,
                      recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                    )
                    """
            );
            modulesDbService.seedDemoDataIfEmpty();
        } catch (Exception e) {
            log.warn("机构扩展表初始化跳过: {}", e.getMessage());
        }
    }

    /** 为旧库补齐服务评价细项字段 */
    private void ensureEvaluationColumns() {
        ensureColumn("agency_service_evaluation", "attitude_rating", "INT NULL COMMENT '服务态度评分1-5'");
        ensureColumn("agency_service_evaluation", "skill_rating", "INT NULL COMMENT '专业技能评分1-5'");
        ensureColumn("agency_service_evaluation", "response_rating", "INT NULL COMMENT '响应速度评分1-5'");
        ensureColumn("agency_service_evaluation", "punctuality_rating", "INT NULL COMMENT '准时到达评分1-5'");
        ensureColumn("agency_service_evaluation", "communication_rating", "INT NULL COMMENT '沟通能力评分1-5'");
        ensureColumn("agency_service_evaluation", "tags", "VARCHAR(256) NULL COMMENT '评价标签'");
        ensureColumn("agency_service_evaluation", "is_anonymous", "TINYINT(1) DEFAULT 0 COMMENT '是否匿名'");
        ensureColumn("agency_service_evaluation", "service_duration_minutes", "INT NULL COMMENT '服务时长(分钟)'");
        ensureColumn("agency_service_evaluation", "task_id", "VARCHAR(64) NULL COMMENT '关联工单ID'");
        ensureColumn("agency_service_evaluation", "images", "VARCHAR(512) NULL COMMENT '评价图片URL'");
        ensureColumn("agency_service_evaluation", "updated_at",
                "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE agency_service_evaluation MODIFY COLUMN comment_text VARCHAR(1024) NULL"
            );
        } catch (Exception ignored) {
            // 表结构已为目标形态时忽略
        }
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
