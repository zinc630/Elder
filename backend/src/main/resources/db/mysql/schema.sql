-- ==========================================
-- 基础账号表（统一登录身份）
-- ==========================================
CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id VARCHAR(32) NOT NULL UNIQUE COMMENT '业务账号ID，如 E100001/C100001',
  role_code VARCHAR(16) NOT NULL COMMENT 'ELDER/CHILD/AGENCY/ADMIN',
  user_name VARCHAR(64) NOT NULL COMMENT '登录用户名',
  password VARCHAR(128) NOT NULL COMMENT '演示环境明文；生产请改哈希',
  gender VARCHAR(8) NULL,
  age INT NULL,
  address VARCHAR(255) NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1=启用,0=禁用',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_role_user_name (role_code, user_name),
  KEY idx_role_code (role_code)
) COMMENT='系统统一账号表';

-- ==========================================
-- 老人档案
-- ==========================================
CREATE TABLE IF NOT EXISTS elder_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  elder_user_id VARCHAR(32) NOT NULL UNIQUE,
  key_health_notes VARCHAR(512) NULL,
  emergency_contact_name VARCHAR(64) NULL,
  emergency_contact_phone VARCHAR(32) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_elder_profile_user FOREIGN KEY (elder_user_id) REFERENCES sys_user(user_id)
) COMMENT='老人扩展档案';

CREATE TABLE IF NOT EXISTS elder_geofence (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  elder_user_id VARCHAR(32) NOT NULL UNIQUE,
  center_lat DOUBLE NOT NULL,
  center_lng DOUBLE NOT NULL,
  radius_meters DOUBLE NOT NULL DEFAULT 500,
  label VARCHAR(128) NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_elder_geofence_user FOREIGN KEY (elder_user_id) REFERENCES sys_user(user_id)
) COMMENT='老人电子围栏配置';

CREATE TABLE IF NOT EXISTS elder_last_location (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  elder_user_id VARCHAR(32) NOT NULL UNIQUE,
  lat DOUBLE NOT NULL,
  lng DOUBLE NOT NULL,
  accuracy_meters DOUBLE NULL,
  source VARCHAR(64) NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_elder_location_user FOREIGN KEY (elder_user_id) REFERENCES sys_user(user_id)
) COMMENT='老人最新位置';

-- ==========================================
-- 子女档案
-- ==========================================
CREATE TABLE IF NOT EXISTS child_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  child_user_id VARCHAR(32) NOT NULL UNIQUE,
  relation_desc VARCHAR(64) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_child_profile_user FOREIGN KEY (child_user_id) REFERENCES sys_user(user_id)
) COMMENT='子女扩展档案';

-- ==========================================
-- 机构档案
-- ==========================================
CREATE TABLE IF NOT EXISTS agency_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  agency_user_id VARCHAR(32) NOT NULL UNIQUE,
  agency_name VARCHAR(128) NULL,
  contact_phone VARCHAR(32) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_agency_profile_user FOREIGN KEY (agency_user_id) REFERENCES sys_user(user_id)
) COMMENT='服务机构扩展档案';

CREATE TABLE IF NOT EXISTS service_worker (
  id VARCHAR(64) PRIMARY KEY,
  name VARCHAR(64) NOT NULL,
  position VARCHAR(128) NULL,
  phone VARCHAR(32) NULL,
  online_status VARCHAR(16) NOT NULL DEFAULT 'ONLINE',
  service_type VARCHAR(32) NOT NULL DEFAULT 'NURSING',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_worker_service_type (service_type),
  KEY idx_worker_name (name)
) COMMENT='服务机构工作人员';

CREATE TABLE IF NOT EXISTS agency_anomaly_report (
  id VARCHAR(64) PRIMARY KEY,
  reporter_name VARCHAR(64) NOT NULL,
  elder_user_id VARCHAR(32) NULL,
  elder_name VARCHAR(64) NULL,
  anomaly_type VARCHAR(64) NOT NULL,
  description VARCHAR(512) NULL,
  reported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  KEY idx_anomaly_reported_at (reported_at),
  KEY idx_anomaly_status (status)
) COMMENT='机构异常上报记录';

CREATE TABLE IF NOT EXISTS agency_clock_in_record (
  id VARCHAR(64) PRIMARY KEY,
  worker_name VARCHAR(64) NOT NULL,
  elder_name VARCHAR(64) NULL,
  service_type_label VARCHAR(64) NULL,
  clock_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  address VARCHAR(255) NULL,
  status_label VARCHAR(64) NULL,
  KEY idx_clock_in_time (clock_at)
) COMMENT='机构上门服务打卡记录';

