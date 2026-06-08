<template>
  <div class="agency-page">
    <!-- ===== 顶部导航 ===== -->
    <header class="app-topbar" :class="{ 'topbar-scrolled': isScrolled }">
      <div class="container">
        <div class="topbar-inner">
          <div class="topbar-brand">
            <div class="brand-mark">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M12 2L4 7V17L12 22L20 17V7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                <path d="M12 8V16M8 12H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="brand-text">
              <span class="brand-name">银发智盾</span>
              <span class="brand-sub">机构管理端</span>
            </div>
          </div>

          <div class="topbar-actions">
            <button class="topbar-btn outline" @click="toggleAssistant">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <circle cx="8" cy="8" r="6.5" stroke="currentColor" stroke-width="1.2"/>
                <path d="M5.5 6.5C5.5 6.5 6.5 5 8 5C9.5 5 10.5 6.5 10.5 6.5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                <path d="M6 9H10" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
              </svg>
              智能助手
            </button>
            <div class="user-avatar">
              <span>管理员</span>
              <div class="avatar-circle">👤</div>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- ===== 告警通知横幅 ===== -->
    <div
        v-if="pendingAlerts.length > 0 && !alertBannerDismissed"
        class="alert-banner"
        :class="{ 'alert-banner-sos': pendingSosAlerts.length > 0 }"
        @click="openAlertList"
    >
      <div class="alert-icon">{{ pendingSosAlerts.length > 0 ? '🆘' : '🔔' }}</div>
      <div class="alert-text">
        <template v-if="pendingSosAlerts.length > 0">
          <strong>{{ pendingSosAlerts[0].elderName }}</strong> 发起 <strong>SOS 紧急求助</strong>，
          <span class="alert-link">请立即响应</span>
          <span v-if="pendingAlerts.length > 1" class="alert-more">（另有 {{ pendingAlerts.length - 1 }} 条待处理）</span>
        </template>
        <template v-else>
          有 <strong>{{ pendingAlerts.length }}</strong> 条待处理告警，
          <span class="alert-link">点击查看</span>
        </template>
      </div>
      <div class="alert-close" @click.stop="dismissAlertBanner">×</div>
    </div>

    <!-- ===== 内容区域 ===== -->
    <div class="main-content">
      <!-- ===== 统计卡片 ===== -->
      <section id="dashboard" class="section metrics-section">
        <div class="container">
          <div class="section-header">
            <h2 class="section-title">📊 运营总览</h2>
            <p class="section-desc">实时掌握机构运营状况，数据驱动决策</p>
          </div>
          <div class="metrics-grid">
            <div class="metric-card hover-lift">
              <div class="metric-icon-wrap">
                <span class="metric-icon">👤</span>
              </div>
              <div class="metric-content">
                <div class="metric-num">
                  {{ dashboardStats.totalElders }}<span class="metric-unit">人</span>
                </div>
                <div class="metric-label">服务老人总数</div>
                <div class="metric-sub">覆盖 {{ dashboardStats.agencyCount }} 个机构</div>
              </div>
            </div>
            <div class="metric-card hover-lift">
              <div class="metric-icon-wrap blue">
                <span class="metric-icon">📋</span>
              </div>
              <div class="metric-content">
                <div class="metric-num">
                  {{ dashboardStats.todayOrders }}<span class="metric-unit">单</span>
                </div>
                <div class="metric-label">今日工单</div>
                <div class="metric-sub">待处理 {{ dashboardStats.pendingOrders }} 单</div>
              </div>
            </div>
            <div class="metric-card hover-lift">
              <div class="metric-icon-wrap green">
                <span class="metric-icon">✅</span>
              </div>
              <div class="metric-content">
                <div class="metric-num">
                  {{ dashboardStats.completedTotal }}<span class="metric-unit">单</span>
                </div>
                <div class="metric-label">已完成（累计）</div>
                <div class="metric-sub">累计完成率 {{ dashboardStats.completionRatePercent }}%</div>
              </div>
            </div>
            <div class="metric-card hover-lift">
              <div class="metric-icon-wrap purple">
                <span class="metric-icon">🟢</span>
              </div>
              <div class="metric-content">
                <div class="metric-num">
                  {{ dashboardStats.onlineWorkers }}<span class="metric-unit">人</span>
                </div>
                <div class="metric-label">服务人员在线</div>
                <div class="metric-sub">总人数 {{ dashboardStats.totalWorkers }} 人</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ===== 趋势图表 ===== -->
      <section class="section chart-section">
        <div class="container">
          <div class="chart-card">
            <div class="chart-header">
              <div>
                <h3 class="chart-title">📈 近7天工单趋势</h3>
                <p class="chart-sub">
                  累计完成率 {{ dashboardStats.completionRatePercent }}% · 实时监控工单动态
                </p>
              </div>
              <button class="chart-btn" @click="openReportModal">查看完整报表</button>
            </div>
            <div class="chart-wrap">
              <svg class="trend-chart" viewBox="0 0 420 160" preserveAspectRatio="none">
                <defs>
                  <linearGradient id="trendArea" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stop-color="#3b82f6" stop-opacity="0.3" />
                    <stop offset="100%" stop-color="#3b82f6" stop-opacity="0.02" />
                  </linearGradient>
                </defs>
                <!-- 背景网格 -->
                <g stroke="rgba(0,0,0,0.06)" stroke-width="1">
                  <line x1="40" y1="20" x2="400" y2="20" />
                  <line x1="40" y1="55" x2="400" y2="55" />
                  <line x1="40" y1="90" x2="400" y2="90" />
                  <line x1="40" y1="125" x2="400" y2="125" />
                </g>
                <!-- 面积填充 -->
                <polygon :points="areaPoints" fill="url(#trendArea)" />
                <!-- 趋势线 -->
                <polyline
                    fill="none"
                    stroke="#3b82f6"
                    stroke-width="3"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    :points="orderTrendPoints"
                />
                <!-- 数据点 -->
                <g v-for="(v, i) in orderTrendValues" :key="i">
                  <circle
                      :cx="40 + i * 52"
                      :cy="getYPosition(v)"
                      r="4"
                      fill="#fff"
                      stroke="#3b82f6"
                      stroke-width="2"
                  />
                </g>
                <!-- X轴标签 -->
                <g v-for="(d, i) in orderTrendLabels" :key="i">
                  <text :x="40 + i * 52" y="148" class="axis-label">{{ d }}</text>
                </g>
              </svg>
            </div>
          </div>
        </div>
      </section>

      <!-- ===== Tab 导航 ===== -->
      <section class="section tabs-section">
        <div class="container">
          <div class="tabs-container">
            <div class="tabs-header">
              <button
                  v-for="tab in tabs"
                  :key="tab.key"
                  class="tab-btn"
                  :class="{ active: activeTab === tab.key }"
                  @click="switchTab(tab.key)"
              >
                <span class="tab-icon">{{ tab.icon }}</span>
                <span class="tab-label">{{ tab.label }}</span>
              </button>
            </div>

            <div class="tab-content">
              <!-- ===== 工单派单管理 ===== -->
              <div v-if="activeTab === 'dispatch'" class="panel-inner">
                <div class="panel-header">
                  <div class="search-row">
                    <input
                        v-model="taskSearch"
                        class="search-input"
                        placeholder="搜索老人/工单号"
                        @keyup.enter="refreshTasks"
                    />
                    <button type="button" class="search-btn" @click="refreshTasks">搜索</button>
                  </div>
                  <button type="button" class="btn-primary" @click="openCreateTaskModal">+ 新建服务工单</button>
                </div>

                <div class="table-wrap">
                  <table class="table">
                    <thead>
                    <tr>
                      <th>工单号</th>
                      <th>老人姓名</th>
                      <th>服务类型</th>
                      <th>状态</th>
                      <th>服务人员</th>
                      <th>预约时间</th>
                      <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-for="t in filteredTasks" :key="t.id">
                      <td class="mono">{{ workOrderNo(t) }}</td>
                      <td>{{ elderNameMap[t.elderId] ?? t.elderId }}</td>
                      <td>{{ serviceTypeLabel(t.serviceType) }}</td>
                      <td>
                          <span class="status-tag" :class="statusClass(t.status)">
                            {{ statusLabel(t.status) }}
                          </span>
                      </td>
                      <td>{{ assignedStaffName(t) }}</td>
                      <td>{{ formatDateTime(t.appointmentTime) }}</td>
                      <td>
                        <div class="row-actions">
                          <template v-if="t.status === 'NEW'">
                            <button type="button" class="btn-blue" @click="openAssignModal(t)">派单</button>
                            <button type="button" class="btn-detail" @click="openTaskDetail(t)">详情</button>
                          </template>
                          <template v-else-if="t.status === 'IN_PROGRESS' || t.status === 'ASSIGNED' || t.status === 'ARRIVING'">
                            <button type="button" class="btn-green" @click="markTaskCompleted(t)">完成服务</button>
                            <button type="button" class="btn-detail" @click="openTaskDetail(t)">详情</button>
                          </template>
                          <template v-else>
                            <button type="button" class="btn-detail" @click="openTaskDetail(t)">详情</button>
                          </template>
                        </div>
                      </td>
                    </tr>
                    </tbody>
                  </table>
                  <div v-if="filteredTasks.length === 0" class="empty">暂无工单</div>
                </div>
              </div>

              <!-- ===== 服务人员管理 ===== -->
              <div v-else-if="activeTab === 'workers'" class="panel-inner">
                <div class="panel-header">
                  <div class="search-row">
                    <input
                        v-model="workerSearch"
                        class="search-input"
                        placeholder="搜索姓名"
                    />
                    <button type="button" class="search-btn" @click="refreshWorkers">搜索</button>
                  </div>
                  <button type="button" class="btn-primary" @click="openAddWorkerModal">+ 添加服务人员</button>
                </div>

                <div class="table-wrap">
                  <table class="table">
                    <thead>
                    <tr>
                      <th>姓名</th>
                      <th>岗位</th>
                      <th>联系电话</th>
                      <th>状态</th>
                      <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-for="w in filteredWorkers" :key="w.id">
                      <td>{{ w.name }}</td>
                      <td>{{ w.position }}</td>
                      <td>{{ w.phone }}</td>
                      <td>
                          <span class="status-tag" :class="w.onlineStatus === 'ONLINE' ? 'online' : 'offline'">
                            {{ w.onlineStatus === 'ONLINE' ? '在线' : '离线' }}
                          </span>
                      </td>
                      <td>
                        <div class="row-actions">
                          <button type="button" class="btn-detail" @click="openEditWorkerModal(w)">编辑</button>
                          <button type="button" class="btn-red" @click="deleteWorker(w.id)">删除</button>
                        </div>
                      </td>
                    </tr>
                    </tbody>
                  </table>
                  <div v-if="filteredWorkers.length === 0" class="empty">暂无服务人员</div>
                </div>
              </div>

              <!-- ===== 排班管理 ===== -->
              <div v-else-if="activeTab === 'schedule'" class="panel-inner">
                <div class="panel-header">
                  <div class="schedule-controls">
                    <button class="btn-outline" @click="prevWeek">‹ 上一周</button>
                    <span class="schedule-date">{{ currentWeekLabel }}</span>
                    <button class="btn-outline" @click="nextWeek">下一周 ›</button>
                  </div>
                  <button type="button" class="btn-primary" @click="openAddScheduleModal">+ 添加排班</button>
                </div>

                <div class="schedule-grid">
                  <div class="schedule-header">
                    <div class="schedule-time">时间</div>
                    <div v-for="day in weekDays" :key="day" class="schedule-day">{{ day }}</div>
                  </div>
                  <div v-for="hour in workHours" :key="hour" class="schedule-row">
                    <div class="schedule-time">{{ hour }}:00</div>
                    <div v-for="day in weekDays" :key="day" class="schedule-cell">
                      <div v-for="shift in getShifts(day, hour)" :key="shift.id" class="shift-item" :class="shift.type">
                        <div class="shift-name">{{ shift.workerName }}</div>
                        <div class="shift-type">{{ shift.type === 'morning' ? '早班' : shift.type === 'afternoon' ? '中班' : '晚班' }}</div>
                        <button class="shift-delete" @click="deleteShift(shift.id)">×</button>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="schedule-legend">
                  <span><span class="dot morning"></span>早班</span>
                  <span><span class="dot afternoon"></span>中班</span>
                  <span><span class="dot night"></span>晚班</span>
                </div>
              </div>

              <!-- ===== 服务评价 ===== -->
              <div v-else-if="activeTab === 'evaluations'" class="panel-inner">
                <div class="panel-header">
                  <div class="search-row">
                    <input
                        v-model="evaluationSearch"
                        class="search-input"
                        placeholder="搜索老人/服务人员/工单号"
                    />
                    <button class="search-btn" @click="refreshEvaluations">搜索</button>
                  </div>
                  <div class="filter-group">
                    <select v-model="evaluationFilter" class="filter-select">
                      <option value="all">全部评价</option>
                      <option value="excellent">非常满意(5星)</option>
                      <option value="good">满意(4星)</option>
                      <option value="normal">一般(3星)</option>
                      <option value="poor">不满意(1-2星)</option>
                    </select>
                    <select v-model="evaluationServiceTypeFilter" class="filter-select">
                      <option value="all">全部服务类型</option>
                      <option value="NURSING">助餐</option>
                      <option value="BATH">助浴</option>
                      <option value="HOUSEKEEPING">保洁</option>
                      <option value="ACCOMPANY">陪诊</option>
                    </select>
                  </div>
                </div>

                <div class="evaluation-stats evaluation-stats-extended">
                  <div class="stat-item">
                    <div class="stat-num">{{ totalEvaluations }}</div>
                    <div class="stat-label">总评价数</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-num">{{ averageRating }}分</div>
                    <div class="stat-label">平均总体评分</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-num">{{ satisfactionRate }}%</div>
                    <div class="stat-label">满意度(≥4星)</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-num">{{ avgAttitudeRating }}</div>
                    <div class="stat-label">服务态度均分</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-num">{{ avgSkillRating }}</div>
                    <div class="stat-label">专业技能均分</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-num">{{ avgPunctualityRating }}</div>
                    <div class="stat-label">准时到达均分</div>
                  </div>
                </div>

                <div class="table-wrap">
                  <table class="table table-evaluations">
                    <thead>
                    <tr>
                      <th>评价ID</th>
                      <th>老人</th>
                      <th>服务人员</th>
                      <th>服务类型</th>
                      <th>总体评分</th>
                      <th>维度评分</th>
                      <th>评价标签</th>
                      <th>评语</th>
                      <th>服务时长</th>
                      <th>关联工单</th>
                      <th>评价时间</th>
                      <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-for="e in filteredEvaluations" :key="e.id">
                      <td class="mono">{{ e.id.slice(0, 8) }}</td>
                      <td>{{ e.isAnonymous ? '匿名用户' : e.elderName }}</td>
                      <td>{{ e.workerName }}</td>
                      <td>{{ serviceTypeLabel(e.serviceType) }}</td>
                      <td>
                        <div class="rating">
                          <span class="stars">{{ '★'.repeat(e.rating) }}{{ '☆'.repeat(5 - e.rating) }}</span>
                          <span class="rating-num">{{ e.rating }}分</span>
                        </div>
                      </td>
                      <td>
                        <div class="detail-ratings">
                          <div v-for="dim in evaluationDimensionRows(e)" :key="dim.key" class="mini-rating" :title="dim.label">
                            <span>{{ dim.label }}:</span>
                            <span class="stars">{{ '★'.repeat(dim.value) }}{{ '☆'.repeat(5 - dim.value) }}</span>
                          </div>
                          <span v-if="evaluationDimensionRows(e).length === 0">—</span>
                        </div>
                      </td>
                      <td>
                        <div v-if="e.tags && e.tags.length > 0" class="tags-container">
                          <span v-for="tag in e.tags" :key="tag" class="tag">{{ tag }}</span>
                        </div>
                        <span v-else>—</span>
                      </td>
                      <td class="comment-cell">
                        <div class="comment-preview">{{ e.comment || '—' }}</div>
                        <button
                            v-if="e.comment && e.comment.length > 40"
                            type="button"
                            class="btn-link"
                            @click="viewFullComment(e)"
                        >查看全文</button>
                      </td>
                      <td>{{ e.serviceDurationMinutes ? `${e.serviceDurationMinutes}分钟` : '—' }}</td>
                      <td class="mono">{{ evaluationTaskLabel(e) }}</td>
                      <td>{{ formatDateTime(e.createdAt) }}</td>
                      <td>
                        <button type="button" class="btn-detail" @click="viewEvaluationDetail(e)">详情</button>
                      </td>
                    </tr>
                    </tbody>
                  </table>
                  <div v-if="filteredEvaluations.length === 0" class="empty">暂无评价记录</div>
                </div>
              </div>

              <!-- ===== 健康档案 ===== -->
              <div v-else-if="activeTab === 'health'" class="panel-inner">
                <div class="panel-header">
                  <div class="search-row">
                    <input
                        v-model="healthSearch"
                        class="search-input"
                        placeholder="搜索老人姓名"
                    />
                    <button type="button" class="search-btn" @click="refreshHealthRecords">搜索</button>
                  </div>
                  <div class="filter-group">
                    <select v-model="healthFilter" class="filter-select">
                      <option value="all">全部</option>
                      <option value="normal">正常</option>
                      <option value="attention">需关注</option>
                      <option value="critical">异常</option>
                    </select>
                  </div>
                </div>

                <div class="table-wrap">
                  <table class="table">
                    <thead>
                    <tr>
                      <th>姓名</th>
                      <th>年龄</th>
                      <th>性别</th>
                      <th>健康状态</th>
                      <th>心率</th>
                      <th>血压</th>
                      <th>血氧</th>
                      <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-for="e in filteredHealthRecords" :key="e.elderId">
                      <td>{{ e.name }}</td>
                      <td>{{ e.age }}岁</td>
                      <td>{{ e.gender }}</td>
                      <td>
                          <span class="status-tag" :class="getHealthStatusClass(e.healthStatus)">
                            {{ e.healthStatus || '正常' }}
                          </span>
                      </td>
                      <td :class="{ crit: e.heartRate > 100 || e.heartRate < 60 }">
                        {{ e.heartRate || '--' }} bpm
                      </td>
                      <td>{{ e.bloodPressure || '--' }}</td>
                      <td :class="{ warn: e.bloodOxygen < 95 }">
                        {{ e.bloodOxygen || '--' }}%
                      </td>
                      <td>
                        <button class="btn-detail" @click="openHealthDetail(e)">查看详情</button>
                      </td>
                    </tr>
                    </tbody>
                  </table>
                  <div v-if="filteredHealthRecords.length === 0" class="empty">暂无健康数据</div>
                </div>
              </div>

              <!-- ===== 告警管理 ===== -->
              <div v-else-if="activeTab === 'alerts'" class="panel-inner">
                <div class="panel-header">
                  <div class="search-row">
                    <input
                        v-model="alertSearch"
                        class="search-input"
                        placeholder="搜索老人/告警类型"
                    />
                    <button type="button" class="search-btn" @click="refreshAlerts">搜索</button>
                  </div>
                  <div class="filter-group">
                    <select v-model="alertFilter" class="filter-select">
                      <option value="all">全部</option>
                      <option value="pending">待处理</option>
                      <option value="processing">处理中</option>
                      <option value="resolved">已处理</option>
                    </select>
                  </div>
                </div>

                <div class="table-wrap">
                  <table class="table">
                    <thead>
                    <tr>
                      <th>告警ID</th>
                      <th>老人姓名</th>
                      <th>告警类型</th>
                      <th>告警时间</th>
                      <th>状态</th>
                      <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-for="a in filteredAlerts" :key="a.id">
                      <td class="mono">{{ a.id.slice(0, 8) }}</td>
                      <td>{{ a.elderName }}</td>
                      <td>
                          <span class="alert-type" :class="getAlertTypeClass(a.type)">
                            {{ getAlertTypeLabel(a.type) }}
                          </span>
                      </td>
                      <td>{{ formatDateTime(a.triggeredAt) }}</td>
                      <td>
                          <span class="status-tag" :class="getAlertStatusClass(a.status)">
                            {{ getAlertStatusLabel(a.status) }}
                          </span>
                      </td>
                      <td>
                        <div class="row-actions">
                          <button class="btn-detail" @click="openAlertDetail(a)">详情</button>
                          <button v-if="a.status === 'PENDING'" class="btn-blue" @click="processAlert(a.id)">
                            处理
                          </button>
                        </div>
                      </td>
                    </tr>
                    </tbody>
                  </table>
                  <div v-if="filteredAlerts.length === 0" class="empty">暂无告警记录</div>
                </div>
              </div>

              <!-- ===== 活动管理 ===== -->
              <div v-else-if="activeTab === 'activities'" class="panel-inner">
                <p class="panel-hint">与功能首页、老人/子女端「文娱活动」共用同一活动库，发布后立即同步。</p>
                <div class="panel-header">
                  <button type="button" class="btn-primary" @click="openAddActivityModal">+ 发布活动</button>
                  <div class="search-row">
                    <input
                        v-model="activitySearch"
                        class="search-input"
                        placeholder="搜索活动名称"
                    />
                    <button class="search-btn" @click="refreshActivities">搜索</button>
                  </div>
                </div>

                <div class="activities-grid">
                  <div v-for="a in filteredActivities" :key="a.id" class="activity-card hover-lift">
                    <div class="activity-header">
                      <div class="activity-title">{{ a.title }}</div>
                      <div class="activity-status" :class="a.status">
                        {{ a.status === 'upcoming' ? '即将开始' : a.status === 'ongoing' ? '进行中' : '已结束' }}
                      </div>
                    </div>
                    <div class="activity-info">
                      <div class="info-item">📅 {{ formatDateTime(a.startTime) }} ~ {{ formatDateTime(a.endTime) }}</div>
                      <div class="info-item">📍 {{ a.location }}</div>
                      <div class="info-item">👥 已报名 {{ a.registered }} / {{ a.maxParticipants }} 人</div>
                      <div class="info-item">📝 {{ a.description || '暂无描述' }}</div>
                    </div>
                    <div class="activity-actions">
                      <button class="btn-detail" @click="openActivityDetail(a)">查看详情</button>
                      <button class="btn-blue" @click="openActivityRegistration(a)">报名管理</button>
                      <button class="btn-green" @click="openActivityCheckin(a)">签到</button>
                    </div>
                  </div>
                </div>
                <div v-if="filteredActivities.length === 0" class="empty">暂无活动</div>
              </div>

              <!-- ===== 收费管理 ===== -->
              <div v-else-if="activeTab === 'finance'" class="panel-inner">
                <div class="panel-header">
                  <div class="search-row">
                    <input
                        v-model="financeSearch"
                        class="search-input"
                        placeholder="搜索老人姓名"
                    />
                    <button class="search-btn" @click="refreshFinance">搜索</button>
                  </div>
                  <button type="button" class="btn-primary" @click="openAddFinanceModal">+ 新增收费</button>
                </div>

                <div class="finance-summary">
                  <div class="summary-item">
                    <div class="summary-num">¥ {{ totalIncome }}</div>
                    <div class="summary-label">总收入</div>
                  </div>
                  <div class="summary-item">
                    <div class="summary-num">{{ financeRecords.length }}</div>
                    <div class="summary-label">收费记录数</div>
                  </div>
                  <div class="summary-item">
                    <div class="summary-num">{{ pendingPayments }}</div>
                    <div class="summary-label">待支付</div>
                  </div>
                </div>

                <div class="table-wrap">
                  <table class="table">
                    <thead>
                    <tr>
                      <th>记录ID</th>
                      <th>老人</th>
                      <th>服务类型</th>
                      <th>金额</th>
                      <th>支付状态</th>
                      <th>支付时间</th>
                      <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-for="f in filteredFinanceRecords" :key="f.id">
                      <td class="mono">{{ f.id.slice(0, 6) }}</td>
                      <td>{{ f.elderName }}</td>
                      <td>{{ serviceTypeLabel(f.serviceType) }}</td>
                      <td>¥ {{ f.amount }}</td>
                      <td>
                          <span class="status-tag" :class="f.status === 'paid' ? 'done' : 'pending'">
                            {{ f.status === 'paid' ? '已支付' : '待支付' }}
                          </span>
                      </td>
                      <td>{{ f.paidAt ? formatDateTime(f.paidAt) : '—' }}</td>
                      <td>
                        <button class="btn-detail" @click="openFinanceDetail(f)">详情</button>
                        <button v-if="f.status === 'pending'" class="btn-blue" @click="markAsPaid(f.id)">标记支付</button>
                      </td>
                    </tr>
                    </tbody>
                  </table>
                  <div v-if="filteredFinanceRecords.length === 0" class="empty">暂无收费记录</div>
                </div>
              </div>

              <!-- ===== 消息通知 ===== -->
              <div v-else-if="activeTab === 'notifications'" class="panel-inner">
                <div class="panel-header">
                  <button type="button" class="btn-primary" @click="openAddNotificationModal">+ 发布通知</button>
                  <div class="search-row">
                    <input
                        v-model="notificationSearch"
                        class="search-input"
                        placeholder="搜索通知标题"
                    />
                    <button class="search-btn" @click="refreshNotifications">搜索</button>
                  </div>
                </div>

                <div class="notification-list">
                  <div v-for="n in filteredNotifications" :key="n.id" class="notification-card">
                    <div class="notification-header">
                      <div class="notification-title">{{ n.title }}</div>
                      <div class="notification-status" :class="n.status">
                        {{ n.status === 'published' ? '已发布' : n.status === 'draft' ? '草稿' : '已过期' }}
                      </div>
                    </div>
                    <div class="notification-content">{{ n.content }}</div>
                    <div class="notification-meta">
                      <span>📅 {{ formatDateTime(n.createdAt) }}</span>
                      <span>👤 {{ n.author }}</span>
                      <span>👁️ {{ n.views }} 次阅读</span>
                    </div>
                    <div class="notification-actions">
                      <button class="btn-detail" @click="openNotificationDetail(n)">详情</button>
                      <button class="btn-blue" @click="editNotification(n)">编辑</button>
                      <button class="btn-red" @click="deleteNotificationHandler(n.id)">删除</button>
                    </div>
                  </div>
                </div>
                <div v-if="filteredNotifications.length === 0" class="empty">暂无通知</div>
              </div>

              <!-- ===== 设备管理 ===== -->
              <div v-else-if="activeTab === 'devices'" class="panel-inner">
                <div class="panel-header">
                  <div class="search-row">
                    <input
                        v-model="deviceSearch"
                        class="search-input"
                        placeholder="搜索设备ID/老人姓名"
                    />
                    <button class="search-btn" @click="refreshDevices">搜索</button>
                  </div>
                  <button type="button" class="btn-primary" @click="openAddDeviceModal">+ 添加设备</button>
                </div>

                <div class="device-grid">
                  <div v-for="d in filteredDevices" :key="d.id" class="device-card">
                    <div class="device-header">
                      <div class="device-name">{{ d.name }}</div>
                      <div class="device-status" :class="d.status">
                        {{ d.status === 'online' ? '在线' : d.status === 'offline' ? '离线' : '低电量' }}
                      </div>
                    </div>
                    <div class="device-info">
                      <div class="info-item">📱 类型: {{ d.type }}</div>
                      <div class="info-item">🔗 绑定老人: {{ d.elderName || '未绑定' }}</div>
                      <div class="info-item">🔋 电量: {{ d.battery }}%</div>
                      <div class="info-item">📅 最后在线: {{ formatDateTime(d.lastOnline) }}</div>
                    </div>
                    <div class="device-actions">
                      <button class="btn-detail" @click="openDeviceDetail(d)">详情</button>
                      <button class="btn-blue" @click="bindDevice(d)">绑定老人</button>
                      <button class="btn-red" @click="deleteDeviceHandler(d.id)">删除</button>
                    </div>
                  </div>
                </div>
                <div v-if="filteredDevices.length === 0" class="empty">暂无设备</div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- ===== 智能助手弹窗 ===== -->
    <Teleport to="body">
      <div v-if="showAssistant" class="assistant-overlay" @click.self="showAssistant = false">
        <div class="assistant-modal">
          <div class="assistant-modal-head">
            <div class="assistant-modal-title">
              <div class="ai-icon-pulse">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                  <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="1.2"/>
                  <path d="M10 6V10L13 13" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                </svg>
              </div>
              <div>
                <h3>智慧助手</h3>
                <span class="assistant-badge">DeepSeek</span>
              </div>
            </div>
            <button class="assistant-close" @click="showAssistant = false">×</button>
          </div>
          <div class="assistant-messages">
            <div v-for="m in chatMessages" :key="m.id" class="assistant-msg" :class="m.role">
              <div class="assistant-bubble">{{ m.text }}</div>
            </div>
          </div>
          <div class="assistant-quick">
            <button v-for="p in quickPrompts" :key="p" class="assistant-chip" @click="applyQuickPrompt(p)">{{ p }}</button>
          </div>
          <div class="assistant-compose">
            <input v-model="aiDraft" type="search" placeholder="输入问题，回车搜索..." @keydown.enter.prevent="sendAiMessage" />
            <button :disabled="aiBusy" @click="sendAiMessage">
              <svg v-if="!aiBusy" width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M2 8L14 2L8 14L6 9L2 8Z" fill="currentColor"/>
              </svg>
              <span v-else class="loading-dot"></span>
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ===== 侧边栏 ===== -->
    <div class="side-service">
      <ul>
        <li>
          <a href="tel:400-000-0000" class="side-service-item">
            <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
              <path d="M6.5 3.5L9 6L7.5 7.5C8.5 9.5 10.5 11.5 12.5 12.5L14 11L16.5 13.5V17.5C16.5 18.3 15.8 19 15 19C8.9 19 4 14.1 4 8C4 7.2 4.7 6.5 5.5 6.5H6.5Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
            </svg>
            <span class="side-service-popup">400-000-0000</span>
          </a>
        </li>
        <li>
          <a href="javascript:;" class="side-service-item" @click="toastDemo('微信号：silverGuard2024')">
            <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
              <rect x="2" y="3" width="18" height="15" rx="3" stroke="currentColor" stroke-width="1.5"/>
              <path d="M8 12C8 12 9.5 10 11 10C12.5 10 14 12 14 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span class="side-service-popup">微信咨询</span>
          </a>
        </li>
        <li>
          <a href="javascript:;" class="side-service-item backtop" :class="{ visible: showBackTop }" @click="scrollToTop">
            <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
              <path d="M11 18V6M6 11L11 6L16 11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span class="side-service-popup">返回顶部</span>
          </a>
        </li>
      </ul>
    </div>

    <!-- ===== 报表弹窗 ===== -->
    <div v-if="showReportModal" class="modal-overlay">
      <div class="modal-card large">
        <div class="modal-top">
          <div class="modal-title">📊 完整数据报表</div>
          <button class="modal-close" @click="showReportModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="report-grid">
            <div class="report-card">
              <h4>📈 工单趋势（近30天）</h4>
              <div class="report-chart-placeholder">
                <div class="bar-chart">
                  <div v-for="(v, i) in monthlyOrderData" :key="i" class="bar-item">
                    <div class="bar" :style="{ height: v / maxMonthlyOrders * 100 + '%' }"></div>
                    <div class="bar-label">{{ i + 1 }}日</div>
                  </div>
                </div>
              </div>
            </div>
            <div class="report-card">
              <h4>📊 服务满意度分布</h4>
              <div class="pie-chart">
                <div class="pie-segment" style="--pct: 72; --color: #67c23a;"></div>
                <div class="pie-segment" style="--pct: 20; --color: #3b82f6;"></div>
                <div class="pie-segment" style="--pct: 8; --color: #e6a23c;"></div>
              </div>
              <div class="legend">
                <span><span class="dot green"></span>非常满意 72%</span>
                <span><span class="dot blue"></span>满意 20%</span>
                <span><span class="dot orange"></span>一般 8%</span>
              </div>
            </div>
            <div class="report-card">
              <h4>📋 服务人员绩效</h4>
              <div class="performance-list">
                <div v-for="w in workerPerformance" :key="w.id" class="perf-item">
                  <span class="perf-name">{{ w.name }}</span>
                  <span class="perf-score">{{ w.score }}分</span>
                  <div class="perf-bar">
                    <div class="perf-fill" :style="{ width: w.score / 100 * 100 + '%' }"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-cta">
            <button class="btn-primary" @click="exportReport">📄 导出完整报表（PDF）</button>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== 健康详情弹窗 ===== -->
    <div v-if="showHealthDetail" class="modal-overlay">
      <div class="modal-card">
        <div class="modal-top">
          <div class="modal-title">🏥 健康详情 - {{ currentHealthElder?.name }}</div>
          <button class="modal-close" @click="showHealthDetail = false">×</button>
        </div>
        <div class="modal-body">
          <div class="detail-grid">
            <div class="d-item"><span class="d-k">姓名</span><span class="d-v">{{ currentHealthElder?.name }}</span></div>
            <div class="d-item"><span class="d-k">年龄</span><span class="d-v">{{ currentHealthElder?.age }}岁</span></div>
            <div class="d-item"><span class="d-k">性别</span><span class="d-v">{{ currentHealthElder?.gender }}</span></div>
            <div class="d-item"><span class="d-k">健康状态</span><span class="d-v">{{ currentHealthElder?.healthStatus || '正常' }}</span></div>
            <div class="d-item"><span class="d-k">心率</span><span class="d-v">{{ currentHealthElder?.heartRate || '--' }} bpm</span></div>
            <div class="d-item"><span class="d-k">血压</span><span class="d-v">{{ currentHealthElder?.bloodPressure || '--' }}</span></div>
            <div class="d-item"><span class="d-k">血氧</span><span class="d-v">{{ currentHealthElder?.bloodOxygen || '--' }}%</span></div>
            <div class="d-item"><span class="d-k">最近记录</span><span class="d-v">{{ formatDateTime(currentHealthElder?.lastUpdated) || '--' }}</span></div>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-confirm" @click="showHealthDetail = false">关闭</button>
        </div>
      </div>
    </div>

    <!-- ===== 告警详情弹窗 ===== -->
    <div v-if="showAlertDetail" class="modal-overlay">
      <div class="modal-card">
        <div class="modal-top">
          <div class="modal-title">🔔 告警详情</div>
          <button class="modal-close" @click="showAlertDetail = false">×</button>
        </div>
        <div class="modal-body">
          <div class="detail-grid">
            <div class="d-item"><span class="d-k">告警ID</span><span class="d-v">{{ currentAlert?.id }}</span></div>
            <div class="d-item"><span class="d-k">老人姓名</span><span class="d-v">{{ currentAlert?.elderName }}</span></div>
            <div class="d-item"><span class="d-k">告警类型</span><span class="d-v">{{ getAlertTypeLabel(currentAlert?.type) }}</span></div>
            <div class="d-item"><span class="d-k">告警时间</span><span class="d-v">{{ formatDateTime(currentAlert?.triggeredAt) }}</span></div>
            <div class="d-item"><span class="d-k">状态</span><span class="d-v">{{ getAlertStatusLabel(currentAlert?.status) }}</span></div>
            <div class="d-item"><span class="d-k">处理记录</span><span class="d-v">{{ currentAlert?.processLog || '暂无处理记录' }}</span></div>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-confirm" @click="showAlertDetail = false">关闭</button>
        </div>
      </div>
    </div>

    <!-- ===== 新建工单 ===== -->
    <div v-if="showCreateTaskModal" class="modal-overlay" role="dialog" aria-modal="true" @click.self="showCreateTaskModal = false">
      <div class="modal-card">
        <div class="modal-top">
          <div class="modal-title">新建服务工单</div>
          <button type="button" class="modal-close" @click="showCreateTaskModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="f-label">选择老人</div>
            <select v-model="createTaskForm.elderId" class="wide-select">
              <option disabled value="">请选择老人</option>
              <option v-for="e in elderOptions" :key="e.elderId" :value="e.elderId">{{ e.name }}</option>
            </select>
          </div>
          <div class="form-row">
            <div class="f-label">服务类型</div>
            <select v-model="createTaskForm.serviceType" class="wide-select">
              <option value="NURSING">助餐</option>
              <option value="BATH">助浴</option>
              <option value="HOUSEKEEPING">保洁</option>
              <option value="ACCOMPANY">陪诊</option>
            </select>
          </div>
          <div class="form-row">
            <div class="f-label">预约时间</div>
            <input v-model="createTaskForm.appointmentTimeLocal" type="datetime-local" class="wide-input" />
          </div>
          <div class="form-row">
            <div class="f-label">备注</div>
            <textarea v-model="createTaskForm.notes" class="wide-textarea" placeholder="特需/注意事项..." />
          </div>
        </div>
        <div class="modal-actions">
          <button type="button" class="btn-cancel" @click="showCreateTaskModal = false">取消</button>
          <button type="button" class="btn-confirm" @click="createTaskSubmit">创建工单</button>
        </div>
      </div>
    </div>

    <!-- ===== 派单 ===== -->
    <div v-if="showAssignModal" class="modal-overlay" role="dialog" aria-modal="true" @click.self="showAssignModal = false">
      <div class="modal-card">
        <div class="modal-top">
          <div class="modal-title">智能派单</div>
          <button type="button" class="modal-close" @click="showAssignModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-row form-row-static">
            <div class="f-label">工单号</div>
            <div class="static-value">{{ selectedTask ? workOrderNo(selectedTask) : '' }}</div>
          </div>
          <div class="form-row form-row-static">
            <div class="f-label">服务类型</div>
            <div class="static-value">{{ serviceTypeLabel(selectedTask?.serviceType ?? '') }}</div>
          </div>
          <div class="form-row">
            <div class="f-label">选择服务人员</div>
            <select v-model="assignForm.workerId" class="wide-select">
              <option v-for="w in assignWorkerOptions" :key="w.id" :value="w.id">
                {{ w.name }}（{{ w.position }} - {{ w.onlineStatus === 'ONLINE' ? '在线' : '离线' }}）
              </option>
            </select>
          </div>
          <div class="reco-box">{{ assignRecoText }}</div>
        </div>
        <div class="modal-actions">
          <button type="button" class="btn-cancel" @click="showAssignModal = false">取消</button>
          <button type="button" class="btn-confirm" @click="assignSubmit">确认派单</button>
        </div>
      </div>
    </div>

    <!-- ===== 工单详情 ===== -->
    <div v-if="showTaskDetailModal" class="modal-overlay" role="dialog" aria-modal="true" @click.self="showTaskDetailModal = false">
      <div class="modal-card">
        <div class="modal-top">
          <div class="modal-title">工单详情</div>
          <button type="button" class="modal-close" @click="showTaskDetailModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="detail-grid">
            <div class="d-item"><span class="d-k">工单号</span><span class="d-v">{{ selectedTask ? workOrderNo(selectedTask) : '' }}</span></div>
            <div class="d-item"><span class="d-k">老人</span><span class="d-v">{{ elderNameMap[selectedTask?.elderId ?? ''] ?? selectedTask?.elderId }}</span></div>
            <div class="d-item"><span class="d-k">预约来源</span><span class="d-v">{{ bookedByLabel(selectedTask?.bookedByRole, selectedTask?.bookedByName) }}</span></div>
            <div class="d-item"><span class="d-k">服务类型</span><span class="d-v">{{ serviceTypeLabel(selectedTask?.serviceType ?? '') }}</span></div>
            <div class="d-item"><span class="d-k">状态</span><span class="d-v">{{ statusLabel(selectedTask?.status ?? 'NEW') }}</span></div>
            <div class="d-item"><span class="d-k">服务人员</span><span class="d-v">{{ selectedTask ? assignedStaffName(selectedTask) : '' }}</span></div>
            <div class="d-item"><span class="d-k">预约时间</span><span class="d-v">{{ selectedTask ? formatDateTime(selectedTask.appointmentTime) : '' }}</span></div>
            <div class="d-item"><span class="d-k">备注</span><span class="d-v">{{ selectedTask?.notes ?? '-' }}</span></div>
          </div>
        </div>
        <div class="modal-actions">
          <button type="button" class="btn-confirm" @click="showTaskDetailModal = false">完成</button>
        </div>
      </div>
    </div>

    <!-- ===== 服务人员 ===== -->
    <div v-if="showWorkerModal" class="modal-overlay" role="dialog" aria-modal="true" @click.self="closeWorkerModal">
      <div class="modal-card">
        <div class="modal-top">
          <div class="modal-title">{{ workerModalMode === 'add' ? '添加服务人员' : '编辑服务人员' }}</div>
          <button type="button" class="modal-close" @click="closeWorkerModal">×</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="f-label">姓名</div>
            <input v-model="workerForm.name" class="wide-input" />
          </div>
          <div class="form-row">
            <div class="f-label">岗位</div>
            <input v-model="workerForm.position" class="wide-input" />
          </div>
          <div class="form-row">
            <div class="f-label">电话</div>
            <input v-model="workerForm.phone" class="wide-input" />
          </div>
        </div>
        <div class="modal-actions">
          <button type="button" class="btn-cancel" @click="closeWorkerModal">取消</button>
          <button type="button" class="btn-confirm" @click="workerModalSubmit">{{ workerModalMode === 'add' ? '确认添加' : '确认保存' }}</button>
        </div>
      </div>
    </div>

    <!-- ===== 服务评价详情 ===== -->
    <div v-if="showEvaluationDetailModal" class="modal-overlay" role="dialog" aria-modal="true" @click.self="showEvaluationDetailModal = false">
      <div class="modal-card large">
        <div class="modal-top">
          <div class="modal-title">服务评价详情</div>
          <button type="button" class="modal-close" @click="showEvaluationDetailModal = false">×</button>
        </div>
        <div v-if="selectedEvaluation" class="modal-body">
          <div class="detail-grid">
            <div class="d-item"><span class="d-k">评价ID</span><span class="d-v mono">{{ selectedEvaluation.id }}</span></div>
            <div class="d-item"><span class="d-k">老人</span><span class="d-v">{{ selectedEvaluation.isAnonymous ? '匿名用户' : selectedEvaluation.elderName }}</span></div>
            <div class="d-item"><span class="d-k">服务人员</span><span class="d-v">{{ selectedEvaluation.workerName }}</span></div>
            <div class="d-item"><span class="d-k">服务类型</span><span class="d-v">{{ serviceTypeLabel(selectedEvaluation.serviceType) }}</span></div>
            <div class="d-item"><span class="d-k">关联工单</span><span class="d-v mono">{{ evaluationTaskLabel(selectedEvaluation) }}</span></div>
            <div class="d-item"><span class="d-k">服务时长</span><span class="d-v">{{ selectedEvaluation.serviceDurationMinutes ? `${selectedEvaluation.serviceDurationMinutes} 分钟` : '—' }}</span></div>
            <div class="d-item"><span class="d-k">评价时间</span><span class="d-v">{{ formatDateTime(selectedEvaluation.createdAt) }}</span></div>
            <div class="d-item d-item-full"><span class="d-k">总体评分</span>
              <span class="d-v">
                <span class="stars">{{ '★'.repeat(selectedEvaluation.rating) }}{{ '☆'.repeat(5 - selectedEvaluation.rating) }}</span>
                {{ selectedEvaluation.rating }} 分
              </span>
            </div>
          </div>
          <div class="eval-dimension-panel">
            <h4 class="eval-section-title">维度评分</h4>
            <div class="eval-dimension-grid">
              <div v-for="dim in evaluationDimensionRows(selectedEvaluation)" :key="dim.key" class="eval-dimension-item">
                <span class="eval-dim-label">{{ dim.label }}</span>
                <span class="stars">{{ '★'.repeat(dim.value) }}{{ '☆'.repeat(5 - dim.value) }}</span>
                <span class="rating-num">{{ dim.value }} 分</span>
              </div>
              <div v-if="evaluationDimensionRows(selectedEvaluation).length === 0" class="empty-inline">暂无维度评分</div>
            </div>
          </div>
          <div v-if="selectedEvaluation.tags && selectedEvaluation.tags.length" class="eval-tags-block">
            <h4 class="eval-section-title">评价标签</h4>
            <div class="tags-container">
              <span v-for="tag in selectedEvaluation.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>
          </div>
          <div class="eval-comment-block">
            <h4 class="eval-section-title">文字评价</h4>
            <p class="eval-comment-text">{{ selectedEvaluation.comment || '—' }}</p>
          </div>
        </div>
        <div class="modal-actions">
          <button type="button" class="btn-confirm" @click="showEvaluationDetailModal = false">关闭</button>
        </div>
      </div>
    </div>

    <!-- ===== 通用录入 ===== -->
    <div v-if="showPromptModal" class="modal-overlay" role="dialog" aria-modal="true" @click.self="showPromptModal = false">
      <div class="modal-card">
        <div class="modal-top">
          <div class="modal-title">{{ promptTitle }}</div>
          <button type="button" class="modal-close" @click="showPromptModal = false">×</button>
        </div>
        <div class="modal-body">
          <div v-for="f in promptFields" :key="f.key" class="form-row">
            <div class="f-label">{{ f.label }}</div>
            <input v-model="promptValues[f.key]" class="wide-input" :placeholder="f.placeholder" />
          </div>
        </div>
        <div class="modal-actions">
          <button type="button" class="btn-cancel" @click="showPromptModal = false">取消</button>
          <button type="button" class="btn-confirm" @click="submitPromptModal">确定</button>
        </div>
      </div>
    </div>

    <!-- ===== Toast ===== -->
    <div v-if="toast" class="toast">
      <div class="toast-left">{{ toast.icon }}</div>
      <div class="toast-right">
        <div class="toast-title">{{ toast.title }}</div>
        <div class="toast-desc">{{ toast.desc }}</div>
      </div>
      <button class="toast-close" @click="toast = null">×</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from "vue";
