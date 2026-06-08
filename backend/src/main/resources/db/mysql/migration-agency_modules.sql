-- 机构端扩展模块（排班/评价/活动/收费/通知/设备/健康快照）
-- 已有库可单独执行本脚本

CREATE TABLE IF NOT EXISTS agency_worker_schedule (
  id VARCHAR(64) PRIMARY KEY,
  day_label VARCHAR(16) NOT NULL COMMENT '周一至周日',
  hour_slot INT NOT NULL COMMENT '8-19',
  worker_name VARCHAR(64) NOT NULL,
  shift_type VARCHAR(16) NOT NULL DEFAULT 'morning',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_schedule_day_hour (day_label, hour_slot)
) COMMENT='机构服务人员排班';

CREATE TABLE IF NOT EXISTS agency_service_evaluation (
  id VARCHAR(64) PRIMARY KEY,
  elder_user_id VARCHAR(32) NULL,
  elder_name VARCHAR(64) NOT NULL,
  worker_name VARCHAR(64) NOT NULL,
  service_type VARCHAR(32) NOT NULL,
  rating INT NOT NULL COMMENT '总体评分1-5',
  attitude_rating INT NULL COMMENT '服务态度评分1-5',
  skill_rating INT NULL COMMENT '专业技能评分1-5',
  response_rating INT NULL COMMENT '响应速度评分1-5',
  punctuality_rating INT NULL COMMENT '准时到达评分1-5',
  communication_rating INT NULL COMMENT '沟通能力评分1-5',
  comment_text VARCHAR(1024) NULL COMMENT '文字评价',
  tags VARCHAR(256) NULL COMMENT '评价标签，逗号分隔',
  is_anonymous TINYINT(1) DEFAULT 0 COMMENT '是否匿名评价',
  service_duration_minutes INT NULL COMMENT '服务时长(分钟)',
  task_id VARCHAR(64) NULL COMMENT '关联工单ID',
  images VARCHAR(512) NULL COMMENT '评价图片，逗号分隔URL',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_eval_created (created_at),
  KEY idx_eval_worker (worker_name),
  KEY idx_eval_service_type (service_type),
  KEY idx_task_id (task_id)
) COMMENT='机构服务评价';

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
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_activity_start (start_time)
) COMMENT='机构活动';

CREATE TABLE IF NOT EXISTS agency_finance_record (
  id VARCHAR(64) PRIMARY KEY,
  elder_user_id VARCHAR(32) NULL,
  elder_name VARCHAR(64) NOT NULL,
  service_type VARCHAR(32) NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'pending',
  paid_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_finance_status (status)
) COMMENT='机构收费记录';

CREATE TABLE IF NOT EXISTS agency_notification (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(128) NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'draft',
  author VARCHAR(64) NULL,
  view_count INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_notification_status (status)
) COMMENT='机构消息通知';

CREATE TABLE IF NOT EXISTS agency_care_device (
  id VARCHAR(64) PRIMARY KEY,
  device_name VARCHAR(128) NOT NULL,
  device_type VARCHAR(64) NOT NULL,
  elder_user_id VARCHAR(32) NULL,
  elder_name VARCHAR(64) NULL,
  battery_percent INT NOT NULL DEFAULT 100,
  status VARCHAR(16) NOT NULL DEFAULT 'offline',
  last_online_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_device_status (status)
) COMMENT='机构照护设备';

CREATE TABLE IF NOT EXISTS elder_vitals_snapshot (
  elder_user_id VARCHAR(32) PRIMARY KEY,
  heart_rate INT NULL,
  systolic_bp INT NULL,
  diastolic_bp INT NULL,
  blood_oxygen INT NULL,
  recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_vitals_elder FOREIGN KEY (elder_user_id) REFERENCES sys_user(user_id)
) COMMENT='老人最新体征快照（供机构健康档案）';
