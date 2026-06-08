-- 已有数据库请手动执行本脚本（若 alarm_event 表不存在）
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
) COMMENT='老人安全告警事件';