CREATE TABLE IF NOT EXISTS agency_worker_schedule (
  id VARCHAR(64) PRIMARY KEY,
  day_label VARCHAR(16) NOT NULL,
  hour_slot INT NOT NULL,
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

CREATE TABLE IF NOT EXISTS family_chat_message (
  id VARCHAR(64) PRIMARY KEY,
  elder_user_id VARCHAR(32) NOT NULL,
  sender_role VARCHAR(16) NOT NULL,
  sender_user_id VARCHAR(32) NULL,
  sender_name VARCHAR(64) NOT NULL,
  content TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_family_chat_elder (elder_user_id, created_at)
) COMMENT='亲情互动聊天';

CREATE TABLE IF NOT EXISTS family_album_photo (
  id VARCHAR(64) PRIMARY KEY,
  elder_user_id VARCHAR(32) NOT NULL,
  uploaded_by_role VARCHAR(16) NOT NULL,
  uploaded_by_user_id VARCHAR(32) NULL,
  file_name VARCHAR(256) NOT NULL,
  media_url VARCHAR(512) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_family_album_elder (elder_user_id, created_at)
) COMMENT='亲情互动家庭相册';

CREATE TABLE IF NOT EXISTS family_video_call (
  call_id VARCHAR(64) PRIMARY KEY,
  elder_user_id VARCHAR(32) NOT NULL,
  child_user_id VARCHAR(32) NULL,
  child_name VARCHAR(64) NOT NULL,
  status VARCHAR(16) NOT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_family_video_elder (elder_user_id, updated_at)
) COMMENT='亲情互动视频通话信令';

CREATE TABLE IF NOT EXISTS elder_vitals_snapshot (
  elder_user_id VARCHAR(32) PRIMARY KEY,
  heart_rate INT NULL,
  systolic_bp INT NULL,
  diastolic_bp INT NULL,
  blood_oxygen INT NULL,
  recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_vitals_elder FOREIGN KEY (elder_user_id) REFERENCES sys_user(user_id)
) COMMENT='老人最新体征快照';

-- ==========================================
-- 健康与安全告警（SOS / 跌倒 / 体征异常）
-- ==========================================
CREATE TABLE IF NOT EXISTS alarm_event (
  id VARCHAR(64) PRIMARY KEY,
  elder_user_id VARCHAR(32) NOT NULL,
  alarm_type VARCHAR(32) NOT NULL COMMENT 'FALL_SUSPECTED/SOS_SUSPECTED/VITALS_LONG_ABNORMAL',
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

-- ==========================================
-- 居家养老服务工单（老人端/子女端预约 → 机构派单）
-- ==========================================
CREATE TABLE IF NOT EXISTS dispatch_task (
  id VARCHAR(64) PRIMARY KEY,
  elder_user_id VARCHAR(32) NOT NULL,
  alarm_event_id VARCHAR(64) NULL,
  service_type VARCHAR(32) NOT NULL COMMENT 'NURSING/HOUSEKEEPING/ACCOMPANY等',
  appointment_time TIMESTAMP NOT NULL,
  notes VARCHAR(512) NULL,
  assigned_worker_id VARCHAR(64) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'NEW',
  booked_by_role VARCHAR(16) NULL COMMENT 'ELDER/CHILD/AGENCY',
  booked_by_user_id VARCHAR(32) NULL,
  booked_by_name VARCHAR(64) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_dispatch_elder (elder_user_id),
  KEY idx_dispatch_status (status),
  KEY idx_dispatch_appointment (appointment_time),
  CONSTRAINT fk_dispatch_elder_user FOREIGN KEY (elder_user_id) REFERENCES sys_user(user_id)
) COMMENT='养老服务派单工单';

-- ==========================================
-- 管理员档案
-- ==========================================
CREATE TABLE IF NOT EXISTS admin_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  admin_user_id VARCHAR(32) NOT NULL UNIQUE,
  org_name VARCHAR(128) NULL COMMENT '所属机构/院区',
  title VARCHAR(64) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_admin_profile_user FOREIGN KEY (admin_user_id) REFERENCES sys_user(user_id)
) COMMENT='管理员扩展档案';

-- ==========================================
-- 亲属关联关系（老人 <-> 子女，多对多）
-- ==========================================
CREATE TABLE IF NOT EXISTS elder_child_binding (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  elder_user_id VARCHAR(32) NOT NULL,
  child_user_id VARCHAR(32) NOT NULL,
  relation_label VARCHAR(32) NULL COMMENT '子女/配偶/亲属',
  is_primary TINYINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_elder_child (elder_user_id, child_user_id),
  KEY idx_binding_elder (elder_user_id),
  KEY idx_binding_child (child_user_id),
  CONSTRAINT fk_binding_elder_user FOREIGN KEY (elder_user_id) REFERENCES sys_user(user_id),
  CONSTRAINT fk_binding_child_user FOREIGN KEY (child_user_id) REFERENCES sys_user(user_id)
) COMMENT='老人与子女的绑定关系';

-- ==========================================
-- 系统设置
-- ==========================================
CREATE TABLE IF NOT EXISTS system_settings (
  setting_key VARCHAR(64) PRIMARY KEY,
  setting_value VARCHAR(255) NOT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='系统设置';

CREATE TABLE IF NOT EXISTS sys_id_sequence (
  role_code VARCHAR(16) PRIMARY KEY,
  seq_no BIGINT NOT NULL
) COMMENT='按角色分配系统账号ID序列';

-- 初始化各角色序列：seq_no=100000，则 next-id/allocate-id 从 100001 开始
INSERT INTO sys_id_sequence(role_code, seq_no)
VALUES
  ('ELDER', 100000),
  ('CHILD', 100000),
  ('AGENCY', 100000),
  ('ADMIN', 100000)
ON DUPLICATE KEY UPDATE seq_no = seq_no;

INSERT INTO system_settings (setting_key, setting_value)
VALUES
  ('bootstrap', 'ok'),
  ('schema.version', 'role-v1')
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value);

-- ==========================================
-- 功能首页门户内容（文娱活动 / 学习赋能 / 新闻资讯）
-- ==========================================
CREATE TABLE IF NOT EXISTS portal_activity (
  id VARCHAR(32) PRIMARY KEY,
  icon VARCHAR(16) NOT NULL,
  tag VARCHAR(64) NOT NULL,
  title VARCHAR(256) NOT NULL,
  time_label VARCHAR(128) NOT NULL,
  location VARCHAR(128) NOT NULL,
  description TEXT NOT NULL,
  capacity INT NOT NULL,
  enrolled_count INT NOT NULL DEFAULT 0,
  open_flag TINYINT NOT NULL DEFAULT 1,
  image_url VARCHAR(512) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='门户文娱活动';

CREATE TABLE IF NOT EXISTS portal_news (
  id VARCHAR(32) PRIMARY KEY,
  icon VARCHAR(16) NOT NULL,
  title VARCHAR(512) NOT NULL,
  summary VARCHAR(1024) NOT NULL,
  publish_date VARCHAR(32) NOT NULL,
  source VARCHAR(128) NOT NULL,
  body MEDIUMTEXT NOT NULL,
  view_count INT NOT NULL DEFAULT 0,
  image_url VARCHAR(512) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='门户新闻资讯';

CREATE TABLE IF NOT EXISTS portal_course (
  id VARCHAR(32) PRIMARY KEY,
  icon VARCHAR(16) NOT NULL,
  category VARCHAR(64) NOT NULL,
  title VARCHAR(256) NOT NULL,
  description TEXT NOT NULL,
  duration_label VARCHAR(32) NOT NULL,
  view_count INT NOT NULL DEFAULT 0,
  rating DECIMAL(3,1) NOT NULL DEFAULT 4.8,
  image_url VARCHAR(512) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='门户学习课程';

CREATE TABLE IF NOT EXISTS portal_course_lesson (
  id VARCHAR(32) PRIMARY KEY,
  course_id VARCHAR(32) NOT NULL,
  title VARCHAR(256) NOT NULL,
  content TEXT NOT NULL,
  duration_minutes INT NOT NULL,
  video_url VARCHAR(512) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  KEY idx_lesson_course (course_id),
  CONSTRAINT fk_portal_lesson_course FOREIGN KEY (course_id) REFERENCES portal_course(id) ON DELETE CASCADE
) COMMENT='课程课时与视频';

CREATE TABLE IF NOT EXISTS portal_registration (
  id VARCHAR(32) PRIMARY KEY,
  reg_type VARCHAR(16) NOT NULL COMMENT 'ACTIVITY/LIFE/COURSE',
  target_id VARCHAR(32) NOT NULL,
  user_id VARCHAR(128) NOT NULL,
  contact_name VARCHAR(64) NOT NULL,
  contact_phone VARCHAR(32) NOT NULL,
  note VARCHAR(512) NULL,
  status VARCHAR(16) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_reg_user_target (user_id, reg_type, target_id)
) COMMENT='门户报名与预约';

CREATE TABLE IF NOT EXISTS portal_lesson_progress (
  actor_key VARCHAR(128) NOT NULL,
  course_id VARCHAR(32) NOT NULL,
  lesson_id VARCHAR(32) NOT NULL,
  completed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (actor_key, course_id, lesson_id),
  KEY idx_progress_course (course_id)
) COMMENT='课程课时完成记录';