import type {
  AgencyActivityDto,
  AgencyDeviceDto,
  AgencyEvaluationDto,
  AgencyFinanceDto,
  AgencyNotificationDto,
  AgencyScheduleDto,
  DispatchTaskDto,
  ServiceWorkerDto
} from "../../api/agency";
import {
  activityCheckin,
  assignWorker,
  createActivity,
  createDevice,
  createFinance,
  createNotification,
  createSchedule,
  createTask as createDispatchTask,
  deleteDevice as apiDeleteDevice,
  deleteNotification as apiDeleteNotification,
  deleteSchedule,
  deleteWorker as apiDeleteWorker,
  listActivities,
  listAlerts,
  listDevices,
  listEvaluations,
  listFinance,
  listHealthRecords,
  listNotifications,
  listSchedules,
  fetchAgencyDashboardStats,
  listTasks,
  listWorkers,
  type AgencyDashboardStatsDto,
  addWorker,
  updateWorker,
  markFinancePaid,
  patchDeviceElder,
  processAgencyAlert,
  updateNotification,
  updateTaskStatus,
  bookedByLabel,
  formatAgencyApiError
} from "../../api/agency";
import { listElders } from "../../api/elder";
import type { ElderProfileDto } from "../../api/elder";
import { deepSeekChat, type ChatTurn } from "../../api/deepseek";

type TabKey =
  | "dispatch"
  | "workers"
  | "schedule"
  | "evaluations"
  | "health"
  | "alerts"
  | "activities"
  | "finance"
  | "notifications"
  | "devices";

type AgencyAlertItem = {
  id: string;
  elderName: string;
  type: string;
  triggeredAt: string;
  status: "PENDING" | "PROCESSING" | "RESOLVED";
  processLog: string;
  source: "ALARM" | "ANOMALY";
};

type HealthRecord = {
  elderId: string;
  name: string;
  age: number | null;
  gender: string;
  healthStatus: string;
  heartRate: number | null;
  bloodPressure: string;
  bloodOxygen: number | null;
  lastUpdated: string;
};

type ChatMessage = { id: string; role: "user" | "assistant"; text: string };

type PromptField = { key: string; label: string; placeholder?: string };

const activeTab = ref<TabKey>("dispatch");
const isScrolled = ref(false);
const showAssistant = ref(false);
const showBackTop = ref(false);
const aiBusy = ref(false);
const aiDraft = ref("");

const tabs = [
  { key: "dispatch" as TabKey, icon: "", label: "工单派单" },
  { key: "workers" as TabKey, icon: "", label: "服务人员" },
  { key: "schedule" as TabKey, icon: "", label: "排班管理" },
  { key: "evaluations" as TabKey, icon: "", label: "服务评价" },
  { key: "health" as TabKey, icon: "", label: "健康档案" },
  { key: "alerts" as TabKey, icon: "", label: "告警管理" },
  { key: "activities" as TabKey, icon: "", label: "活动管理" },
  { key: "finance" as TabKey, icon: "", label: "收费管理" },
  { key: "notifications" as TabKey, icon: "", label: "消息通知" },
  { key: "devices" as TabKey, icon: "", label: "设备管理" }
];

const chatMessages = ref<ChatMessage[]>([
  { id: "welcome", role: "assistant", text: "您好，我是银发智盾智慧助手。在下方输入问题即可获得解答。" }
]);
const quickPrompts = ["如何查看工单？", "添加服务人员", "发布活动", "查看健康档案"];

const tasks = ref<DispatchTaskDto[]>([]);
const workers = ref<ServiceWorkerDto[]>([]);
const elderList = ref<ElderProfileDto[]>([]);
const elderNameMap = reactive<Record<string, string>>({});

const activities = ref<AgencyActivityDto[]>([]);
const financeRecords = ref<AgencyFinanceDto[]>([]);
const notifications = ref<AgencyNotificationDto[]>([]);
const devices = ref<AgencyDeviceDto[]>([]);
const schedules = ref<AgencyScheduleDto[]>([]);
const evaluations = ref<AgencyEvaluationDto[]>([]);
const healthRecordsList = ref<HealthRecord[]>([]);
const alerts = ref<AgencyAlertItem[]>([]);

const defaultDashboardStats = (): AgencyDashboardStatsDto => ({
  totalElders: 0,
  agencyCount: 1,
  todayOrders: 0,
  pendingOrders: 0,
  completedTotal: 0,
  completionRatePercent: 0,
  onlineWorkers: 0,
  totalWorkers: 0
});

const dashboardStats = ref<AgencyDashboardStatsDto>(defaultDashboardStats());

/** 与后端 /dashboard/stats 相同口径的本地回退计算（累计） */
function applyDashboardStatsFromLocal() {
  const now = new Date();
  const y = now.getFullYear();
  const m = now.getMonth();
  const d = now.getDate();
  const todayCount = tasks.value.filter(t => {
    const dt = new Date(t.createdAt || t.appointmentTime);
    return dt.getFullYear() === y && dt.getMonth() === m && dt.getDate() === d;
  }).length;
  const pending = tasks.value.filter(t =>
    ["NEW", "ASSIGNED", "ARRIVING", "IN_PROGRESS"].includes(t.status)
  ).length;
  const completedTotal = tasks.value.filter(t => t.status === "COMPLETED").length;
  const activeOrders = tasks.value.filter(t => t.status !== "CANCELLED").length;
  const rate =
    activeOrders === 0 ? 0 : Math.min(100, Math.round((completedTotal * 100) / activeOrders));
  dashboardStats.value = {
    totalElders: Math.max(elderList.value.length, new Set(tasks.value.map(t => t.elderId)).size),
    agencyCount: dashboardStats.value.agencyCount || 1,
    todayOrders: todayCount,
    pendingOrders: pending,
    completedTotal,
    completionRatePercent: rate,
    onlineWorkers: workers.value.filter(w => w.onlineStatus === "ONLINE").length,
    totalWorkers: workers.value.length
  };
}

const orderTrendValues = computed(() => {
  const vals: number[] = [];
  const now = new Date();
  for (let i = 6; i >= 0; i--) {
    const d = new Date(now);
    d.setDate(d.getDate() - i);
    const y = d.getFullYear();
    const m = d.getMonth();
    const day = d.getDate();
    vals.push(
      tasks.value.filter(t => {
        const created = new Date(t.createdAt || t.appointmentTime);
        return created.getFullYear() === y && created.getMonth() === m && created.getDate() === day;
      }).length
    );
  }
  return vals;
});

const orderTrendLabels = computed(() => {
  const labels: string[] = [];
  const now = new Date();
  for (let i = 6; i >= 0; i--) {
    const d = new Date(now);
    d.setDate(d.getDate() - i);
    labels.push(`${d.getMonth() + 1}/${d.getDate()}`);
  }
  return labels;
});

const orderTrendPoints = computed(() =>
  orderTrendValues.value.map((v, i) => `${40 + i * 52},${getYPosition(v)}`).join(" ")
);

const areaPoints = computed(() => {
  const points = orderTrendValues.value.map((v, i) => `${40 + i * 52},${getYPosition(v)}`).join(" ");
  return `${40},140 ${points} ${40 + 6 * 52},140`;
});

function getYPosition(v: number) {
  const max = Math.max(...orderTrendValues.value, 1);
  return 125 - (v / max) * 100;
}

const healthRecords = computed(() => healthRecordsList.value);

const healthSearch = ref("");
const healthFilter = ref("all");

const filteredHealthRecords = computed(() => {
  let list = healthRecords.value;
  if (healthFilter.value === "normal") list = list.filter(e => e.healthStatus === "正常");
  else if (healthFilter.value === "attention") list = list.filter(e => e.healthStatus === "需关注");
  else if (healthFilter.value === "critical") list = list.filter(e => e.healthStatus === "异常");
  const kw = healthSearch.value.trim().toLowerCase();
  if (kw) list = list.filter(e => e.name.toLowerCase().includes(kw));
  return list;
});

function getHealthStatusClass(status: string) {
  if (status === "需关注") return "pending";
  if (status === "异常") return "danger";
  return "done";
}

const alertSearch = ref("");
const alertFilter = ref("all");
const alertBannerDismissed = ref(false);

const pendingAlerts = computed(() => alerts.value.filter(a => a.status === "PENDING"));
const pendingSosAlerts = computed(() => pendingAlerts.value.filter(a => a.type === "SOS"));

const knownPendingSosIds = ref<Set<string>>(new Set());

const filteredAlerts = computed(() => {
  let list = alerts.value;
  if (alertFilter.value !== "all") {
    const want = alertFilter.value.toUpperCase();
    list = list.filter(a => a.status === want);
  }
  const kw = alertSearch.value.trim().toLowerCase();
  if (kw) list = list.filter(a => a.elderName.toLowerCase().includes(kw) || a.type.toLowerCase().includes(kw));
  return list;
});

function mapAnomalyType(t: string): string {
  const u = (t || "").toUpperCase();
  if (u.includes("跌倒") || u.includes("FALL")) return "FALL";
  if (u.includes("SOS") || u.includes("求助")) return "SOS";
  if (u.includes("体征") || u.includes("VITAL")) return "VITALS";
  if (u.includes("电")) return "BATTERY";
  return u || "VITALS";
}

function syncAlertsFromApi(rows: Awaited<ReturnType<typeof listAlerts>>) {
  alerts.value = rows.map(a => ({
    id: a.id,
    elderName: a.elderName,
    type: a.type,
    triggeredAt: a.triggeredAt,
    status: a.status,
    processLog: a.processLog ?? "",
    source: a.source
  }));
}

function getAlertTypeLabel(type?: string) {
  const map: Record<string, string> = {
    SOS: "SOS求助",
    FALL: "跌倒预警",
    VITALS: "体征异常",
    BATTERY: "电量不足"
  };
  return type ? map[type] ?? type : "";
}

function getAlertTypeClass(type: string) {
  const map: Record<string, string> = {
    SOS: "alert-sos",
    FALL: "alert-fall",
    VITALS: "alert-vitals",
    BATTERY: "alert-battery"
  };
  return map[type] || "";
}

function getAlertStatusLabel(status: string) {
  const map: Record<string, string> = {
    PENDING: "待处理",
    PROCESSING: "处理中",
    RESOLVED: "已处理"
  };
  return map[status] || status;
}

function getAlertStatusClass(status: string) {
  const map: Record<string, string> = {
    PENDING: "pending",
    PROCESSING: "doing",
    RESOLVED: "done"
  };
  return map[status] || "";
}

function dismissAlertBanner() {
  alertBannerDismissed.value = true;
}

function openAlertList() {
  navToTab("alerts");
}

const activitySearch = ref("");
const filteredActivities = computed(() => {
  const kw = activitySearch.value.trim().toLowerCase();
  if (!kw) return activities.value;
  return activities.value.filter(a => a.title.toLowerCase().includes(kw));
});

const financeSearch = ref("");
const filteredFinanceRecords = computed(() => {
  const kw = financeSearch.value.trim().toLowerCase();
  if (!kw) return financeRecords.value;
  return financeRecords.value.filter(f => f.elderName.toLowerCase().includes(kw));
});

const totalIncome = computed(() =>
  financeRecords.value.reduce((acc, f) => (f.status === "paid" ? acc + f.amount : acc), 0).toFixed(2)
);

const pendingPayments = computed(() => financeRecords.value.filter(f => f.status === "pending").length);

const notificationSearch = ref("");
const filteredNotifications = computed(() => {
  const kw = notificationSearch.value.trim().toLowerCase();
  if (!kw) return notifications.value;
  return notifications.value.filter(n => n.title.toLowerCase().includes(kw));
});

const deviceSearch = ref("");
const filteredDevices = computed(() => {
  const kw = deviceSearch.value.trim().toLowerCase();
  if (!kw) return devices.value;
  return devices.value.filter(
    d => d.id.toLowerCase().includes(kw) || d.name.toLowerCase().includes(kw) || (d.elderName?.toLowerCase().includes(kw) ?? false)
  );
});

const weekDays = ["周一", "周二", "周三", "周四", "周五", "周六", "周日"];
const workHours = Array.from({ length: 12 }, (_, i) => i + 8);
const currentWeekStart = ref(new Date());

const currentWeekLabel = computed(() => {
  const start = currentWeekStart.value;
  const end = new Date(start);
  end.setDate(end.getDate() + 6);
  return `${start.getMonth() + 1}月${start.getDate()}日 - ${end.getMonth() + 1}月${end.getDate()}日`;
});

function prevWeek() {
  const d = new Date(currentWeekStart.value);
  d.setDate(d.getDate() - 7);
  currentWeekStart.value = d;
}

function nextWeek() {
  const d = new Date(currentWeekStart.value);
  d.setDate(d.getDate() + 7);
  currentWeekStart.value = d;
}

function getShifts(day: string, hour: number) {
  return schedules.value.filter(s => s.day === day && s.hour === hour);
}

async function deleteShift(id: string) {
  if (!confirm("确定删除该排班吗？")) return;
  try {
    await deleteSchedule(id);
    await refreshSchedules();
    showToast({ icon: "✓", title: "已删除", desc: "排班已移除" });
  } catch (e: unknown) {
    showToast({ icon: "!", title: "删除失败", desc: formatAgencyApiError(e) });
  }
}

const evaluationSearch = ref("");
const evaluationFilter = ref("all");
const evaluationServiceTypeFilter = ref("all");
const showEvaluationDetailModal = ref(false);
const selectedEvaluation = ref<AgencyEvaluationDto | null>(null);

const EVALUATION_DIMENSIONS: { key: keyof AgencyEvaluationDto; label: string }[] = [
  { key: "attitudeRating", label: "态度" },
  { key: "skillRating", label: "技能" },
  { key: "responseRating", label: "响应" },
  { key: "punctualityRating", label: "准时" },
  { key: "communicationRating", label: "沟通" }
];

function evaluationDimensionRows(e: AgencyEvaluationDto) {
  return EVALUATION_DIMENSIONS.flatMap(dim => {
    const raw = e[dim.key];
    const value = typeof raw === "number" ? raw : undefined;
    if (value == null || value < 1) return [];
    return [{ key: dim.key, label: dim.label, value }];
  });
}

function evaluationTaskLabel(e: AgencyEvaluationDto) {
  if (e.taskIdLabel) return e.taskIdLabel;
  if (!e.taskId) return "—";
  const tid = e.taskId.replace(/-/g, "");
  return "WO" + tid.slice(0, 6).toUpperCase();
}

function avgEvaluationDimension(getter: (e: AgencyEvaluationDto) => number | undefined) {
  const vals = evaluations.value
      .map(getter)
      .filter((v): v is number => v != null && v > 0);
  if (vals.length === 0) return "—";
  return (vals.reduce((a, b) => a + b, 0) / vals.length).toFixed(1) + "分";
}

const filteredEvaluations = computed(() => {
  let list = evaluations.value;
  if (evaluationFilter.value === "excellent") list = list.filter(e => e.rating >= 5);
  else if (evaluationFilter.value === "good") list = list.filter(e => e.rating === 4);
  else if (evaluationFilter.value === "normal") list = list.filter(e => e.rating === 3);
  else if (evaluationFilter.value === "poor") list = list.filter(e => e.rating <= 2);
  if (evaluationServiceTypeFilter.value !== "all") {
    list = list.filter(e => e.serviceType === evaluationServiceTypeFilter.value);
  }
  const kw = evaluationSearch.value.trim().toLowerCase();
  if (kw) {
    list = list.filter(e => {
      const elder = e.isAnonymous ? "匿名" : e.elderName;
      const task = evaluationTaskLabel(e).toLowerCase();
      return (
          elder.toLowerCase().includes(kw) ||
          e.workerName.toLowerCase().includes(kw) ||
          (e.taskId ?? "").toLowerCase().includes(kw) ||
          task.includes(kw) ||
          (e.comment ?? "").toLowerCase().includes(kw) ||
          (e.tags ?? []).some(t => t.toLowerCase().includes(kw))
      );
    });
  }
  return list;
});

const totalEvaluations = computed(() => evaluations.value.length);
const averageRating = computed(() => {
  if (evaluations.value.length === 0) return "0";
  const sum = evaluations.value.reduce((acc, e) => acc + e.rating, 0);
  return (sum / evaluations.value.length).toFixed(1);
});
const satisfactionRate = computed(() => {
  if (evaluations.value.length === 0) return 0;
  const satisfied = evaluations.value.filter(e => e.rating >= 4).length;
  return Math.round((satisfied / evaluations.value.length) * 100);
});
const avgAttitudeRating = computed(() => avgEvaluationDimension(e => e.attitudeRating));
const avgSkillRating = computed(() => avgEvaluationDimension(e => e.skillRating));
const avgPunctualityRating = computed(() => avgEvaluationDimension(e => e.punctualityRating));

function viewEvaluationDetail(e: AgencyEvaluationDto) {
  selectedEvaluation.value = e;
  showEvaluationDetailModal.value = true;
}

function viewFullComment(e: AgencyEvaluationDto) {
  viewEvaluationDetail(e);
}

const taskSearch = ref("");
const filteredTasks = computed(() => {
  const k = taskSearch.value.trim();
  if (!k) return tasks.value;
  const kw = k.toLowerCase();
  return tasks.value.filter(t => {
    const elderName = elderNameMap[t.elderId] ?? t.elderId;
    const assigned = t.assignedWorkerId ? workers.value.find(w => w.id === t.assignedWorkerId)?.name ?? "" : "";
    const wo = workOrderNo(t).toLowerCase();
    return (
      wo.includes(kw) ||
      t.id.toLowerCase().includes(kw) ||
      t.elderId.toLowerCase().includes(kw) ||
      elderName.toLowerCase().includes(kw) ||
      t.serviceType.toLowerCase().includes(kw) ||
      assigned.toLowerCase().includes(kw)
    );
  });
});

function workOrderNo(t: DispatchTaskDto): string {
  return "WO" + t.id.replace(/-/g, "").slice(0, 6).toUpperCase();
}

function serviceTypeLabel(st: string) {
  const m: Record<string, string> = {
    NURSING: "助餐",
    BATH: "助浴",
    HOUSEKEEPING: "保洁",
    ACCOMPANY: "陪诊"
  };
  return m[st] ?? st;
}

function statusLabel(st?: string) {
  if (!st) return "";
  switch (st) {
    case "NEW":
      return "待派单";
    case "ASSIGNED":
      return "派单中";
    case "ARRIVING":
      return "到达中";
    case "IN_PROGRESS":
      return "服务中";
    case "COMPLETED":
      return "已完成";
    case "CANCELLED":
      return "已取消";
    default:
      return st;
  }
}

function statusClass(st: DispatchTaskDto["status"]) {
  if (st === "NEW") return "pending";
  if (st === "IN_PROGRESS" || st === "ASSIGNED" || st === "ARRIVING") return "doing";
  if (st === "COMPLETED") return "done";
  return "pending";
}

function assignedStaffName(t: DispatchTaskDto) {
  if (!t.assignedWorkerId) return "—";
  return workers.value.find(w => w.id === t.assignedWorkerId)?.name ?? "—";
}

const workerSearch = ref("");
const filteredWorkers = computed(() => {
  const k = workerSearch.value.trim();
  if (!k) return workers.value;
  const kw = k.toLowerCase();
  return workers.value.filter(
    w => w.name.toLowerCase().includes(kw) || (w.position ?? "").toLowerCase().includes(kw) || (w.phone ?? "").toLowerCase().includes(kw)
  );
});

function formatDateTime(isoOrTs: string) {
  if (!isoOrTs) return "";
  const d = new Date(isoOrTs);
  if (Number.isNaN(d.getTime())) return String(isoOrTs);
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  const hh = String(d.getHours()).padStart(2, "0");
  const mi = String(d.getMinutes()).padStart(2, "0");
  const ss = String(d.getSeconds()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}:${ss}`;
}

function toLocalDateTimeLocal(d: Date) {
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

const showReportModal = ref(false);
const monthlyOrderData = computed(() => {
  const data: number[] = [];
  const now = new Date();
  for (let i = 29; i >= 0; i--) {
    const d = new Date(now);
    d.setDate(d.getDate() - i);
    const y = d.getFullYear();
    const m = d.getMonth();
    const day = d.getDate();
    data.push(
      tasks.value.filter(t => {
        const created = new Date(t.createdAt || t.appointmentTime);
        return created.getFullYear() === y && created.getMonth() === m && created.getDate() === day;
      }).length
    );
  }
  return data;
});
const maxMonthlyOrders = computed(() => Math.max(...monthlyOrderData.value, 1));
const workerPerformance = computed(() =>
  workers.value.slice(0, 5).map((w, i) => ({
    id: w.id,
    name: w.name,
    score: 88 + ((w.name.codePointAt(0) ?? 0) + i * 7) % 10
  }))
);

const showHealthDetail = ref(false);
const currentHealthElder = ref<HealthRecord | null>(null);
const showAlertDetail = ref(false);
const currentAlert = ref<AgencyAlertItem | null>(null);
const showCreateTaskModal = ref(false);
const showAssignModal = ref(false);
const showTaskDetailModal = ref(false);
const showWorkerModal = ref(false);
const workerModalMode = ref<"add" | "edit">("add");
const selectedTask = ref<DispatchTaskDto | null>(null);

const showPromptModal = ref(false);
const promptTitle = ref("");
const promptFields = ref<PromptField[]>([]);
const promptValues = reactive<Record<string, string>>({});
let promptSubmitHandler: ((values: Record<string, string>) => void) | null = null;

const createTaskForm = reactive({
  elderId: "",
  serviceType: "NURSING",
  appointmentTimeLocal: toLocalDateTimeLocal(new Date(Date.now() + 3600000)),
  notes: ""
});

const assignForm = reactive({ workerId: "" });
const workerForm = reactive({ id: "", name: "", position: "", phone: "" });

const elderOptions = computed(() => elderList.value);

const assignWorkerOptions = computed(() => {
  if (!selectedTask.value) return [];
  const st = selectedTask.value.serviceType;
  return workers.value.filter(w => {
    if (w.onlineStatus !== "ONLINE") return false;
    if (w.serviceType === st) return true;
    if (st === "BATH" && w.serviceType === "NURSING") return true;
    return false;
  });
});

const assignRecoText = computed(() => {
  const w = workers.value.find(x => x.id === assignForm.workerId);
  if (!w) return "请选择服务人员后查看智能推荐说明。";
  const dist = 600 + ((w.id.length * 37) % 500);
  const rate = 95 + ((w.name.codePointAt(0) ?? 0) % 4);
  return `系统根据距离&评价推荐：${w.name}（距养老院约${dist}m，好评率${rate}%）`;
});

const toast = ref<{ icon: string; title: string; desc: string } | null>(null);
let toastTimer: number | undefined;

function showToast(payload: { icon: string; title: string; desc: string }) {
  toast.value = payload;
  if (toastTimer !== undefined) window.clearTimeout(toastTimer);
  toastTimer = window.setTimeout(() => {
    toast.value = null;
    toastTimer = undefined;
  }, 3000);
}

function toastDemo(msg: string) {
  showToast({ icon: "ℹ", title: "提示", desc: msg });
}

function openPrompt(title: string, fields: PromptField[], onSubmit: (values: Record<string, string>) => void) {
  promptTitle.value = title;
  promptFields.value = fields;
  for (const k of Object.keys(promptValues)) delete promptValues[k];
  for (const f of fields) promptValues[f.key] = "";
  promptSubmitHandler = onSubmit;
  showPromptModal.value = true;
}

function submitPromptModal() {
  const handler = promptSubmitHandler;
  if (!handler) return;
  const snapshot: Record<string, string> = {};
  for (const f of promptFields.value) snapshot[f.key] = (promptValues[f.key] ?? "").trim();
  showPromptModal.value = false;
  promptSubmitHandler = null;
  handler(snapshot);
}

function navToTab(key: TabKey) {
  activeTab.value = key;
  if (key === "alerts") void refreshAlerts();
  // nextTick(() => {
  //   document.getElementById("dashboard")?.scrollIntoView({ behavior: "smooth", block: "start" });
  // });
}

function switchTab(key: TabKey) {
  navToTab(key);
  if (key === "dispatch") {
    void refreshTasks();
  }
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: "smooth" });
}

function toggleAssistant() {
  showAssistant.value = !showAssistant.value;
}

async function sendAiMessage() {
  const text = aiDraft.value.trim();
  if (!text || aiBusy.value) return;
  chatMessages.value.push({ id: `m-${Date.now()}`, role: "user", text });
  aiDraft.value = "";
  aiBusy.value = true;
  try {
    const turns: ChatTurn[] = chatMessages.value.map(m => ({ role: m.role, text: m.text }));
    const reply = await deepSeekChat(turns);
    chatMessages.value.push({ id: `m-${Date.now() + 1}`, role: "assistant", text: reply });
  } catch (e: unknown) {
    chatMessages.value.push({
      id: `m-${Date.now() + 1}`,
      role: "assistant",
      text: e instanceof Error ? e.message : "助手暂时不可用，请稍后再试。"
    });
  } finally {
    aiBusy.value = false;
  }
}

function applyQuickPrompt(p: string) {
  aiDraft.value = p;
  void sendAiMessage();
}

function openReportModal() {
  showReportModal.value = true;
}

function exportReport() {
  showReportModal.value = false;
  showToast({ icon: "✓", title: "报表已生成", desc: "演示环境：报表数据已汇总，可对接 PDF 导出。" });
}

function openHealthDetail(e: HealthRecord) {
  currentHealthElder.value = e;
  showHealthDetail.value = true;
}

function openAlertDetail(a: AgencyAlertItem) {
  currentAlert.value = a;
  showAlertDetail.value = true;
}

async function processAlert(id: string) {
  const item = alerts.value.find(a => a.id === id);
  if (!item) return;
  try {
    await processAgencyAlert(id, item.source);
    await refreshAlerts();
    showToast({ icon: "✓", title: "告警已处理", desc: "该告警已标记为已处理。" });
  } catch (e: unknown) {
    showToast({ icon: "!", title: "处理失败", desc: formatAgencyApiError(e) });
  }
}

function openAddScheduleModal() {
  openPrompt(
    "添加排班",
    [
      { key: "day", label: "星期", placeholder: "周一" },
      { key: "hour", label: "小时(8-19)", placeholder: "9" },
      { key: "workerName", label: "服务人员", placeholder: "王阿姨" },
      { key: "type", label: "班次(morning/afternoon/night)", placeholder: "morning" }
    ],
    async values => {
      const hour = Number(values.hour);
      const type = (values.type || "morning") as AgencyScheduleDto["type"];
      if (!values.day || !values.workerName || Number.isNaN(hour)) {
        showToast({ icon: "!", title: "请填写完整", desc: "星期、小时、人员为必填项" });
        return;
      }
      try {
        await createSchedule({
          day: values.day,
          hour,
          workerName: values.workerName,
          type: type === "afternoon" || type === "night" ? type : "morning"
        });
        await refreshSchedules();
        showToast({ icon: "✓", title: "排班已添加", desc: `${values.day} ${hour}:00` });
      } catch (e: unknown) {
        showToast({ icon: "!", title: "添加失败", desc: formatAgencyApiError(e) });
      }
    }
  );
}

function openAddActivityModal() {
  openPrompt(
    "发布活动",
    [
      { key: "title", label: "活动名称" },
      { key: "tag", label: "活动类型(首页标签)", placeholder: "文娱活动" },
      { key: "location", label: "地点" },
      { key: "maxParticipants", label: "名额", placeholder: "50" }
    ],
    async values => {
      if (!values.title) return;
      const now = new Date();
      const end = new Date(now.getTime() + 7200000);
      try {
        await createActivity({
          title: values.title,
          startTime: now.toISOString(),
          endTime: end.toISOString(),
          location: values.location || "活动中心",
          maxParticipants: Number(values.maxParticipants) || 30,
          description: "",
          tag: values.tag || "文娱活动",
          icon: "🎨"
        });
        await refreshActivities();
        showToast({ icon: "✓", title: "活动已发布", desc: values.title });
      } catch (e: unknown) {
        showToast({ icon: "!", title: "发布失败", desc: formatAgencyApiError(e) });
      }
    }
  );
}

function openActivityDetail(a: AgencyActivityDto) {
  showToast({ icon: "📋", title: a.title, desc: `${a.location} · ${a.description || "暂无描述"}` });
}

function openActivityRegistration(a: AgencyActivityDto) {
  showToast({ icon: "👥", title: "报名管理", desc: `${a.title}：已报名 ${a.registered}/${a.maxParticipants}` });
}

async function openActivityCheckin(a: AgencyActivityDto) {
  try {
    await activityCheckin(a.id);
    await refreshActivities();
    showToast({ icon: "✓", title: "签到成功", desc: `${a.title} 签到 +1` });
  } catch (e: unknown) {
    showToast({ icon: "!", title: "签到失败", desc: formatAgencyApiError(e) });
  }
}

function openAddFinanceModal() {
  openPrompt(
    "新增收费",
    [
      { key: "elderName", label: "老人姓名" },
      { key: "serviceType", label: "服务类型(NURSING等)" },
      { key: "amount", label: "金额", placeholder: "100" }
    ],
    async values => {
      if (!values.elderName || !values.amount) return;
      try {
        await createFinance({
          elderName: values.elderName,
          serviceType: values.serviceType || "NURSING",
          amount: Number(values.amount) || 0,
          status: "pending"
        });
        await refreshFinance();
        showToast({ icon: "✓", title: "收费已录入", desc: values.elderName });
      } catch (e: unknown) {
        showToast({ icon: "!", title: "录入失败", desc: formatAgencyApiError(e) });
      }
    }
  );
}

function openFinanceDetail(f: AgencyFinanceDto) {
  showToast({
    icon: "💰",
    title: `${f.elderName} · ${serviceTypeLabel(f.serviceType)}`,
    desc: `¥${f.amount} · ${f.status === "paid" ? "已支付" : "待支付"}`
  });
}

async function markAsPaid(id: string) {
  const record = financeRecords.value.find(f => f.id === id);
  if (!record) return;
  try {
    await markFinancePaid(id);
    await refreshFinance();
    showToast({ icon: "✓", title: "已标记支付", desc: record.elderName });
  } catch (e: unknown) {
    showToast({ icon: "!", title: "操作失败", desc: formatAgencyApiError(e) });
  }
}

function openAddNotificationModal() {
  openPrompt(
    "发布通知",
    [
      { key: "title", label: "标题" },
      { key: "content", label: "内容" }
    ],
    async values => {
      if (!values.title) return;
      try {
        await createNotification({
          title: values.title,
          content: values.content || "",
          status: "published",
          author: "管理员"
        });
        await refreshNotifications();
        showToast({ icon: "✓", title: "通知已发布", desc: values.title });
      } catch (e: unknown) {
        showToast({ icon: "!", title: "发布失败", desc: formatAgencyApiError(e) });
      }
    }
  );
}

function openNotificationDetail(n: AgencyNotificationDto) {
  showToast({ icon: "📨", title: n.title, desc: n.content });
}

function editNotification(n: AgencyNotificationDto) {
  openPrompt(
    "编辑通知",
    [
      { key: "title", label: "标题" },
      { key: "content", label: "内容" }
    ],
    async values => {
      try {
        await updateNotification(n.id, {
          title: values.title || undefined,
          content: values.content || undefined
        });
        await refreshNotifications();
        showToast({ icon: "✓", title: "已保存", desc: values.title || n.title });
      } catch (e: unknown) {
        showToast({ icon: "!", title: "保存失败", desc: formatAgencyApiError(e) });
      }
    }
  );
}

async function deleteNotificationHandler(id: string) {
  if (!confirm("确定删除该通知吗？")) return;
  try {
    await apiDeleteNotification(id);
    await refreshNotifications();
    showToast({ icon: "✓", title: "已删除", desc: "通知已移除" });
  } catch (e: unknown) {
    showToast({ icon: "!", title: "删除失败", desc: formatAgencyApiError(e) });
  }
}

function openAddDeviceModal() {
  openPrompt(
    "添加设备",
    [
      { key: "name", label: "设备名称" },
      { key: "type", label: "类型", placeholder: "智能手环" }
    ],
    async values => {
      if (!values.name) return;
      try {
        await createDevice({
          name: values.name,
          type: values.type || "智能设备",
          battery: 100,
          status: "online"
        });
        await refreshDevices();
        showToast({ icon: "✓", title: "设备已添加", desc: values.name });
      } catch (e: unknown) {
        showToast({ icon: "!", title: "添加失败", desc: formatAgencyApiError(e) });
      }
    }
  );
}

function openDeviceDetail(d: AgencyDeviceDto) {
  showToast({ icon: "📱", title: d.name, desc: `${d.type} · 电量 ${d.battery}%` });
}

async function bindDevice(d: AgencyDeviceDto) {
  try {
    if (d.elderName) {
      await patchDeviceElder(d.id, null);
      showToast({ icon: "🔗", title: "已解绑", desc: d.name });
    } else {
      const name = window.prompt("绑定老人姓名：", elderList.value[0]?.name ?? "");
      if (name) await patchDeviceElder(d.id, name);
    }
    await refreshDevices();
  } catch (e: unknown) {
    showToast({ icon: "!", title: "操作失败", desc: formatAgencyApiError(e) });
  }
}

async function deleteDeviceHandler(id: string) {
  if (!confirm("确定删除该设备吗？")) return;
  try {
    await apiDeleteDevice(id);
    await refreshDevices();
    showToast({ icon: "✓", title: "已删除", desc: "设备已移除" });
  } catch (e: unknown) {
    showToast({ icon: "!", title: "删除失败", desc: formatAgencyApiError(e) });
  }
}

function openCreateTaskModal() {
  showCreateTaskModal.value = true;
  if (!createTaskForm.elderId && elderList.value.length > 0) {
    createTaskForm.elderId = elderList.value[0].elderId;
  }
}

function parseLocalDateTimeToMillis(local: string) {
  return new Date(local).getTime();
}

async function createTaskSubmit() {
  if (!createTaskForm.elderId) {
    showToast({ icon: "!", title: "请选择老人", desc: "创建工单前需选择服务对象" });
    return;
  }
  try {
    const id = await createDispatchTask({
      elderId: createTaskForm.elderId,
      serviceType: createTaskForm.serviceType,
      appointmentTimeMillis: parseLocalDateTimeToMillis(createTaskForm.appointmentTimeLocal),
      notes: createTaskForm.notes
    });
    showCreateTaskModal.value = false;
    showToast({ icon: "✓", title: "创建成功", desc: `工单 ${id.slice(0, 8)} 已提交` });
    await refreshTasks();
    await refreshDashboardStats();
  } catch (e: unknown) {
    showToast({ icon: "!", title: "创建失败", desc: formatAgencyApiError(e) });
  }
}

function openAssignModal(t: DispatchTaskDto) {
  selectedTask.value = t;
  showAssignModal.value = true;
  assignForm.workerId = assignWorkerOptions.value[0]?.id ?? "";
}

async function assignSubmit() {
  if (!selectedTask.value || !assignForm.workerId) {
    showToast({ icon: "!", title: "请选择服务人员", desc: "派单前需指定服务人员" });
    return;
  }
  try {
    await assignWorker(selectedTask.value.id, assignForm.workerId);
    showAssignModal.value = false;
    showToast({ icon: "✓", title: "派单成功", desc: "工单已指派" });
    await refreshTasks();
    await refreshDashboardStats();
  } catch (e: unknown) {
    showToast({ icon: "!", title: "派单失败", desc: formatAgencyApiError(e) });
  }
}

function openTaskDetail(t: DispatchTaskDto) {
  selectedTask.value = t;
  showTaskDetailModal.value = true;
}

async function markTaskCompleted(t: DispatchTaskDto) {
  try {
    await updateTaskStatus(t.id, "COMPLETED");
    showToast({ icon: "✓", title: "完成服务", desc: "工单状态已更新为已完成" });
    await refreshTasks();
    await refreshDashboardStats();
  } catch (e: unknown) {
    showToast({ icon: "!", title: "更新失败", desc: formatAgencyApiError(e) });
  }
}

function openAddWorkerModal() {
  workerModalMode.value = "add";
  workerForm.id = "";
  workerForm.name = "";
  workerForm.position = "";
  workerForm.phone = "";
  showWorkerModal.value = true;
}

function openEditWorkerModal(w: ServiceWorkerDto) {
  workerModalMode.value = "edit";
  workerForm.id = w.id;
  workerForm.name = w.name;
  workerForm.position = w.position;
  workerForm.phone = w.phone;
  showWorkerModal.value = true;
}

function closeWorkerModal() {
  showWorkerModal.value = false;
}

async function workerModalSubmit() {
  if (!workerForm.name.trim() || !workerForm.position.trim() || !workerForm.phone.trim()) {
    showToast({ icon: "!", title: "请填写完整", desc: "姓名、岗位、电话为必填" });
    return;
  }
  try {
    if (workerModalMode.value === "add") {
      await addWorker({
        name: workerForm.name.trim(),
        position: workerForm.position.trim(),
        phone: workerForm.phone.trim(),
        onlineStatus: "ONLINE"
      });
      showToast({ icon: "✓", title: "已添加", desc: "服务人员已加入列表" });
    } else {
      await updateWorker(workerForm.id, {
        name: workerForm.name.trim(),
        position: workerForm.position.trim(),
        phone: workerForm.phone.trim(),
        onlineStatus: "ONLINE"
      });
      showToast({ icon: "✓", title: "已保存", desc: "服务人员信息已更新" });
    }
    showWorkerModal.value = false;
    await refreshWorkers();
    await refreshDashboardStats();
  } catch (e: unknown) {
    showToast({ icon: "!", title: "保存失败", desc: formatAgencyApiError(e) });
  }
}

async function deleteWorker(id: string) {
  if (!confirm("确定删除该服务人员吗？")) return;
  try {
    await apiDeleteWorker(id);
    showToast({ icon: "✓", title: "删除成功", desc: "服务人员已移除" });
    await refreshWorkers();
    await refreshDashboardStats();
  } catch (e: unknown) {
    showToast({ icon: "!", title: "删除失败", desc: formatAgencyApiError(e) });
  }
}

async function refreshHealthRecords() {
  try {
    const rows = await listHealthRecords();
    healthRecordsList.value = rows.map(r => ({
      elderId: r.elderId,
      name: r.name,
      age: r.age,
      gender: r.gender,
      healthStatus: r.healthStatus,
      heartRate: r.heartRate,
      bloodPressure: r.bloodPressure,
      bloodOxygen: r.bloodOxygen,
      lastUpdated: r.lastUpdated ?? ""
    }));
  } catch (e: unknown) {
    healthRecordsList.value = [];
    showToast({ icon: "!", title: "健康档案加载失败", desc: formatAgencyApiError(e) });
  }
}

async function refreshAlerts() {
  try {
    const rows = await listAlerts();
    const sosPending = rows.filter(a => a.type === "SOS" && a.status === "PENDING");
    for (const a of sosPending) {
      if (!knownPendingSosIds.value.has(a.id)) {
        alertBannerDismissed.value = false;
        if (activeTab.value !== "alerts") {
          showToast({
            icon: "🆘",
            title: "SOS 紧急求助",
            desc: `${a.elderName} 已触发一键呼救，请立即处理`
          });
        }
        break;
      }
    }
    knownPendingSosIds.value = new Set(sosPending.map(a => a.id));
    syncAlertsFromApi(rows);
  } catch (e: unknown) {
    showToast({ icon: "!", title: "告警加载失败", desc: formatAgencyApiError(e) });
  }
}

const AGENCY_DATA_POLL_MS = 3000;
const AGENCY_ALERT_POLL_MS = 8000;

let alertPollTimer: number | undefined;
let dataPollTimer: number | undefined;

async function refreshDashboardStats() {
  try {
    const stats = await fetchAgencyDashboardStats();
    const legacy = stats as AgencyDashboardStatsDto & { completedThisMonth?: number };
    dashboardStats.value = {
      ...stats,
      completedTotal: stats.completedTotal ?? legacy.completedThisMonth ?? 0,
      completionRatePercent: Math.min(100, Math.max(0, stats.completionRatePercent ?? 0))
    };
  } catch {
    applyDashboardStatsFromLocal();
  }
}

async function refreshAgencyLiveData() {
  await Promise.all([refreshTasks(), refreshDashboardStats()]);
}

function startAlertPolling() {
  stopAlertPolling();
  void refreshAgencyLiveData();
  dataPollTimer = window.setInterval(() => {
    void refreshAgencyLiveData();
  }, AGENCY_DATA_POLL_MS);
  alertPollTimer = window.setInterval(() => {
    void refreshAlerts();
  }, AGENCY_ALERT_POLL_MS);
}

function stopAlertPolling() {
  if (alertPollTimer !== undefined) {
    window.clearInterval(alertPollTimer);
    alertPollTimer = undefined;
  }
  if (dataPollTimer !== undefined) {
    window.clearInterval(dataPollTimer);
    dataPollTimer = undefined;
  }
}

function onAgencyPageVisible() {
  if (document.visibilityState === "visible") {
    void refreshAgencyLiveData();
    void refreshAlerts();
  }
}

async function refreshActivities() {
  try {
    activities.value = await listActivities();
  } catch (e: unknown) {
    activities.value = [];
  }
}

async function refreshFinance() {
  try {
    financeRecords.value = await listFinance();
  } catch (e: unknown) {
    financeRecords.value = [];
  }
}

async function refreshNotifications() {
  try {
    notifications.value = await listNotifications();
  } catch (e: unknown) {
    notifications.value = [];
  }
}

async function refreshDevices() {
  try {
    devices.value = await listDevices();
  } catch (e: unknown) {
    devices.value = [];
  }
}

async function refreshSchedules() {
  try {
    schedules.value = await listSchedules();
  } catch (e: unknown) {
    schedules.value = [];
  }
}

async function refreshEvaluations() {
  try {
    evaluations.value = await listEvaluations();
  } catch (e: unknown) {
    evaluations.value = [];
  }
}

async function refreshTasks() {
  try {
    tasks.value = await listTasks();
  } catch (e: unknown) {
    tasks.value = [];
    showToast({ icon: "!", title: "工单加载失败", desc: formatAgencyApiError(e) });
  }
}

async function refreshWorkers() {
  try {
    workers.value = await listWorkers();
  } catch (e: unknown) {
    workers.value = [];
    showToast({ icon: "!", title: "人员加载失败", desc: formatAgencyApiError(e) });
  }
}

async function refreshEldersNameMap() {
  for (const k of Object.keys(elderNameMap)) delete elderNameMap[k];
  for (const e of elderList.value) elderNameMap[e.elderId] = e.name;
}

async function refreshAll() {
  try {
    elderList.value = await listElders();
    await refreshEldersNameMap();
  } catch {
    elderList.value = [];
  }
  await Promise.all([
    refreshWorkers(),
    refreshTasks(),
    refreshAlerts(),
    refreshSchedules(),
    refreshEvaluations(),
    refreshActivities(),
    refreshFinance(),
    refreshNotifications(),
    refreshDevices(),
    refreshHealthRecords()
  ]);
  await refreshDashboardStats();
}

function handleScroll() {
  isScrolled.value = window.scrollY > 50;
  showBackTop.value = window.scrollY > 500;
}

onMounted(async () => {
  await refreshAll();
  startAlertPolling();
  window.addEventListener("scroll", handleScroll, { passive: true });
  document.addEventListener("visibilitychange", onAgencyPageVisible);
});

onUnmounted(() => {
  stopAlertPolling();
  window.removeEventListener("scroll", handleScroll);
  document.removeEventListener("visibilitychange", onAgencyPageVisible);
});
</script>


<style scoped>
/* ===== 全局重置 ===== */
* { margin: 0; padding: 0; box-sizing: border-box; }
.agency-page {
  font-family: 'Inter', 'Noto Sans SC', -apple-system, sans-serif;
  color: #1a3347;
  background: #f8fafc;
  -webkit-font-smoothing: antialiased;
  padding-top: 72px;
}

.container { max-width: 1200px; margin: 0 auto; padding: 0 clamp(16px, 4vw, 40px); }
.section { padding: clamp(32px, 4vw, 48px) 0; }

/* ===== 顶部导航 ===== */
.app-topbar {
  position: fixed; top: 0; left: 0; right: 0; z-index: 1000;
  padding: 14px 0; background: #fff;
  border-bottom: 1px solid rgba(0,0,0,.04);
  transition: all .3s;
}
.app-topbar.topbar-scrolled {
  background: rgba(255,255,255,.98);
  backdrop-filter: blur(20px);
  box-shadow: 0 4px 20px rgba(0,0,0,.06);
  padding: 10px 0;
}
.topbar-inner {
  display: flex; align-items: center; justify-content: space-between;
}
.topbar-brand { display: flex; align-items: center; gap: 10px; }
.brand-mark {
  width: 42px; height: 42px; border-radius: 12px;
  background: linear-gradient(135deg, #1f6aa5, #174d7a);
  display: flex; align-items: center; justify-content: center; color: #fff;
}
.brand-name { font-size: 18px; font-weight: 700; }
.brand-sub { font-size: 10px; color: #94a3b8; letter-spacing: 2px; }
.nav-desktop { margin: 0 24px; }
.nav-list { display: flex; align-items: center; gap: 2px; list-style: none; }
.nav-link {
  padding: 8px 14px; border-radius: 6px;
  font-size: 13px; font-weight: 500; color: #475569;
  text-decoration: none; transition: all .2s;
}
.nav-link:hover { background: #f1f5f9; color: #1f6aa5; }
.nav-link.active { color: #1f6aa5; background: #eff6ff; }
.nav-arrow { font-size: 9px; opacity: .5; }
.nav-dropdown { position: relative; }
.nav-dropdown-simple {
  position: absolute; top: 100%; left: 0; padding-top: 6px;
  opacity: 0; visibility: hidden; transition: all .3s;
  list-style: none; min-width: 160px; background: #fff;
  border-radius: 10px; box-shadow: 0 12px 32px rgba(0,0,0,.1);
  pointer-events: none;
}
.nav-dropdown:hover .nav-dropdown-simple {
  opacity: 1; visibility: visible; pointer-events: auto;
}
.nav-dropdown-simple li { border-bottom: 1px solid #f1f5f9; }
.nav-dropdown-simple a {
  display: block; padding: 10px 16px; font-size: 13px; color: #475569;
  text-decoration: none; transition: all .2s;
}
.nav-dropdown-simple a:hover { background: #f8fafc; color: #1f6aa5; }
.nav-dropdown-simple li:last-child { border-bottom: none; }
.topbar-actions { display: flex; gap: 10px; align-items: center; }
.topbar-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 9px 18px; border-radius: 8px; font-size: 13px; font-weight: 500;
  cursor: pointer; border: none; font-family: inherit; transition: all .3s;
}
.topbar-btn.outline {
  background: #fff; color: #1a3347; border: 1px solid #e2e8f0;
}
.topbar-btn.outline:hover { background: #f1f5f9; }
.user-avatar { display: flex; align-items: center; gap: 8px; font-size: 13px; font-weight: 500; }
.avatar-circle {
  width: 36px; height: 36px; border-radius: 50%;
  background: #eff6ff; color: #1f6aa5;
  display: flex; align-items: center; justify-content: center; font-size: 16px;
}

/* ===== 告警横幅 ===== */
.alert-banner-sos {
  background: linear-gradient(90deg, #fef2f2, #fee2e2);
  border-color: #fca5a5;
  animation: sos-banner-pulse 2s ease-in-out infinite;
}
.alert-banner-sos .alert-text { color: #7f1d1d; }
.alert-more { font-size: 12px; color: #b91c1c; margin-left: 4px; }
@keyframes sos-banner-pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(220, 38, 38, 0.25); }
  50% { box-shadow: 0 0 0 6px rgba(220, 38, 38, 0); }
}

.alert-banner {
  background: linear-gradient(135deg, #fef2f2, #fee2e2);
  border: 1px solid #fecaca;
  border-radius: 12px; padding: 12px 16px;
  display: flex; align-items: center; gap: 12px;
  margin: 0 auto 16px; max-width: 1200px; cursor: pointer;
}
.alert-icon { font-size: 24px; }
.alert-text { flex: 1; font-weight: 500; color: #991b1b; }
.alert-link { color: #dc2626; text-decoration: underline; }
.alert-close { font-size: 20px; color: #9ca3af; cursor: pointer; }

/* ===== 统计卡片 ===== */
.metrics-section { padding-top: 16px; }
.section-header { text-align: center; margin-bottom: 32px; }
.section-title { font-size: clamp(24px, 3vw, 32px); font-weight: 700; margin-bottom: 8px; }
.section-desc { font-size: 14px; color: #7b95a8; }
.metrics-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px;
}
.metric-card {
  background: #fff; border-radius: 14px;
  padding: 24px; border: 1px solid #f1f5f9;
  transition: all .4s; display: flex; align-items: center; gap: 16px;
}
.metric-icon-wrap {
  width: 52px; height: 52px; border-radius: 14px;
  background: #f0fdf4; display: flex; align-items: center; justify-content: center; font-size: 24px;
  flex-shrink: 0;
}
.metric-icon-wrap.blue { background: #eff6ff; }
.metric-icon-wrap.green { background: #f0fdf4; }
.metric-icon-wrap.purple { background: #f5f3ff; }
.metric-content { flex: 1; }
.metric-num { font-size: 28px; font-weight: 800; color: #1a3347; line-height: 1.2; }
.metric-unit { font-size: 15px; font-weight: 600; color: #64748b; margin-left: 2px; }
.metric-label { font-size: 13px; color: #64748b; }
.metric-sub { font-size: 12px; color: #94a3b8; margin-top: 2px; }

/* ===== 趋势图表 ===== */
.chart-card {
  background: #fff; border-radius: 14px;
  padding: 24px; border: 1px solid #f1f5f9;
}
.chart-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
}
.chart-title { font-size: 16px; font-weight: 600; margin-bottom: 4px; }
.chart-sub { font-size: 12px; color: #94a3b8; }
.chart-btn {
  padding: 8px 16px; border-radius: 8px;
  border: 1px solid #e2e8f0; background: #fff; color: #1f6aa5;
  font-size: 12px; font-weight: 500; cursor: pointer; transition: all .2s;
}
.chart-btn:hover { background: #eff6ff; }
.chart-wrap { position: relative; }
.trend-chart { width: 100%; height: 160px; display: block; }
.axis-label { font-size: 11px; font-weight: 500; color: #94a3b8; }

/* ===== Tab 导航 ===== */
.tabs-container {
  background: #fff; border-radius: 16px;
  border: 1px solid #f1f5f9; overflow: hidden;
}
.tabs-header {
  display: flex; overflow-x: auto;
  border-bottom: 1px solid #f1f5f9; background: aliceblue;
}
.tab-btn {
  flex: 1; min-width: 60px; padding: 14px 16px;
  background: transparent; border: none; cursor: pointer;
  font-size: 13px; font-weight: 500; color: #64748b;
  transition: all .3s; border-bottom: 2px solid transparent;
  display: flex; align-items: center; gap: 6px; justify-content: center;
}
.tab-btn:hover { color: #1f6aa5; background: #f8fafc; }
.tab-btn.active {
  color: #1f6aa5; border-bottom-color: #1f6aa5;
  background: #fff;
}
.tab-icon { font-size: 16px; }
.tab-content { padding: 20px; }

/* ===== 表格 ===== */
.panel-hint {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 12px;
}
.panel-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 10px;
}
.search-row { display: flex; gap: 10px; align-items: center; flex: 1; min-width: 200px; }
.search-input {
  flex: 1; padding: 8px 12px; border-radius: 8px;
  border: 1px solid #e2e8f0; font-size: 13px; outline: none;
}
.search-input:focus { border-color: #93c5fd; box-shadow: 0 0 0 3px rgba(59,130,246,.06); }
.search-btn {
  padding: 8px 16px; border-radius: 8px;
  background: #1f6aa5; color: #fff; border: none; cursor: pointer; font-size: 13px; font-weight: 500;
}
.btn-primary {
  padding: 10px 20px; border-radius: 8px;
  background: #1f6aa5; color: #fff; border: none; cursor: pointer; font-size: 13px; font-weight: 500;
}
.btn-primary:hover { background: #174d7a; }
.btn-outline {
  padding: 8px 16px; border-radius: 8px;
  border: 1px solid #e2e8f0; background: #fff; color: #64748b; cursor: pointer; font-size: 13px;
}
.btn-outline:hover { border-color: #93c5fd; color: #1f6aa5; }
.table-wrap { overflow-x: auto; }
.table { width: 100%; border-collapse: collapse; font-size: 14px; }
.table th {
  text-align: left; padding: 10px 12px;
  color: #64748b; font-weight: 600; border-bottom: 1px solid #f1f5f9;
}
.table td {
  padding: 10px 12px; border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}
.mono { font-family: ui-monospace, monospace; }
.empty { padding: 32px; text-align: center; color: #94a3b8; font-size: 14px; }

/* ===== 状态标签 ===== */
.status-tag {
  display: inline-block; padding: 2px 10px; border-radius: 999px;
  font-size: 12px; font-weight: 500;
}
.status-tag.pending { background: #fef3c7; color: #d97706; }
.status-tag.doing { background: #dbeafe; color: #2563eb; }
.status-tag.done { background: #d1fae5; color: #059669; }
.status-tag.danger { background: #fee2e2; color: #dc2626; }
.status-tag.online { background: #d1fae5; color: #059669; }
.status-tag.offline { background: #f1f5f9; color: #94a3b8; }

/* ===== 操作按钮 ===== */
.row-actions { display: flex; gap: 6px; flex-wrap: wrap; }
.btn-detail {
  padding: 4px 12px; border-radius: 6px;
  border: 1px solid #e2e8f0; background: #fff; cursor: pointer; font-size: 12px; color: #64748b;
}
.btn-detail:hover { border-color: #93c5fd; color: #1f6aa5; }
.btn-blue {
  padding: 4px 12px; border-radius: 6px;
  background: #1f6aa5; color: #fff; border: none; cursor: pointer; font-size: 12px;
}
.btn-green {
  padding: 4px 12px; border-radius: 6px;
  background: #059669; color: #fff; border: none; cursor: pointer; font-size: 12px;
}
.btn-red {
  padding: 4px 12px; border-radius: 6px;
  background: #dc2626; color: #fff; border: none; cursor: pointer; font-size: 12px;
}

/* ===== 排班 ===== */
.schedule-controls { display: flex; align-items: center; gap: 12px; }
.schedule-date { font-weight: 500; font-size: 14px; }
.schedule-grid {
  border: 1px solid #f1f5f9; border-radius: 10px; overflow: hidden;
}
.schedule-header {
  display: grid; grid-template-columns: 80px repeat(7, 1fr);
  background: #f8fafc; padding: 8px; font-weight: 600; font-size: 13px;
}
.schedule-day { text-align: center; }
.schedule-row {
  display: grid; grid-template-columns: 80px repeat(7, 1fr);
  border-top: 1px solid #f1f5f9;
}
.schedule-time { padding: 8px; font-weight: 500; color: #64748b; font-size: 13px; }
.schedule-cell { padding: 4px; border-left: 1px solid #f1f5f9; min-height: 50px; }
.shift-item {
  padding: 4px 8px; border-radius: 4px; margin-bottom: 2px;
  font-size: 12px; display: flex; justify-content: space-between; align-items: center;
}
.shift-item.morning { background: #eff6ff; }
.shift-item.afternoon { background: #fef3c7; }
.shift-item.night { background: #ecfdf5; }
.shift-name { font-weight: 500; }
.shift-type { font-size: 10px; color: #94a3b8; }
.shift-delete { border: 0; background: transparent; color: #ef4444; cursor: pointer; }
.schedule-legend { display: flex; gap: 16px; margin-top: 12px; font-size: 13px; }
.schedule-legend .dot {
  display: inline-block; width: 12px; height: 12px; border-radius: 4px; margin-right: 4px;
}
.schedule-legend .dot.morning { background: #eff6ff; }
.schedule-legend .dot.afternoon { background: #fef3c7; }
.schedule-legend .dot.night { background: #ecfdf5; }

/* ===== 服务评价 ===== */
.evaluation-stats {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 16px;
}
.evaluation-stats-extended {
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
}
.stat-item {
  background: #f8fafc; border-radius: 10px; padding: 16px; text-align: center;
}
.stat-num { font-size: 24px; font-weight: 700; color: #1f6aa5; }
.stat-label { font-size: 12px; color: #64748b; margin-top: 4px; }
.rating { display: flex; align-items: center; gap: 4px; flex-wrap: wrap; }
.stars { color: #f59e0b; letter-spacing: -1px; }
.rating-num { font-weight: 500; font-size: 13px; }
.detail-ratings { display: flex; flex-direction: column; gap: 2px; min-width: 120px; }
.mini-rating { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #64748b; }
.mini-rating .stars { font-size: 11px; }
.tags-container { display: flex; flex-wrap: wrap; gap: 4px; }
.tag {
  display: inline-block; padding: 2px 8px; border-radius: 999px;
  font-size: 11px; background: #eff6ff; color: #1f6aa5;
}
.comment-cell { max-width: 200px; }
.comment-preview {
  font-size: 13px; color: #475569;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden;
}
.btn-link {
  border: 0; background: transparent; color: #1f6aa5; font-size: 12px;
  cursor: pointer; padding: 0; margin-top: 4px;
}
.btn-link:hover { text-decoration: underline; }
.eval-dimension-panel { margin-top: 16px; }
.eval-section-title { font-size: 14px; font-weight: 600; margin: 0 0 10px; color: #334155; }
.eval-dimension-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 10px;
}
.eval-dimension-item {
  background: #f8fafc; border-radius: 8px; padding: 10px 12px;
  display: flex; flex-direction: column; gap: 4px;
}
.eval-dim-label { font-size: 12px; color: #64748b; }
.eval-tags-block, .eval-comment-block { margin-top: 16px; }
.eval-comment-text {
  margin: 0; padding: 12px; background: #f8fafc; border-radius: 8px;
  font-size: 14px; line-height: 1.6; color: #475569;
}
.d-item-full { grid-column: 1 / -1; }
.empty-inline { font-size: 13px; color: #94a3b8; }

/* ===== 活动 ===== */
.activities-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px;
}
.activity-card {
  background: #fff; border-radius: 12px;
  padding: 16px; border: 1px solid #f1f5f9;
}
.activity-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.activity-title { font-weight: 600; font-size: 15px; }
.activity-status {
  padding: 2px 10px; border-radius: 999px; font-size: 12px;
}
.activity-status.upcoming { background: #eff6ff; color: #1f6aa5; }
.activity-status.ongoing { background: #fef3c7; color: #d97706; }
.activity-status.ended { background: #d1fae5; color: #059669; }
.activity-info .info-item { font-size: 13px; color: #64748b; margin-bottom: 4px; }
.activity-actions { display: flex; gap: 6px; margin-top: 10px; flex-wrap: wrap; }

/* ===== 收费 ===== */
.finance-summary {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 16px;
}
.summary-item {
  background: #f8fafc; border-radius: 10px; padding: 16px; text-align: center;
}
.summary-num { font-size: 24px; font-weight: 700; color: #1f6aa5; }
.summary-label { font-size: 12px; color: #64748b; margin-top: 4px; }

/* ===== 通知 ===== */
.notification-list { display: grid; gap: 12px; }
.notification-card {
  background: #fff; border-radius: 12px;
  padding: 16px; border: 1px solid #f1f5f9;
}
.notification-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.notification-title { font-weight: 600; font-size: 15px; }
.notification-status {
  padding: 2px 10px; border-radius: 999px; font-size: 12px;
}
.notification-status.published { background: #d1fae5; color: #059669; }
.notification-status.draft { background: #fef3c7; color: #d97706; }
.notification-status.expired { background: #fee2e2; color: #dc2626; }
.notification-content { color: #64748b; font-size: 14px; margin-bottom: 6px; }
.notification-meta { display: flex; gap: 16px; font-size: 12px; color: #94a3b8; }
.notification-actions { display: flex; gap: 6px; margin-top: 8px; flex-wrap: wrap; }

/* ===== 设备 ===== */
.device-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px;
}
.device-card {
  background: #fff; border-radius: 12px;
  padding: 16px; border: 1px solid #f1f5f9;
}
.device-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.device-name { font-weight: 600; font-size: 15px; }
.device-status {
  padding: 2px 10px; border-radius: 999px; font-size: 12px;
}
.device-status.online { background: #d1fae5; color: #059669; }
.device-status.offline { background: #fee2e2; color: #dc2626; }
.device-status.low { background: #fef3c7; color: #d97706; }
.device-info .info-item { font-size: 13px; color: #64748b; margin-bottom: 4px; }
.device-actions { display: flex; gap: 6px; margin-top: 8px; flex-wrap: wrap; }

/* ===== 侧边栏 ===== */
.side-service {
  position: fixed; right: 16px; bottom: 50%;
  transform: translateY(50%); z-index: 999;
}
.side-service ul { list-style: none; display: flex; flex-direction: column; gap: 2px; }
.side-service-item {
  width: 44px; height: 44px; display: flex; align-items: center; justify-content: center;
  border-radius: 10px; background: #fff; border: 1px solid #e2e8f0;
  color: #64748b; cursor: pointer; transition: all .3s; position: relative;
}
.side-service-item:hover { background: #1f6aa5; color: #fff; border-color: #1f6aa5; }
.side-service-popup {
  position: absolute; right: 56px; top: 50%; transform: translateY(-50%);
  background: #1a2332; color: #fff; font-size: 11px; padding: 6px 12px;
  border-radius: 6px; white-space: nowrap; opacity: 0; pointer-events: none;
  transition: opacity .3s;
}
.side-service-item:hover .side-service-popup { opacity: 1; }
.side-service-item.backtop { opacity: 0; visibility: hidden; transition: all .3s; }
.side-service-item.backtop.visible { opacity: 1; visibility: visible; }

/* ===== 助手弹窗 ===== */
.assistant-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,.4);
  backdrop-filter: blur(4px); z-index: 2000;
  display: flex; align-items: center; justify-content: center; padding: 20px;
}
.assistant-modal {
  background: #fff; border-radius: 20px; width: 100%; max-width: 460px;
  max-height: 85vh; display: flex; flex-direction: column;
  box-shadow: 0 30px 60px rgba(0,0,0,.2);
}
.assistant-modal-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 18px 20px; border-bottom: 1px solid #f1f5f9;
}
.assistant-modal-title { display: flex; align-items: center; gap: 10px; }
.assistant-modal-title h3 { font-size: 16px; font-weight: 700; }
.assistant-badge { font-size: 9px; padding: 2px 7px; border-radius: 999px; background: #f5f3ff; color: #7c3aed; }
.ai-icon-pulse {
  width: 36px; height: 36px; border-radius: 10px;
  background: linear-gradient(135deg, #eef2ff, #e0e7ff);
  display: flex; align-items: center; justify-content: center; color: #4f46e5;
}
.assistant-close {
  width: 34px; height: 34px; border-radius: 8px;
  border: 1px solid #e2e8f0; background: #fff; color: #94a3b8; cursor: pointer;
}
.assistant-messages { flex: 1; max-height: 280px; overflow-y: auto; padding: 12px 16px; display: flex; flex-direction: column; gap: 8px; }
.assistant-msg { display: flex; }
.assistant-msg.user { justify-content: flex-end; }
.assistant-bubble {
  max-width: 85%; padding: 8px 12px; border-radius: 12px;
  font-size: 13px; line-height: 1.5;
}
.user .assistant-bubble { background: #1f6aa5; color: #fff; border-bottom-right-radius: 4px; }
.assistant .assistant-bubble { background: #f8fafc; border: 1px solid #e2e8f0; border-bottom-left-radius: 4px; }
.assistant-quick { display: flex; flex-wrap: wrap; gap: 6px; padding: 0 16px 10px; }
.assistant-chip {
  font-size: 11px; padding: 5px 10px; border-radius: 999px;
  border: 1px solid #ddd6fe; background: #f5f3ff; color: #7c3aed; cursor: pointer;
}
.assistant-compose { display: flex; gap: 8px; padding: 10px 16px 16px; }
.assistant-compose input {
  flex: 1; padding: 10px 12px; border-radius: 10px;
  border: 1px solid #e2e8f0; font-size: 13px; outline: none;
}
.assistant-compose input:focus { border-color: #93c5fd; box-shadow: 0 0 0 3px rgba(59,130,246,.06); }
.assistant-compose button {
  width: 40px; height: 40px; border-radius: 10px;
  background: #7c3aed; border: none; color: #fff; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
}
.loading-dot {
  width: 14px; height: 14px; border-radius: 50%;
  border: 2px solid rgba(255,255,255,.3); border-top-color: #fff;
  animation: spin .6s linear infinite; display: block;
}

/* ===== 弹窗 ===== */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,.4);
  backdrop-filter: blur(4px); z-index: 2000;
  display: flex; align-items: center; justify-content: center; padding: 20px;
}
.modal-card {
  background: #fff; border-radius: 16px; padding: 24px;
  width: 500px; max-width: 100%; box-shadow: 0 20px 60px rgba(0,0,0,.15);
}
.modal-card.large { width: 800px; }
.modal-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.modal-title { font-size: 18px; font-weight: 700; }
.modal-close { border: 0; background: transparent; font-size: 24px; color: #9ca3af; cursor: pointer; }
.modal-body { padding: 4px 0; }
.modal-actions { display: flex; gap: 10px; margin-top: 16px; }
.btn-confirm { padding: 8px 24px; border-radius: 8px; background: #1f6aa5; color: #fff; border: none; cursor: pointer; }

/* ===== 详情网格 ===== */
.detail-grid { display: grid; gap: 10px; }
.d-item { display: flex; gap: 12px; padding: 8px 0; border-bottom: 1px solid #f1f5f9; }
.d-item:last-child { border-bottom: none; }
.d-k { width: 100px; font-weight: 500; color: #64748b; }
.d-v { flex: 1; font-weight: 500; }

/* ===== 报表 ===== */
.report-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.report-card { border: 1px solid #f1f5f9; border-radius: 12px; padding: 16px; }
.report-card h4 { margin: 0 0 12px; font-size: 14px; font-weight: 600; }
.report-chart-placeholder { height: 160px; display: flex; align-items: flex-end; justify-content: center; gap: 2px; }
.bar-chart { display: flex; align-items: flex-end; gap: 2px; height: 100%; width: 100%; }
.bar-item { flex: 1; display: flex; flex-direction: column; align-items: center; height: 100%; justify-content: flex-end; }
.bar { width: 80%; background: #3b82f6; border-radius: 2px 2px 0 0; }
.bar-label { font-size: 9px; color: #94a3b8; margin-top: 4px; }
.pie-chart { width: 120px; height: 120px; margin: 0 auto; border-radius: 50%; background: conic-gradient(#67c23a 0% 72%, #3b82f6 72% 92%, #e6a23c 92% 100%); }
.legend { display: flex; justify-content: center; gap: 16px; margin-top: 12px; font-size: 13px; }
.legend .dot { display: inline-block; width: 10px; height: 10px; border-radius: 2px; margin-right: 4px; }
.legend .dot.green { background: #67c23a; }
.legend .dot.blue { background: #3b82f6; }
.legend .dot.orange { background: #e6a23c; }
.performance-list { display: flex; flex-direction: column; gap: 6px; }
.perf-item { display: flex; align-items: center; gap: 12px; }
.perf-name { width: 80px; font-weight: 500; }
.perf-score { width: 60px; font-weight: 700; color: #3b82f6; }
.perf-bar { flex: 1; height: 6px; background: #f1f5f9; border-radius: 4px; overflow: hidden; }
.perf-fill { height: 100%; background: #3b82f6; border-radius: 4px; }
.modal-cta { margin-top: 16px; display: flex; justify-content: center; }

.form-row {
  display: flex; justify-content: space-between; align-items: center;
  gap: 12px; margin-bottom: 14px;
}
.form-row-static { align-items: flex-start; }
.f-label { width: 120px; font-weight: 600; color: #64748b; font-size: 14px; flex-shrink: 0; }
.wide-input, .wide-select, .wide-textarea {
  flex: 1; border: 1px solid #e2e8f0; border-radius: 8px;
  padding: 10px 12px; font-size: 14px; font-family: inherit;
}
.wide-textarea { min-height: 80px; resize: vertical; }
.static-value { flex: 1; font-weight: 500; color: #1a3347; padding: 8px 0; }
.reco-box {
  margin-top: 8px; padding: 12px; border-radius: 8px;
  background: #f8fafc; border: 1px solid #e2e8f0;
  font-size: 13px; color: #64748b; line-height: 1.5;
}
.btn-cancel {
  padding: 8px 20px; border-radius: 8px; border: 1px solid #e2e8f0;
  background: #fff; cursor: pointer; font-weight: 600;
}
.btn-confirm { padding: 8px 20px; border-radius: 8px; background: #1f6aa5; color: #fff; border: none; cursor: pointer; font-weight: 600; }

/* ===== Toast ===== */
.toast {
  position: fixed; left: 50%; bottom: 80px;
  transform: translateX(-50%);
  width: 90%; max-width: 420px;
  background: #fff; border-radius: 12px; padding: 12px 16px;
  display: flex; gap: 12px; align-items: center;
  z-index: 3000; box-shadow: 0 8px 30px rgba(0,0,0,.12);
  border: 1px solid #f1f5f9;
}
.toast-left { width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; background: #d1fae5; color: #059669; }
.toast-right { flex: 1; }
.toast-title { font-weight: 600; }
.toast-desc { margin-top: 4px; font-size: 13px; color: #64748b; }
.toast-close { border: 0; background: transparent; font-size: 18px; color: #9ca3af; cursor: pointer; }

@keyframes spin { to { transform: rotate(360deg); } }

/* ===== 响应式 ===== */
@media (max-width: 1199px) {
  .nav-desktop { display: none; }
  .side-service { right: 8px; }
}
@media (max-width: 1023px) {
  .metrics-grid { grid-template-columns: repeat(2, 1fr); }
  .report-grid { grid-template-columns: 1fr; }
}
@media (max-width: 640px) {
  .metrics-grid { grid-template-columns: 1fr; }
  .activities-grid { grid-template-columns: 1fr; }
  .device-grid { grid-template-columns: 1fr; }
  .evaluation-stats,
  .evaluation-stats-extended { grid-template-columns: 1fr; }
  .table-evaluations { font-size: 12px; }
  .finance-summary { grid-template-columns: 1fr; }
  .tabs-header { flex-wrap: nowrap; }
  .tab-btn { font-size: 12px; padding: 10px 12px; }
}
</style>