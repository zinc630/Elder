<template>
  <div class="app-container child-app">
    <div class="bottom-nav">
      <div class="nav-item" :class="{ active: childTab === 'home' }" @click="childTab = 'home'">
        <span class="nav-icon">🏠</span>
        <span class="nav-label">首页</span>
      </div>
      <div class="nav-item" :class="{ active: childTab === 'health' }" @click="childTab = 'health'">
        <span class="nav-icon">❤️</span>
        <span class="nav-label">健康</span>
      </div>
      <div class="nav-item" :class="{ active: childTab === 'mine' }" @click="childTab = 'mine'">
        <span class="nav-icon">👤</span>
        <span class="nav-label">我的</span>
      </div>
    </div>

    <!-- ===== 切换老人弹窗 ===== -->
    <div v-if="showElderSwitchModal" class="modal-overlay" role="dialog" aria-modal="true">
      <div class="modal-card compact-modal">
        <div class="modal-top">
          <div class="modal-title">切换老人</div>
          <button class="modal-close" @click="showElderSwitchModal = false" aria-label="关闭">×</button>
        </div>

        <div class="modal-body">
          <div class="elder-list">
            <button
              v-for="e in elderOptions"
              :key="e.elderId"
              class="elder-choice"
              :class="{ active: e.elderId === elderId }"
              @click="selectElder(e.elderId)"
            >
              <div class="elder-choice-name">{{ e.name }}</div>
              <div class="elder-choice-sub">{{ e.address }}</div>
            </button>
          </div>
          <div v-if="elderOptions.length === 0" class="muted">暂无可切换老人</div>
        </div>

        <div class="modal-actions">
          <button class="btn-cancel" @click="showElderSwitchModal = false">取消</button>
          <button class="btn-confirm" @click="showElderSwitchModal = false">完成</button>
        </div>
      </div>
    </div>

    <!-- ===== 首页 TAB（布局对齐老人端） ===== -->
    <div v-show="childTab === 'home'" class="home-page">
      <div
          v-if="activeSosAlarm"
          class="sos-response-banner"
          role="alert"
          @click="goHealthTab('latest')"
      >
        <div class="sos-banner-icon">🆘</div>
        <div class="sos-banner-body">
          <div class="sos-banner-title">SOS 紧急求助</div>
          <div class="sos-banner-desc">
            {{ profile?.name ?? "老人" }} 已触发一键呼救，请立即联系并确认安全
          </div>
          <div class="sos-banner-time">{{ formatAlarmTime(activeSosAlarm.triggeredAt) }}</div>
        </div>
        <span class="sos-banner-action">去处理 ›</span>
      </div>

      <div class="notice-bar" :class="{ 'notice-bar-urgent': activeSosAlarm }" @click="goHealthTab('latest')">
        <div class="notice-left">
          <span class="notice-icon">{{ activeSosAlarm ? '🆘' : '📢' }}</span>
          <span class="notice-text">{{ activeSosAlarm ? 'SOS 待响应' : '守护提醒' }}</span>
        </div>
        <div class="notice-right">
          <span class="notice-count">{{ pendingAlarmCount }} 条待处理告警</span>
          <span class="notice-arrow">›</span>
        </div>
      </div>

      <div class="home-header">
        <div class="search-bar">
          <span class="search-icon">🔍</span>
          <input v-model="homeSearch" class="search-input" placeholder="搜索守护功能" />
        </div>
        <div class="header-right">
          <button type="button" class="ai-assistant" title="智能助手" @click="toggleAiChat">
            <span class="ai-icon">🤖</span>
          </button>
          <button type="button" class="switch-chip" @click="onSwitchElder" title="切换老人">👤</button>
        </div>
      </div>

      <div class="banner-carousel" @mouseenter="stopBannerTimer" @mouseleave="startBannerTimer">
        <transition name="banner-fade" mode="out-in">
          <img
            :key="bannerIndex"
            :src="bannerSlides[bannerIndex].src"
            :alt="bannerSlides[bannerIndex].alt"
            class="banner-img"
          />
        </transition>
        <div class="carousel-dots">
          <span
            v-for="(_, i) in bannerSlides"
            :key="i"
            class="dot"
            :class="{ active: bannerIndex === i }"
            @click="setBanner(i)"
          />
        </div>
      </div>

      <div class="care-cta-container">
        <button type="button" class="care-big-btn" @click="openRemoteVisit">
          <div class="care-icon">📹</div>
          <div class="care-text">远程探视</div>
        </button>
      </div>

      <section v-if="profile" class="guardian-strip">
        <div class="guardian-strip-head">
          <div>
            <div class="guardian-name">{{ profile.name }} · 远程守护</div>
            <div class="guardian-desc">{{ todayDesc }}</div>
          </div>
          <div class="guardian-tag">{{ latestContactText }}</div>
        </div>
      </section>
      <section v-else class="card guardian-empty">
        <p>请先切换并绑定老人账号，查看健康动态。</p>
        <button type="button" class="link-btn" @click="onSwitchElder">去绑定老人</button>
      </section>

      <section class="card child-news-section">
        <div class="child-news-head">
          <span class="child-news-title">📰 新闻资讯</span>
          <span v-if="newsList.length" class="child-news-count">{{ newsList.length }} 条</span>
        </div>
        <div v-if="newsLoading" class="portal-empty">加载中…</div>
        <div v-else-if="newsList.length === 0" class="portal-empty">暂无资讯，请稍后再看</div>
        <div v-else class="portal-list">
          <div
            v-for="(item, index) in newsList"
            :key="item.id || `news-${index}`"
            class="portal-card"
            @click="openPortalNews(item)"
          >
            <div class="portal-card-thumb">
              <img v-if="item.imageUrl" :src="resolveMediaUrl(item.imageUrl)" :alt="item.title" />
              <span v-else class="portal-card-emoji">{{ item.icon || "📰" }}</span>
            </div>
            <div class="portal-card-body">
              <div class="portal-card-title">{{ item.title }}</div>
              <p v-if="item.summary" class="portal-card-summary">{{ item.summary }}</p>
              <div class="portal-card-sub">{{ item.publishDate }} · {{ item.source }}</div>
            </div>
          </div>
        </div>
      </section>

      <div class="hot-section">
        <div class="hot-title">远程守护服务</div>
        <div class="hot-grid">
          <div class="hot-item" @click="showServiceModal = true">
            <div class="hot-icon">🏠</div>
            <div class="hot-name">预约居家服务</div>
          </div>
          <div class="hot-item" @click="showWeeklyReport = true">
            <div class="hot-icon">📊</div>
            <div class="hot-name">健康周报</div>
          </div>
          <div class="hot-item" @click="goHealthTab('map')">
            <div class="hot-icon">📍</div>
            <div class="hot-name">位置围栏</div>
          </div>
        </div>
      </div>

      <div class="grid-container">
        <div class="grid-row-4col">
          <div class="grid-item" @click="openFamilyModal">
            <div class="grid-icon pink-bg">💬</div>
            <span class="grid-label">亲情互动</span>
          </div>
          <div class="grid-item" @click="openVitalsModal">
            <div class="grid-icon green-bg">📊</div>
            <span class="grid-label">录入健康</span>
          </div>
          <div class="grid-item" @click="showServiceModal = true">
            <div class="grid-icon blue-bg">📅</div>
            <span class="grid-label">服务预约</span>
          </div>
          <div class="grid-item" @click="onSwitchElder">
            <div class="grid-icon purple-bg">👤</div>
            <span class="grid-label">切换老人</span>
          </div>
          <div class="grid-item" @click="goHealthTab('latest')">
            <div class="grid-icon red-bg">🔔</div>
            <span class="grid-label">告警中心</span>
          </div>
          <div class="grid-item" @click="showWeeklyReport = true">
            <div class="grid-icon orange-bg">📋</div>
            <span class="grid-label">查看周报</span>
          </div>
          <div class="grid-item" @click="goHealthTab('map')">
            <div class="grid-icon blue-light-bg">🗺️</div>
            <span class="grid-label">电子地图</span>
          </div>
          <div class="grid-item" @click="childTab = 'mine'">
            <div class="grid-icon green-bg">⚙️</div>
            <span class="grid-label">我的设置</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== 健康 TAB（布局对齐老人端） ===== -->
    <div v-show="childTab === 'health'" class="health-page">
      <div class="health-header">
        <div class="health-title">守护健康数据</div>
        <div class="health-avatar-wrapper">
          <div class="health-avatar-ring">
            <div class="health-avatar-inner">
              <span class="avatar-icon">🧓</span>
            </div>
          </div>
        </div>
        <div class="health-name">{{ elderName }}</div>
        <div class="health-sub">{{ todayDesc }}</div>
        <div class="health-actions">
          <div class="health-action-btn" @click="openVitalsModal">
            <span class="icon">📋</span> 录入数据
          </div>
          <div class="health-action-btn" @click="goHealthTab('latest')">
            <span class="icon">🔔</span> 告警中心
          </div>
          <div class="health-action-btn" @click="openFamilyModal">
            <span class="icon">💬</span> 亲情互动
          </div>
        </div>
      </div>

      <div class="health-cards">
        <div class="health-card">
          <div class="card-label">心率 BPM</div>
          <div class="card-value">{{ vitals.hr }}</div>
          <div class="card-range">参考 60-100</div>
        </div>
        <div class="health-card">
          <div class="card-label">血压 mmHg</div>
          <div class="card-value">{{ vitals.sbp }}/{{ vitals.dbp }}</div>
          <div class="card-range">关注波动</div>
        </div>
        <div class="health-card">
          <div class="card-label">血氧 %</div>
          <div class="card-value">{{ vitals.spo2 }}</div>
          <div class="card-range">建议 ≥95</div>
        </div>
        <div class="health-card">
          <div class="card-label">今日步数</div>
          <div class="card-value">{{ steps }}</div>
          <div class="card-range">步</div>
        </div>
      </div>

      <section class="card alarm-center">
        <div class="section-head">
          <div class="section-title">安全预警 & 告警中心</div>
        </div>
        <div class="alarm-tabs">
          <button type="button" class="tab" :class="{ active: alarmTab === 'latest' }" @click="alarmTab = 'latest'">最新告警</button>
          <button type="button" class="tab" :class="{ active: alarmTab === 'history' }" @click="alarmTab = 'history'">历史预警</button>
          <button type="button" class="tab" :class="{ active: alarmTab === 'map' }" @click="alarmTab = 'map'">电子地图</button>
        </div>
        <div class="alarm-body">
          <ElderLocationMap v-if="alarmTab === 'map' && elderId" class="child-loc-map" :elder-id="elderId" height="300px" />
          <div v-else-if="alarmTab === 'map'" class="muted">未绑定老人账号，无法展示位置地图。</div>
          <div v-else>
            <div v-if="alarmsUi.length === 0" class="empty">暂无告警</div>
            <div
                v-for="a in alarmsUi"
                :key="a.id"
                class="alarm-item"
                :class="{ 'alarm-item-sos': a.isSos, 'alarm-item-pending': a.isPending }"
            >
              <div class="alarm-item-left">
                <div class="alarm-time">{{ a.triggeredAt }}</div>
                <div class="alarm-subtitle">
                  {{ a.subtitle }}
                  <span v-if="a.isSos" class="alarm-sos-badge">紧急</span>
                </div>
                <div class="alarm-desc">{{ a.desc }}</div>
                <div class="alarm-status-line">{{ a.statusLabel }}</div>
              </div>
              <div class="alarm-item-right">
                <button type="button" class="confirm-btn" :class="{ 'confirm-btn-sos': a.isSos }" @click="confirmAlarm(a.id)">
                  {{ a.isSos ? '确认已联系' : '确认处理' }}
                </button>
              </div>
            </div>
          </div>
        </div>
        <div class="rule-box">
          <div class="rule-title">报警规则</div>
          <div class="rule-text">离开电子围栏或 SOS 触发后，将立即通知子女端并记录告警。</div>
        </div>
      </section>

      <div class="health-quick-actions">
        <div class="quick-icon-item" @click="goHealthTab('map')">
          <div class="quick-icon-circle blue">📍</div>
          <span class="quick-icon-label">位置地图</span>
        </div>
        <div class="quick-icon-item" @click="openVitalsModal">
          <div class="quick-icon-circle green">📊</div>
          <span class="quick-icon-label">录入数据</span>
        </div>
        <div class="quick-icon-item" @click="showWeeklyReport = true">
          <div class="quick-icon-circle purple-light">📋</div>
          <span class="quick-icon-label">健康周报</span>
        </div>
        <div class="quick-icon-item" @click="showServiceModal = true">
          <div class="quick-icon-circle orange">🏠</div>
          <span class="quick-icon-label">预约服务</span>
        </div>
      </div>

      <div class="steps-section">
        <div class="steps-header">
          <span class="steps-title">活动计步</span>
          <span class="steps-plus">+</span>
        </div>
        <div class="steps-content">
          <div class="step-item">
            <div class="step-label">昨日</div>
            <div class="step-number">{{ stepsYesterday }}</div>
          </div>
          <div class="step-item">
            <div class="step-label">今日</div>
            <div class="step-number">{{ steps }}</div>
          </div>
          <div class="step-item">
            <div class="step-label">血糖</div>
            <div class="step-number">{{ vVitals.glucose }}</div>
          </div>
        </div>
      </div>

      <section class="card">
        <div class="section-title-inline">📋 最近健康记录</div>
        <div class="health-record-list">
          <div v-for="i in 3" :key="i" class="health-record-item">
            <div class="record-time">{{ new Date(Date.now() - i * 86400000).toLocaleDateString() }}</div>
            <div class="record-data">心率: {{ vitals.hr - i * 2 }} bpm · 血压: {{ vitals.sbp - i * 3 }}/{{ vitals.dbp - i * 2 }}</div>
          </div>
        </div>
      </section>
    </div>

      <!-- ===== 我的 TAB（菜单式，对齐老人端） ===== -->
      <div v-show="childTab === 'mine'" class="mine-page">
        <div class="mine-header">
          <div class="mine-avatar-section">
            <div class="mine-avatar-circle">👨‍👩‍👧</div>
            <div>
              <div class="mine-name">{{ childDisplayName }}</div>
              <div class="mine-sub">守护老人：{{ elderDisplayName }}</div>
            </div>
          </div>
        </div>

        <div class="menu-list">
          <div class="menu-item" @click="openChildProfileModal">
            <div class="menu-left">
              <span class="menu-icon blue">👤</span>
              <span class="menu-text">个人信息（子女）</span>
            </div>
            <span class="menu-arrow">›</span>
          </div>
          <div class="menu-item" @click="openChangePassword">
            <div class="menu-left">
              <span class="menu-icon red">🔒</span>
              <span class="menu-text">修改密码</span>
            </div>
            <span class="menu-arrow">›</span>
          </div>
          <div class="menu-item" @click="openChildProfileModal">
            <div class="menu-left">
              <span class="menu-icon pink">📍</span>
              <span class="menu-text">联系地址（子女）</span>
            </div>
            <span class="menu-arrow">›</span>
          </div>
        </div>

        <div class="menu-section-label">守护中的老人</div>
        <div class="menu-list">
          <div class="menu-item" @click="onSwitchElder">
            <div class="menu-left">
              <span class="menu-icon green">🔄</span>
              <span class="menu-text">切换老人</span>
            </div>
            <span class="menu-arrow">›</span>
          </div>
          <div class="menu-item" @click="openElderProfileModal">
            <div class="menu-left">
              <span class="menu-icon blue-light">👤</span>
              <span class="menu-text">老人个人信息</span>
            </div>
            <span class="menu-arrow">›</span>
          </div>
          <div class="menu-item" @click="openElderProfileModal">
            <div class="menu-left">
              <span class="menu-icon pink">📍</span>
              <span class="menu-text">老人服务住址</span>
            </div>
            <span class="menu-arrow">›</span>
          </div>
          <div class="menu-item" @click="openElderHealthNotes">
            <div class="menu-left">
              <span class="menu-icon orange">📋</span>
              <span class="menu-text">老人健康备注</span>
            </div>
            <span class="menu-arrow">›</span>
          </div>
        </div>

        <div class="menu-section-label">服务与账户</div>
        <div class="menu-list">
          <div class="menu-item" @click="openVolunteer">
            <div class="menu-left">
              <span class="menu-icon orange">❤️</span>
              <span class="menu-text">帮扶志愿者</span>
            </div>
            <span class="menu-arrow">›</span>
          </div>
          <div class="menu-item" @click="openMyButler">
            <div class="menu-left">
              <span class="menu-icon blue-light">👤</span>
              <span class="menu-text">我的管家</span>
            </div>
            <span class="menu-arrow">›</span>
          </div>
          <div class="menu-item" @click="openMyOrders">
            <div class="menu-left">
              <span class="menu-icon red-light">📋</span>
              <span class="menu-text">服务订单</span>
            </div>
            <span class="menu-arrow">›</span>
          </div>
          <div class="menu-item" @click="openMemberAccount">
            <div class="menu-left">
              <span class="menu-icon blue">💳</span>
              <span class="menu-text">会员账户</span>
            </div>
            <span class="menu-arrow">›</span>
          </div>
        </div>

        <div class="settings-section">
          <div class="setting-item">
            <div class="setting-left">
              <span class="setting-icon red">⚠️</span>
              <span class="setting-text">告警推送</span>
            </div>
            <div class="toggle-switch active" @click="toggleSetting('alert')"></div>
          </div>
          <div class="setting-item">
            <div class="setting-left">
              <span class="setting-icon blue">👤</span>
              <span class="setting-text">完善信息</span>
            </div>
            <div class="toggle-switch active" @click="toggleSetting('info')"></div>
          </div>
        </div>
      </div>

    <!-- ===== 周报弹窗 ===== -->
    <div v-if="showWeeklyReport" class="modal-overlay">
      <div class="modal-card compact-modal weekly-modal">
        <div class="modal-top">
          <div class="modal-title">老人健康周报（近7天）</div>
          <button class="modal-close" @click="showWeeklyReport = false">×</button>
        </div>
        <div class="modal-body">
          <div class="chart-tip">血压-收缩压</div>
          <svg width="100%" height="210" viewBox="0 0 520 210" role="img" aria-label="血压趋势">
            <path d="M70,120 L150,116 L230,118 L310,112 L390,114 L480,110 L480,190 L70,190 Z" fill="rgba(30,136,229,0.08)" />
            <polyline fill="none" stroke="#1976d2" stroke-width="3" points="70,120 150,116 230,118 310,112 390,114 480,110" />
            <g fill="#1976d2">
              <circle cx="70" cy="120" r="4"/>
              <circle cx="150" cy="116" r="4"/>
              <circle cx="230" cy="118" r="4"/>
              <circle cx="310" cy="112" r="4"/>
              <circle cx="390" cy="114" r="4"/>
              <circle cx="480" cy="110" r="4"/>
            </g>
          </svg>
          <div class="modal-cta">
            <button class="primary-btn" @click="onExportWeekly">导出健康报告</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 远程探视（首页大按钮） -->
    <div v-if="showRemoteVisitModal" class="modal-overlay visit-overlay" role="dialog" aria-modal="true">
      <div class="modal-card visit-modal">
        <div class="modal-top">
          <div class="modal-title">远程探视 · {{ elderName }}</div>
          <button class="modal-close" @click="closeRemoteVisit" aria-label="关闭">×</button>
        </div>
        <div class="modal-body small-pad visit-body">
          <div class="visit-mode-tabs">
            <button
              type="button"
              class="visit-mode-tab"
              :class="{ active: visitMode === 'elder' }"
              @click="setVisitMode('elder')"
            >
              📞 视频通话
            </button>
            <button
              type="button"
              class="visit-mode-tab"
              :class="{ active: visitMode === 'monitor' }"
              @click="setVisitMode('monitor')"
            >
              📡 家庭监控
            </button>
          </div>

          <template v-if="visitMode === 'elder'">
            <div class="visit-hint" v-if="videoState.status === 'idle' || videoState.status === 'dialing'">
              {{ videoState.status === "dialing" ? "正在呼叫老人端，请等待接听…" : "与老人端设备进行视频通话" }}
            </div>
            <div class="video-stage visit-stage" :class="{ connected: videoState.status === 'connected' }">
              <div class="video-main">
                <div class="video-title">老人端画面</div>
                <video
                  ref="remoteVideoRef"
                  class="video-remote-stream"
                  :class="{ hidden: videoState.status !== 'connected' }"
                  autoplay
                  playsinline
                />
                <div v-if="videoState.status !== 'connected'" class="video-avatar">{{ elderName.slice(0, 1) }}</div>
                <div class="video-status">{{ videoStatusText }} · {{ videoDurationText }}</div>
              </div>
              <video
                ref="localVideoRef"
                class="video-local-stream"
                :class="{ off: !videoState.cameraOn || videoState.status === 'idle' }"
                autoplay
                playsinline
                muted
              />
            </div>
            <div class="visit-actions">
              <button
                class="video-btn"
                :disabled="videoState.status === 'dialing' || videoState.status === 'connected'"
                @click="startVideoCall"
              >
                {{ videoState.status === "dialing" ? "呼叫中…" : videoState.status === "connected" ? "通话中" : "发起呼叫" }}
              </button>
              <button class="video-btn ghost" :disabled="videoState.status === 'idle'" @click="endVideoCall">
                挂断
              </button>
            </div>
            <div class="video-controls">
              <button class="control-btn" :disabled="videoState.status !== 'connected'" @click="toggleVideoMute">
                {{ videoState.muted ? "取消静音" : "静音" }}
              </button>
              <button class="control-btn" :disabled="videoState.status !== 'connected'" @click="toggleVideoCamera">
                {{ videoState.cameraOn ? "关闭摄像头" : "开启摄像头" }}
              </button>
              <button class="control-btn" :disabled="videoState.status !== 'connected'" @click="videoState.speakerOn = !videoState.speakerOn">
                {{ videoState.speakerOn ? "免提开" : "免提关" }}
              </button>
            </div>
          </template>

          <template v-else>
            <div class="visit-hint monitor-hint">
              <template v-if="monitorState.status === 'connecting'">正在连接 {{ activeMonitorDevice?.name }}…</template>
              <template v-else-if="monitorState.status === 'connected'">已连接 · {{ activeMonitorDevice?.location }}</template>
              <template v-else>选择家中监控摄像头，实时查看老人居家画面</template>
            </div>

            <div v-if="monitorState.status !== 'connected'" class="monitor-device-list">
              <button
                v-for="device in monitorDevices"
                :key="device.id"
                type="button"
                class="monitor-device-card"
                :class="{ active: selectedMonitorId === device.id, offline: device.status === 'offline' }"
                @click="selectedMonitorId = device.id"
              >
                <div class="monitor-device-icon">{{ device.status === "online" ? "📹" : "⚫" }}</div>
                <div class="monitor-device-info">
                  <div class="monitor-device-name">{{ device.name }}</div>
                  <div class="monitor-device-meta">{{ device.location }} · {{ device.deviceSn }}</div>
                </div>
                <span class="monitor-device-status" :class="device.status">
                  {{ device.status === "online" ? "在线" : "离线" }}
                </span>
              </button>
            </div>

            <div v-else class="monitor-live-stage">
              <img
                v-if="activeMonitorDevice"
                :key="monitorPreviewTick"
                class="monitor-live-img"
                :src="monitorLiveUrl"
                :alt="activeMonitorDevice.name"
              />
              <div class="monitor-live-badge">● LIVE</div>
              <div class="monitor-live-label">{{ activeMonitorDevice?.name }}</div>
            </div>

            <div class="visit-actions">
              <button
                v-if="monitorState.status !== 'connected'"
                class="video-btn"
                :disabled="!selectedMonitorId || monitorState.status === 'connecting'"
                @click="connectMonitorDevice"
              >
                {{ monitorState.status === "connecting" ? "连接中…" : "连接监控" }}
              </button>
              <button v-else class="video-btn" @click="refreshMonitorFrame">刷新画面</button>
              <button
                class="video-btn ghost"
                @click="monitorState.status === 'connected' ? disconnectMonitor() : closeRemoteVisit()"
              >
                {{ monitorState.status === "connected" ? "断开监控" : "关闭" }}
              </button>
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- 老人端发起的视频来电（不受家庭监控模式限制） -->
    <div v-if="incomingElderVideoCall" class="incoming-call-overlay" role="dialog" aria-modal="true">
      <div class="incoming-call-card">
        <div class="incoming-call-icon">📹</div>
        <div class="incoming-call-title">{{ familyCallDisplayName(incomingElderVideoCall) }} 发起视频通话</div>
        <div class="incoming-call-sub">可在家居监控界面直接接听，无需先断开监控</div>
        <div class="incoming-call-actions">
          <button type="button" class="incoming-decline" @click="declineElderVideoCall">拒绝</button>
          <button type="button" class="incoming-accept" @click="acceptElderVideoCall">接听</button>
        </div>
      </div>
    </div>

    <!-- Family interaction modal -->
    <div v-if="showFamilyModal" class="modal-overlay">
      <div class="modal-card family-modal">
        <div class="modal-top">
          <div class="modal-title">亲情互动 · 与子女连线</div>
          <button class="modal-close" @click="closeFamilyModal">×</button>
        </div>

        <div class="family-tabs2">
          <button class="tab2" :class="{ active: familyTab === 'chat' }" @click="familyTab = 'chat'">
            聊天记录
          </button>
          <button class="tab2" :class="{ active: familyTab === 'album' }" @click="familyTab = 'album'">
            家庭相册
          </button>
          <button class="tab2" :class="{ active: familyTab === 'video' }" @click="familyTab = 'video'">
            视频通话
          </button>
        </div>

        <div class="modal-body small-pad">
          <div class="family-video-banner" v-if="familyTab === 'video'">
            <button
              class="video-btn"
              :disabled="videoState.status === 'dialing' || videoState.status === 'connected'"
              @click="onMockVideo"
            >
              {{ videoState.status === "dialing" ? "呼叫中..." : videoState.status === "connected" ? "通话进行中" : "发起视频通话" }}
            </button>
            <button class="video-btn ghost" :disabled="videoState.status !== 'connected'" @click="endVideoCall">
              挂断
            </button>
          </div>

          <div v-if="familyTab === 'chat'" class="chat-wrap2">
            <div class="chat-list2">
              <div v-for="m in familyChatMessages" :key="m.id" class="chat-item2" :class="{ mine: m.mine }">
                <div class="bubble2">{{ m.text }}</div>
                <div class="meta2">{{ m.meta }}</div>
              </div>
            </div>
            <div class="chat-input2">
              <input v-model="familyChatInput" class="chat-input-field" placeholder="输入文字或发送语音..." />
              <button class="send-btn" @click="sendFamilyChat">发送</button>
            </div>
          </div>

          <div v-else-if="familyTab === 'album'" class="album-placeholder2">
            <div class="album-tools">
              <button class="video-btn" :disabled="albumUploading" @click="triggerAlbumUpload">
                {{ albumUploading ? "上传中..." : "上传照片" }}
              </button>
              <input
                ref="albumInputRef"
                type="file"
                accept="image/*"
                style="display: none"
                @change="onAlbumFileChange"
              />
              <div class="muted">支持上传家庭照片，方便老人端回看</div>
            </div>
            <div v-if="albumItems.length === 0" class="video-placeholder2">暂无照片，请先上传</div>
            <div v-else class="album-grid">
              <div v-for="item in albumItems" :key="item.id" class="album-photo-card">
                <img class="album-photo" :src="item.url" :alt="item.name" @click="albumPreview = item" />
                <div class="album-meta">
                  <div class="album-name">{{ item.name }}</div>
                  <div class="album-time">{{ item.createdAt }}</div>
                </div>
                <button class="album-del" @click="removeAlbumItem(item.id)">删除</button>
              </div>
            </div>
          </div>
          <div v-else class="video-placeholder2">
            <div class="video-stage visit-stage" :class="{ connected: videoState.status === 'connected' }">
              <div class="video-main">
                <div class="video-title">老人画面</div>
                <video
                  ref="familyRemoteVideoRef"
                  class="video-remote-stream"
                  :class="{ hidden: videoState.status !== 'connected' }"
                  autoplay
                  playsinline
                />
                <div v-if="videoState.status !== 'connected'" class="video-avatar">{{ elderName.slice(0, 1) }}</div>
                <div class="video-status">{{ videoStatusText }} · {{ videoDurationText }}</div>
              </div>
              <video
                ref="familyLocalVideoRef"
                class="video-local-stream"
                :class="{ off: !videoState.cameraOn || videoState.status === 'idle' }"
                autoplay
                playsinline
                muted
              />
            </div>
            <div class="video-controls">
              <button
                class="control-btn"
                :disabled="videoState.status !== 'connected'"
                @click="videoState.muted = !videoState.muted"
              >
                {{ videoState.muted ? "取消静音" : "静音" }}
              </button>
              <button
                class="control-btn"
                :disabled="videoState.status !== 'connected'"
                @click="videoState.cameraOn = !videoState.cameraOn"
              >
                {{ videoState.cameraOn ? "关闭摄像头" : "开启摄像头" }}
              </button>
              <button
                class="control-btn"
                :disabled="videoState.status !== 'connected'"
                @click="videoState.speakerOn = !videoState.speakerOn"
              >
                {{ videoState.speakerOn ? "免提开" : "免提关" }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="albumPreview" class="modal-overlay">
      <div class="modal-card family-modal album-preview">
        <div class="modal-top">
          <div class="modal-title">{{ albumPreview.name }}</div>
          <button class="modal-close" @click="albumPreview = null">×</button>
        </div>
        <div class="modal-body">
          <img class="album-preview-img" :src="albumPreview.url" :alt="albumPreview.name" />
        </div>
      </div>
    </div>

    <!-- Vitals modal -->
    <div v-if="showVitalsModal" class="modal-overlay">
      <div class="modal-card vitals-modal">
        <div class="modal-top">
          <div class="modal-title">手动录入健康数据</div>
          <button class="modal-close" @click="showVitalsModal = false">×</button>
        </div>
        <div class="vitals-form">
          <div class="form-row">
            <div class="form-label">心率 (bpm)</div>
            <div class="stepper">
              <button type="button" class="step-btn" @click="vVitals.hr = Math.max(0, vVitals.hr - 1)">－</button>
              <div class="step-value">{{ vVitals.hr }}</div>
              <button type="button" class="step-btn" @click="vVitals.hr++">＋</button>
            </div>
          </div>
          <div class="form-row">
            <div class="form-label">血压</div>
            <div class="bp-input">
              <input v-model.number="vVitals.sbp" type="number" placeholder="收缩压" />
              <span>/</span>
              <input v-model.number="vVitals.dbp" type="number" placeholder="舒张压" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-label">血氧 (%)</div>
            <div class="stepper">
              <button type="button" class="step-btn" @click="vVitals.spo2 = Math.max(0, vVitals.spo2 - 1)">－</button>
              <div class="step-value">{{ vVitals.spo2 }}</div>
              <button type="button" class="step-btn" @click="vVitals.spo2++">＋</button>
            </div>
          </div>
          <div class="form-row">
            <div class="form-label">血糖 (mmol/L)</div>
            <input v-model.number="vVitals.glucose" type="number" class="wide-input" />
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showVitalsModal = false">取消</button>
          <button class="btn-confirm" @click="submitVitalsFromChild">提交并同步家属</button>
        </div>
      </div>
    </div>

    <!-- Service modal（尺寸与老人端 service-modal 一致） -->
    <div v-if="showServiceModal" class="modal-overlay">
      <div class="modal-card service-modal">
        <div class="modal-top">
          <div class="modal-title">预约居家养老服务</div>
          <button class="modal-close" @click="showServiceModal = false">×</button>
        </div>
        <div class="service-form">
          <div class="form-row">
            <div class="form-label">服务类型</div>
            <select v-model="vService.serviceType" class="wide-select">
              <option value="NURSING">助餐（送餐上门）</option>
              <option value="HOUSEKEEPING">家政保洁</option>
              <option value="ACCOMPANY">陪诊陪护</option>
            </select>
          </div>
          <div class="form-row">
            <div class="form-label">预约时间</div>
            <input v-model="vService.appointmentTime" type="datetime-local" class="wide-input" />
          </div>
          <div class="form-row">
            <div class="form-label">备注</div>
            <input v-model="vService.notes" class="wide-input" placeholder="特殊需求..." />
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showServiceModal = false">取消</button>
          <button class="btn-confirm" @click="confirmServiceOrder">确认预约</button>
        </div>
      </div>
    </div>

    <!-- 服务订单（尺寸与老人端 orders-modal 一致） -->
    <div v-if="showOrdersModal" class="modal-overlay">
      <div class="modal-card orders-modal">
        <div class="modal-top">
          <div class="modal-title">服务订单</div>
          <button class="modal-close" @click="showOrdersModal = false">×</button>
        </div>
        <div class="order-list">
          <div v-for="order in childOrderList" :key="order.id" class="order-item">
            <div class="order-header">
              <span class="order-id">订单号: {{ order.id }}</span>
              <span class="order-status" :class="order.statusClass">{{ order.status }}</span>
            </div>
            <div class="order-body">
              <div class="order-service">{{ order.service }}</div>
              <div class="order-time">{{ order.time }}</div>
            </div>
          </div>
          <div v-if="childOrderList.length === 0" class="no-data">暂无订单</div>
        </div>
        <div class="modal-actions">
          <button class="btn-confirm" @click="showOrdersModal = false">关闭</button>
        </div>
      </div>
    </div>

    <!-- 子女个人信息 -->
    <div v-if="showChildProfileModal" class="modal-overlay">
      <div class="modal-card compact-modal profile-modal">
        <div class="modal-top">
          <div class="modal-title">我的信息（子女）</div>
          <button class="modal-close" @click="showChildProfileModal = false">×</button>
        </div>
        <div class="modal-body small-pad">
          <div v-if="childProfileLoading" class="muted">加载中...</div>
          <div v-else class="profile-form">
            <div class="form-group">
              <label>账号 ID</label>
              <input :value="childForm.childId" class="wide-input2 full" readonly />
            </div>
            <div class="form-group">
              <label>姓名</label>
              <input v-model="childForm.name" class="wide-input2 full" placeholder="请输入姓名" />
            </div>
            <div class="form-group">
              <label>年龄</label>
              <input v-model.number="childForm.age" type="number" min="18" max="120" class="wide-input2 full" />
            </div>
            <div class="form-group">
              <label>性别</label>
              <select v-model="childForm.gender" class="wide-select2 full">
                <option value="男">男</option>
                <option value="女">女</option>
                <option value="其他">其他</option>
              </select>
            </div>
            <div class="form-group">
              <label>与老人关系</label>
              <input v-model="childForm.relationDesc" class="wide-input2 full" placeholder="如：儿子、女儿" />
            </div>
            <div class="form-group">
              <label>联系地址</label>
              <textarea v-model="childForm.address" class="wide-input2 full profile-notes" rows="2" placeholder="您的常住地址" />
            </div>
          </div>
          <div class="modal-actions2">
            <button class="btn-cancel2" @click="showChildProfileModal = false">取消</button>
            <button class="btn-confirm2" :disabled="childProfileSaving" @click="saveChildProfile">
              {{ childProfileSaving ? "保存中..." : "保存" }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 老人个人信息 -->
    <div v-if="showElderProfileModal" class="modal-overlay">
      <div class="modal-card compact-modal profile-modal">
        <div class="modal-top">
          <div class="modal-title">老人信息</div>
          <button class="modal-close" @click="showElderProfileModal = false">×</button>
        </div>
        <div class="modal-body small-pad">
          <div v-if="profileLoading" class="muted">加载中...</div>
          <div v-else class="profile-form">
            <div class="form-group">
              <label>账号 ID</label>
              <input :value="elderForm.elderId" class="wide-input2 full" readonly />
            </div>
            <div class="form-group">
              <label>姓名</label>
              <input v-model="elderForm.name" class="wide-input2 full" placeholder="请输入姓名" />
            </div>
            <div class="form-group">
              <label>年龄</label>
              <input v-model.number="elderForm.age" type="number" min="1" max="120" class="wide-input2 full" />
            </div>
            <div class="form-group">
              <label>性别</label>
              <select v-model="elderForm.gender" class="wide-select2 full">
                <option value="男">男</option>
                <option value="女">女</option>
                <option value="其他">其他</option>
              </select>
            </div>
            <div class="form-group">
              <label>服务住址</label>
              <textarea v-model="elderForm.address" class="wide-input2 full profile-notes" rows="2" placeholder="老人常住地址，用于找医院等" />
            </div>
            <div class="form-group">
              <label>健康备注</label>
              <textarea v-model="elderForm.keyHealthNotes" class="wide-input2 full profile-notes" rows="2" placeholder="如高血压、糖尿病等" />
            </div>
          </div>
          <div class="modal-actions2">
            <button class="btn-cancel2" @click="showElderProfileModal = false">取消</button>
            <button class="btn-confirm2" :disabled="elderProfileSaving" @click="saveElderProfile">
              {{ elderProfileSaving ? "保存中..." : "保存" }}
            </button>
          </div>
        </div>
      </div>
    </div>
    <!-- 智能助手 -->
    <div v-if="showAiChat" class="ai-chat-window">
      <div class="ai-chat-header">
        <span class="ai-chat-title">🤖 智能助手</span>
        <button type="button" class="ai-chat-close" @click="toggleAiChat">×</button>
      </div>
      <div ref="aiChatBodyRef" class="ai-chat-body">
        <div class="ai-message">
          您好！我是子女端智能助手，可解答告警处理、健康守护、预约服务与远程探视等问题。
        </div>
        <div v-for="msg in aiMessages" :key="msg.id" :class="['chat-msg', msg.role === 'user' ? 'user' : 'ai']">
          <div class="msg-bubble">{{ msg.text }}</div>
        </div>
        <div v-if="aiBusy" class="chat-msg ai">
          <div class="msg-bubble ai-thinking">正在思考…</div>
        </div>
      </div>
      <div class="ai-quick-row">
        <button
          v-for="p in aiQuickPrompts"
          :key="p"
          type="button"
          class="ai-quick-chip"
          :disabled="aiBusy"
          @click="applyAiQuick(p)"
        >
          {{ p }}
        </button>
      </div>
      <div class="ai-chat-footer">
        <input
          v-model="aiInput"
          class="ai-input"
          placeholder="请输入您的问题…"
          :disabled="aiBusy"
          @keyup.enter="sendAiMessage"
        />
        <button type="button" class="ai-send-btn" :disabled="aiBusy" @click="sendAiMessage">
          {{ aiBusy ? "…" : "发送" }}
        </button>
      </div>
    </div>

    <PortalHomeModals ref="portalModals" />
    <!-- Toast -->
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
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import type { AlarmDto } from "../../api/alarm";
import { confirmAlarm as confirmAlarmApi, listAlarms } from "../../api/alarm";
import type { ChildProfileDto } from "../../api/child";
import { getChildProfile, upsertChildProfile } from "../../api/child";
import type { ElderProfileDto } from "../../api/elder";
import { getElderProfile, upsertElderProfile } from "../../api/elder";
import {
  createServiceOrder,
  formatServiceOrderError,
  listServiceOrders,
  serviceOrderStatusClass,
  type ServiceOrderDto
} from "../../api/serviceOrder";
import { childSessionUserId, childSessionUserName } from "../../utils/session";
import ElderLocationMap from "../../components/ElderLocationMap.vue";
import PortalHomeModals from "../../components/portal/PortalHomeModals.vue";
import { fetchNews, type PortalNews } from "../../api/portal";
import { resolveMediaUrl } from "../../utils/mediaUrl";
import {
  FAMILY_CHAT_POLL_MS,
  deleteFamilyAlbum,
  listFamilyAlbum,
  listFamilyMessages,
  mapFamilyAlbumToUi,
  mapFamilyChatToUi,
  sendFamilyMessage,
  uploadFamilyAlbum
} from "../../api/family";
import {
  applyStreamTrackState,
  bindStreamToVideo,
  FamilyVideoRtc,
  openLocalCamera
} from "../../utils/familyVideoRtc";
import {
  clearFamilyVideoSession,
  familyCallDisplayName,
  patchFamilyVideoSession,
  readFamilyVideoSession,
  subscribeFamilyVideoSession,
  writeFamilyVideoSession,
  type FamilyVideoCallSession
} from "../../utils/familyVideoSignal";
import {
  loadMonitorDevices,
  monitorPreviewUrl,
  type HomeMonitorDevice
} from "../../utils/familyMonitorDevices";
import { deepSeekChat, type ChatTurn } from "../../api/deepseek";
import { postMeasurement } from "../../api/measurement";
import {
  ALARM_POLL_INTERVAL_MS,
  alarmDescription,
  alarmStatusLabel,
  alarmTypeLabel,
  formatAlarmTime,
  isPendingAlarm,
  isSosAlarm
} from "../../utils/alarmDisplay";

type AlarmUiItem = {
  id: string;
  triggeredAt: string;
  type: string;
  status: string;
  subtitle: string;
  desc: string;
  statusLabel: string;
  isSos: boolean;
  isPending: boolean;
};

const portalModals = ref<InstanceType<typeof PortalHomeModals> | null>(null);
const newsList = ref<PortalNews[]>([]);
const newsLoading = ref(false);
const childTab = ref<"home" | "health" | "mine">("home");

const showAiChat = ref(false);
const aiInput = ref("");
const aiBusy = ref(false);
const aiChatBodyRef = ref<HTMLElement | null>(null);
const aiMessages = ref<Array<{ id: string; role: "user" | "assistant"; text: string }>>([]);
const aiQuickPrompts = [
  "告警收到后怎么处理？",
  "如何为父母预约服务？",
  "远程探视怎么使用？",
  "健康页数据怎么看？"
];

function toggleAiChat() {
  showAiChat.value = !showAiChat.value;
}

async function scrollAiChatBottom() {
  await nextTick();
  const el = aiChatBodyRef.value;
  if (el) el.scrollTop = el.scrollHeight;
}

async function sendAiMessage() {
  const text = aiInput.value.trim();
  if (!text || aiBusy.value) return;
  aiMessages.value.push({ id: `u-${Date.now()}`, role: "user", text });
  aiInput.value = "";
  aiBusy.value = true;
  await scrollAiChatBottom();
  try {
    const turns: ChatTurn[] = aiMessages.value.map(m => ({ role: m.role, text: m.text }));
    const reply = await deepSeekChat(turns);
    aiMessages.value.push({ id: `a-${Date.now()}`, role: "assistant", text: reply });
  } catch (e: unknown) {
    aiMessages.value.push({
      id: `a-${Date.now()}`,
      role: "assistant",
      text: e instanceof Error ? e.message : "助手暂时不可用，请稍后再试。"
    });
  } finally {
    aiBusy.value = false;
    await scrollAiChatBottom();
  }
}

function applyAiQuick(p: string) {
  aiInput.value = p;
  void sendAiMessage();
}
const childUserId = ref(childSessionUserId());
const elderId = ref(localStorage.getItem("elder.id") ?? "");

const showElderSwitchModal = ref(false);
const linkedElders = ref<string[]>([]);
const elderOptions = ref<ElderProfileDto[]>([]);

const profile = ref<ElderProfileDto | null>(null);
const childProfile = ref<ChildProfileDto | null>(null);
const profileLoading = ref(false);
const childProfileLoading = ref(false);
const showChildProfileModal = ref(false);
const showElderProfileModal = ref(false);
const childProfileSaving = ref(false);
const elderProfileSaving = ref(false);

const childForm = reactive({
  childId: "",
  name: "",
  age: 40,
  gender: "男",
  address: "",
  relationDesc: ""
});

const elderForm = reactive({
  elderId: "",
  name: "",
  age: 72,
  gender: "男",
  address: "",
  keyHealthNotes: ""
});

const vitals = reactive({
  hr: 79,
  sbp: 127,
  dbp: 82,
  spo2: 97
});
const steps = 3534;
const todayDesc = computed(() => "今日状态：休息正常，建议多喝水");
const latestContactText = computed(() => "最近联系：刚刚");

const childDisplayName = computed(
  () => childProfile.value?.name?.trim() || childSessionUserName() || childUserId.value || "未登录"
);
const elderDisplayName = computed(() => {
  if (profile.value?.name?.trim()) return profile.value.name;
  const cached = localStorage.getItem("elder.linkedElderName");
  if (cached?.trim()) return cached;
  if (elderId.value) return elderId.value;
  return "未绑定老人";
});

const elderName = computed(() => profile.value?.name?.trim() || elderDisplayName.value);
const homeSearch = ref("");
const stepsYesterday = computed(() => Math.max(0, steps - 420));

const bannerSlides = [
  { src: "/images/banner-home.png", alt: "远程守护 · 银发智盾" },
  { src: "/images/banner-home-2.png", alt: "亲情相伴 · 幸福晚年" }
];
const bannerIndex = ref(0);
let bannerTimer: number | undefined;

function startBannerTimer() {
  stopBannerTimer();
  bannerTimer = window.setInterval(() => {
    bannerIndex.value = (bannerIndex.value + 1) % bannerSlides.length;
  }, 4500);
}

function stopBannerTimer() {
  if (bannerTimer !== undefined) {
    window.clearInterval(bannerTimer);
    bannerTimer = undefined;
  }
}

function setBanner(i: number) {
  bannerIndex.value = i;
}

function goHealthTab(tab: "latest" | "history" | "map" = "latest") {
  childTab.value = "health";
  alarmTab.value = tab;
}

const alarmTab = ref<"latest" | "history" | "map">("latest");
const alarms = ref<AlarmDto[]>([]);
const pendingAlarmCount = computed(
  () => alarms.value.filter((a) => a.status !== "CONFIRMED" && a.status !== "CLOSED").length
);
const activeSosAlarm = computed(
  () => alarms.value.find(a => isSosAlarm(a) && isPendingAlarm(a)) ?? null
);

const alarmsUi = computed<AlarmUiItem[]>(() => {
  const list = [...alarms.value].sort((a, b) => (a.triggeredAt < b.triggeredAt ? 1 : -1));
  const ui = list.map((a) => ({
    id: a.id,
    triggeredAt: formatAlarmTime(a.triggeredAt),
    type: a.type,
    status: a.status,
    subtitle: alarmTypeLabel(a.type),
    desc: alarmDescription(a),
    statusLabel: alarmStatusLabel(a.status),
    isSos: isSosAlarm(a),
    isPending: isPendingAlarm(a)
  }));

  if (alarmTab.value === "latest") return ui.slice(0, 3);
  if (alarmTab.value === "history") return ui.slice(0, 10);
  return ui;
});

let knownChildSosId: string | null = null;
let alarmPollTimer: number | undefined;

function startAlarmPolling() {
  stopAlarmPolling();
  alarmPollTimer = window.setInterval(() => {
    void loadAlarms();
  }, ALARM_POLL_INTERVAL_MS);
}

function stopAlarmPolling() {
  if (alarmPollTimer !== undefined) {
    window.clearInterval(alarmPollTimer);
    alarmPollTimer = undefined;
  }
}

async function loadProfile() {
  if (!elderId.value) {
    profile.value = null;
    return;
  }
  profileLoading.value = true;
  try {
    profile.value = await getElderProfile(elderId.value);
    if (profile.value?.name) {
      localStorage.setItem("elder.linkedElderName", profile.value.name);
    }
  } catch {
    profile.value = null;
  } finally {
    profileLoading.value = false;
  }
}

async function loadChildProfile() {
  if (!childUserId.value) {
    childProfile.value = null;
    return;
  }
  childProfileLoading.value = true;
  try {
    childProfile.value = await getChildProfile(childUserId.value);
  } catch {
    childProfile.value = null;
  } finally {
    childProfileLoading.value = false;
  }
}

function syncChildFormFromProfile() {
  const p = childProfile.value;
  childForm.childId = p?.childId ?? childUserId.value;
  childForm.name = p?.name ?? "";
  childForm.age = p?.age ?? 40;
  childForm.gender = p?.gender || "男";
  childForm.address = p?.address ?? "";
  childForm.relationDesc = p?.relationDesc ?? "";
}

function syncElderFormFromProfile() {
  const p = profile.value;
  elderForm.elderId = p?.elderId ?? elderId.value;
  elderForm.name = p?.name ?? "";
  elderForm.age = p?.age ?? 72;
  elderForm.gender = p?.gender || "男";
  elderForm.address = p?.address ?? "";
  elderForm.keyHealthNotes = p?.keyHealthNotes ?? "";
}

async function openChildProfileModal() {
  showChildProfileModal.value = true;
  await loadChildProfile();
  syncChildFormFromProfile();
}

async function openElderProfileModal() {
  if (!elderId.value) {
    toast.value = { icon: "!", title: "未绑定老人", desc: "请先切换或绑定老人账号" };
    return;
  }
  showElderProfileModal.value = true;
  await loadProfile();
  syncElderFormFromProfile();
}

function openElderHealthNotes() {
  openElderProfileModal();
}

function openChangePassword() {
  toast.value = { icon: "🔒", title: "修改密码", desc: "功能开发中，请联系管理员重置密码" };
}

async function saveChildProfile() {
  if (!childUserId.value) return;
  if (!childForm.name.trim()) {
    toast.value = { icon: "!", title: "提示", desc: "请输入姓名" };
    return;
  }
  childProfileSaving.value = true;
  try {
    const saved = await upsertChildProfile(childUserId.value, {
      name: childForm.name.trim(),
      gender: childForm.gender,
      age: childForm.age,
      address: childForm.address.trim(),
      relationDesc: childForm.relationDesc.trim()
    });
    childProfile.value = saved;
    localStorage.setItem("child.userName", saved.name);
    showChildProfileModal.value = false;
    toast.value = { icon: "✓", title: "保存成功", desc: "子女信息已更新" };
  } catch (e: unknown) {
    toast.value = { icon: "!", title: "保存失败", desc: (e as { message?: string })?.message ?? "请稍后重试" };
  } finally {
    childProfileSaving.value = false;
  }
}

async function saveElderProfile() {
  if (!elderId.value) return;
  if (!elderForm.name.trim()) {
    toast.value = { icon: "!", title: "提示", desc: "请输入老人姓名" };
    return;
  }
  elderProfileSaving.value = true;
  try {
    const saved = await upsertElderProfile(elderId.value, {
      name: elderForm.name.trim(),
      gender: elderForm.gender,
      address: elderForm.address.trim() || "未填写",
      age: elderForm.age,
      keyHealthNotes: elderForm.keyHealthNotes.trim()
    });
    profile.value = saved;
    await loadElderOptionsIfNeeded();
    showElderProfileModal.value = false;
    toast.value = { icon: "✓", title: "保存成功", desc: "老人信息已更新" };
  } catch (e: unknown) {
    toast.value = { icon: "!", title: "保存失败", desc: (e as { message?: string })?.message ?? "请稍后重试" };
  } finally {
    elderProfileSaving.value = false;
  }
}

async function loadAlarms() {
  if (!elderId.value) return;
  const list = await listAlarms(elderId.value, 20);
  const sos = list.find(a => isSosAlarm(a) && isPendingAlarm(a));
  if (sos && sos.id !== knownChildSosId) {
    knownChildSosId = sos.id;
    toast.value = {
      icon: "🆘",
      title: "SOS 紧急求助",
      desc: `${profile.value?.name ?? "老人"} 已触发一键呼救，请立即联系并确认安全`
    };
  }
  if (!sos) knownChildSosId = null;
  alarms.value = list;
}

async function confirmAlarmAction(id: string) {
  await confirmAlarmApi(id, "child_app_confirmed");
  await loadAlarms();
  toast.value = { icon: "✓", title: "已确认处理", desc: "告警已更新并停止升级链路（示例）。" };
}

function confirmAlarm(id: string) {
  void confirmAlarmAction(id);
}

function loadLinkedEldersFromLS() {
  const raw = localStorage.getItem("elder.linkedElders");
  if (!raw) return;
  try {
    const parsed = JSON.parse(raw) as string[];
    if (Array.isArray(parsed)) linkedElders.value = parsed;
  } catch {
    // ignore
  }

  if (linkedElders.value.length === 0 && elderId.value) {
    linkedElders.value = [elderId.value];
  }
}

async function loadElderOptionsIfNeeded() {
  if (elderOptions.value.length > 0) return;
  const ids = linkedElders.value;
  if (ids.length === 0) return;

  const profiles = await Promise.all(
    ids.map(async (id) => {
      try {
        return await getElderProfile(id);
      } catch {
        // Fallback for broken records
        return {
          elderId: id,
          name: id,
          age: null,
          gender: "",
          address: "",
          keyHealthNotes: ""
        } as ElderProfileDto;
      }
    })
  );

  elderOptions.value = profiles;
}

async function onSwitchElder() {
  showElderSwitchModal.value = true;
  loadLinkedEldersFromLS();
  await loadElderOptionsIfNeeded();
}

async function selectElder(newElderId: string) {
  showElderSwitchModal.value = false;
  localStorage.setItem("elder.id", newElderId);
  elderId.value = newElderId;

  // Update UI data
  await loadProfile();
  await loadAlarms();
  loadVitalsFromLocal();
  reloadMonitorDevices();
  if (childTab.value === "mine") {
    syncElderFormFromProfile();
  }

  window.dispatchEvent(new CustomEvent("elder-switched", { detail: { elderId: newElderId } }));
}

// Weekly report
const showWeeklyReport = ref(false);

const toast = ref<{ icon: string; title: string; desc: string } | null>(null);
function onExportWeekly() {
  showWeeklyReport.value = false;
  toast.value = { icon: "✓", title: "健康报告已生成PDF并发送至您邮箱（示例）", desc: "如需真实导出，可接入后端报表服务。" };
}

// Family interaction
const showFamilyModal = ref(false);
const showRemoteVisitModal = ref(false);
const visitMode = ref<"elder" | "monitor">("elder");
const monitorDevices = ref<HomeMonitorDevice[]>([]);
const selectedMonitorId = ref("");
const monitorPreviewTick = ref(0);
const monitorState = reactive({
  status: "idle" as "idle" | "connecting" | "connected",
  connectedDeviceId: ""
});
let monitorRefreshTimer: number | null = null;
let monitorConnectTimer: number | null = null;

const activeMonitorDevice = computed(() =>
  monitorDevices.value.find((d) => d.id === (monitorState.connectedDeviceId || selectedMonitorId.value))
);

const monitorLiveUrl = computed(() => {
  const device = monitorDevices.value.find((d) => d.id === monitorState.connectedDeviceId);
  if (!device) return "";
  return monitorPreviewUrl(device, monitorPreviewTick.value);
});

const familyTab = ref<"chat" | "album" | "video">("chat");
const localVideoRef = ref<HTMLVideoElement | null>(null);
const remoteVideoRef = ref<HTMLVideoElement | null>(null);
const familyLocalVideoRef = ref<HTMLVideoElement | null>(null);
const familyRemoteVideoRef = ref<HTMLVideoElement | null>(null);
const incomingElderVideoCall = ref<FamilyVideoCallSession | null>(null);
let localMediaStream: MediaStream | null = null;
let familyRtc: FamilyVideoRtc | null = null;
let signalPollTimer: number | null = null;
let unsubscribeChildVideo: (() => void) | null = null;
let activeVideoCallId = "";

function activeLocalVideoEl() {
  if (showRemoteVisitModal.value && visitMode.value === "elder") return localVideoRef.value;
  if (showFamilyModal.value && familyTab.value === "video") return familyLocalVideoRef.value;
  return localVideoRef.value ?? familyLocalVideoRef.value;
}

function activeRemoteVideoEl() {
  if (showRemoteVisitModal.value && visitMode.value === "elder") return remoteVideoRef.value;
  if (showFamilyModal.value && familyTab.value === "video") return familyRemoteVideoRef.value;
  return remoteVideoRef.value ?? familyRemoteVideoRef.value;
}

const familyChatInput = ref("");
const familyChatMessages = ref<{ id: string; mine: boolean; text: string; meta: string }[]>([]);
let familySyncPollTimer: number | undefined;

async function loadFamilyChat() {
  if (!elderId.value) return;
  try {
    const rows = await listFamilyMessages(elderId.value, 100);
    familyChatMessages.value = mapFamilyChatToUi(rows, "CHILD");
  } catch {
    /* keep last messages */
  }
}

async function loadFamilyAlbum() {
  if (!elderId.value) return;
  try {
    albumItems.value = mapFamilyAlbumToUi(await listFamilyAlbum(elderId.value));
  } catch {
    albumItems.value = [];
  }
}

function startFamilySyncPoll() {
  stopFamilySyncPoll();
  void loadFamilyChat();
  void loadFamilyAlbum();
  familySyncPollTimer = window.setInterval(() => {
    void loadFamilyChat();
    if (showFamilyModal.value && familyTab.value === "album") void loadFamilyAlbum();
  }, FAMILY_CHAT_POLL_MS);
}

function stopFamilySyncPoll() {
  if (familySyncPollTimer !== undefined) {
    window.clearInterval(familySyncPollTimer);
    familySyncPollTimer = undefined;
  }
}

async function sendFamilyChat() {
  const text = familyChatInput.value.trim();
  const eid = elderId.value;
  if (!text || !eid) return;
  try {
    await sendFamilyMessage(eid, {
      senderRole: "CHILD",
      senderUserId: childUserId.value || undefined,
      senderName: childDisplayName.value,
      content: text
    });
    familyChatInput.value = "";
    await loadFamilyChat();
  } catch (e: unknown) {
    toast.value = { icon: "!", title: "发送失败", desc: (e as { message?: string })?.message ?? "请稍后重试" };
  }
}

type AlbumItem = {
  id: string;
  name: string;
  url: string;
  createdAt: string;
};

const albumInputRef = ref<HTMLInputElement | null>(null);
const albumUploading = ref(false);
const albumItems = ref<AlbumItem[]>([]);
const albumPreview = ref<AlbumItem | null>(null);

function triggerAlbumUpload() {
  albumInputRef.value?.click();
}

async function onAlbumFileChange(event: Event) {
  const input = event.target as HTMLInputElement;
  const files = input.files;
  const eid = elderId.value;
  if (!files || files.length === 0 || !eid) return;
  const file = files[0];
  albumUploading.value = true;
  try {
    await uploadFamilyAlbum(eid, file, "CHILD", childUserId.value || undefined);
    await loadFamilyAlbum();
    toast.value = { icon: "✓", title: "照片已上传", desc: "已同步至家庭相册，老人端可查看。" };
  } catch (e: unknown) {
    toast.value = { icon: "!", title: "上传失败", desc: (e as { message?: string })?.message ?? "请重试" };
  } finally {
    albumUploading.value = false;
    input.value = "";
  }
}

async function removeAlbumItem(id: string) {
  const eid = elderId.value;
  if (!eid) return;
  try {
    await deleteFamilyAlbum(eid, id);
    await loadFamilyAlbum();
    if (albumPreview.value?.id === id) albumPreview.value = null;
  } catch (e: unknown) {
    toast.value = { icon: "!", title: "删除失败", desc: (e as { message?: string })?.message ?? "请重试" };
  }
}

const videoState = reactive({
  status: "idle" as "idle" | "dialing" | "connected" | "ended",
  muted: false,
  cameraOn: true,
  speakerOn: true,
  durationSec: 0
});
let videoCallTimer: number | null = null;
let videoConnectTimer: number | null = null;
const videoStatusText = computed(() => {
  if (videoState.status === "dialing") return "正在呼叫老人设备...";
  if (videoState.status === "connected") return "通话中";
  if (videoState.status === "ended") return "通话已结束";
  return "待发起";
});

const videoDurationText = computed(() => {
  const mm = String(Math.floor(videoState.durationSec / 60)).padStart(2, "0");
  const ss = String(videoState.durationSec % 60).padStart(2, "0");
  return `${mm}:${ss}`;
});

function clearVideoTimers() {
  if (videoCallTimer != null) {
    window.clearInterval(videoCallTimer);
    videoCallTimer = null;
  }
  if (videoConnectTimer != null) {
    window.clearTimeout(videoConnectTimer);
    videoConnectTimer = null;
  }
}

function clearSignalPoll() {
  if (signalPollTimer != null) {
    window.clearInterval(signalPollTimer);
    signalPollTimer = null;
  }
}

function stopLocalMedia() {
  familyRtc?.stop();
  familyRtc = null;
  if (localMediaStream) {
    localMediaStream.getTracks().forEach((t) => t.stop());
    localMediaStream = null;
  }
  bindStreamToVideo(localVideoRef.value, null);
  bindStreamToVideo(remoteVideoRef.value, null);
  bindStreamToVideo(familyLocalVideoRef.value, null);
  bindStreamToVideo(familyRemoteVideoRef.value, null);
}

async function startFamilyRtc(role: "caller" | "callee") {
  const eid = elderId.value;
  const callId = activeVideoCallId;
  if (!eid || !callId || !localMediaStream) return;
  familyRtc?.stop();
  familyRtc = new FamilyVideoRtc(eid, callId, role, (remote) => {
    bindStreamToVideo(activeRemoteVideoEl(), remote);
  });
  await familyRtc.start(localMediaStream);
}

async function ensureLocalMedia() {
  if (!localMediaStream) {
    localMediaStream = await openLocalCamera();
  }
  await nextTick();
  bindStreamToVideo(activeLocalVideoEl(), localMediaStream);
  applyStreamTrackState(localMediaStream, videoState.muted, videoState.cameraOn);
}

function applyVideoTrackState() {
  applyStreamTrackState(localMediaStream, videoState.muted, videoState.cameraOn);
}

function toggleVideoMute() {
  videoState.muted = !videoState.muted;
  applyVideoTrackState();
}

function toggleVideoCamera() {
  videoState.cameraOn = !videoState.cameraOn;
  applyVideoTrackState();
}

async function beginConnectedTimer() {
  if (videoState.status === "connected") return;
  clearVideoTimers();
  videoState.status = "connected";
  videoCallTimer = window.setInterval(() => {
    videoState.durationSec += 1;
  }, 1000);
  try {
    await startFamilyRtc("caller");
    bindStreamToVideo(activeLocalVideoEl(), localMediaStream);
  } catch (e: unknown) {
    toast.value = { icon: "!", title: "视频连接失败", desc: (e as Error)?.message ?? "请重试" };
  }
}

function startSignalPoll() {
  clearSignalPoll();
  const eid = elderId.value;
  if (!eid || !activeVideoCallId) return;
  signalPollTimer = window.setInterval(() => {
    void (async () => {
      const session = await readFamilyVideoSession(eid);
      if (!session || session.callId !== activeVideoCallId) return;
      if (session.status === "connected" && videoState.status === "dialing") {
      void beginConnectedTimer();
      toast.value = { icon: "📹", title: "已接通", desc: `${elderName.value} 已接听远程探视。` };
    }
      if (session.status === "declined" && videoState.status === "dialing") {
        finishVideoCall("老人暂未接听", false);
      }
      if (session.status === "ended" && (videoState.status === "connected" || videoState.status === "dialing")) {
        finishVideoCall("对方已挂断", false);
      }
    })();
  }, 500);
}

async function writeRingingSignal() {
  const eid = elderId.value;
  if (!eid) return;
  const session = await writeFamilyVideoSession({
    elderId: eid,
    childId: childUserId.value,
    childName: childDisplayName.value,
    elderDisplayName: "",
    initiatorRole: "CHILD"
  });
  activeVideoCallId = session.callId;
}

async function startVideoCall() {
  const eid = elderId.value;
  if (!eid) {
    toast.value = { icon: "!", title: "未绑定老人", desc: "请先切换并绑定老人账号。" };
    return;
  }
  if (videoState.status === "dialing" || videoState.status === "connected") return;

  if (monitorState.status === "connected" || monitorState.status === "connecting") {
    disconnectMonitor();
  }

  clearVideoTimers();
  clearSignalPoll();
  videoState.status = "dialing";
  videoState.durationSec = 0;
  try {
    await writeRingingSignal();
  } catch (e: unknown) {
    finishVideoCall((e as Error)?.message ?? "呼叫失败", false);
    return;
  }
  startSignalPoll();

  if (!localMediaStream) {
    try {
      await ensureLocalMedia();
    } catch (e: unknown) {
      finishVideoCall((e as Error)?.message ?? "无法开启摄像头", false);
      toast.value = {
        icon: "!",
        title: "无法开启摄像头",
        desc: (e as Error)?.message ?? "请在浏览器中允许摄像头与麦克风权限。"
      };
      return;
    }
  }

  toast.value = { icon: "📹", title: "正在呼叫", desc: `已向 ${elderName.value} 发起远程探视，请等待老人端接听。` };
}

function finishVideoCall(message?: string, notifyElder = true) {
  clearVideoTimers();
  clearSignalPoll();
  videoState.status = "ended";
  const eid = elderId.value;
  const callId = activeVideoCallId;
  if (notifyElder && eid && callId) {
    void patchFamilyVideoSession(eid, callId, "ended").then(() => clearFamilyVideoSession(eid, callId));
  }
  activeVideoCallId = "";
  if (message) {
    toast.value = { icon: "📹", title: "探视结束", desc: message };
  }
}

function endVideoCall() {
  finishVideoCall();
}

function resetVideoCall() {
  clearVideoTimers();
  clearSignalPoll();
  videoState.status = "idle";
  videoState.durationSec = 0;
  videoState.muted = false;
  videoState.cameraOn = true;
  videoState.speakerOn = true;
  activeVideoCallId = "";
}

function onMockVideo() {
  if (videoState.status === "connected" || videoState.status === "dialing") return;
  void startVideoCall();
}

function reloadMonitorDevices() {
  if (!elderId.value) {
    monitorDevices.value = [];
    return;
  }
  monitorDevices.value = loadMonitorDevices(elderId.value);
  if (!monitorDevices.value.some((d) => d.id === selectedMonitorId.value)) {
    const firstOnline = monitorDevices.value.find((d) => d.status === "online");
    selectedMonitorId.value = firstOnline?.id ?? monitorDevices.value[0]?.id ?? "";
  }
}

function clearMonitorTimers() {
  if (monitorRefreshTimer != null) {
    window.clearInterval(monitorRefreshTimer);
    monitorRefreshTimer = null;
  }
  if (monitorConnectTimer != null) {
    window.clearTimeout(monitorConnectTimer);
    monitorConnectTimer = null;
  }
}

function resetMonitorView() {
  clearMonitorTimers();
  monitorState.status = "idle";
  monitorState.connectedDeviceId = "";
  monitorPreviewTick.value = 0;
}

function startMonitorLiveRefresh() {
  clearMonitorTimers();
  monitorRefreshTimer = window.setInterval(() => {
    monitorPreviewTick.value += 1;
  }, 4000);
}

function setVisitMode(mode: "elder" | "monitor") {
  if (visitMode.value === mode) return;
  visitMode.value = mode;
  if (mode === "monitor") {
    if ((videoState.status === "dialing" || videoState.status === "connected") && !incomingElderVideoCall.value) {
      finishVideoCall("已切换至家庭监控", true);
      resetVideoCall();
    }
    if (videoState.status === "idle") {
      stopLocalMedia();
    }
    reloadMonitorDevices();
  } else {
    disconnectMonitor();
    void nextTick().then(() => {
      if (localMediaStream) bindStreamToVideo(activeLocalVideoEl(), localMediaStream);
    });
  }
}

async function acceptElderVideoCall() {
  const session = incomingElderVideoCall.value;
  if (!session) return;
  incomingElderVideoCall.value = null;
  activeVideoCallId = session.callId;
  visitMode.value = "elder";
  showRemoteVisitModal.value = true;
  showFamilyModal.value = false;
  if (monitorState.status === "connected" || monitorState.status === "connecting") {
    disconnectMonitor();
  }
  clearVideoTimers();
  clearSignalPoll();
  videoState.durationSec = 0;
  try {
    await patchFamilyVideoSession(session.elderId, session.callId, "connected");
    await ensureLocalMedia();
    videoState.status = "connected";
    videoCallTimer = window.setInterval(() => {
      videoState.durationSec += 1;
    }, 1000);
    await startFamilyRtc("callee");
    bindStreamToVideo(activeLocalVideoEl(), localMediaStream);
    startSignalPoll();
    toast.value = { icon: "📹", title: "已接听", desc: `${familyCallDisplayName(session)} 的视频通话已接通` };
  } catch (e: unknown) {
    finishVideoCall((e as Error)?.message ?? "接听失败", false);
  }
}

async function declineElderVideoCall() {
  const session = incomingElderVideoCall.value;
  if (!session) return;
  incomingElderVideoCall.value = null;
  await patchFamilyVideoSession(session.elderId, session.callId, "declined");
  window.setTimeout(() => void clearFamilyVideoSession(session.elderId, session.callId), 1500);
  toast.value = { icon: "📹", title: "已拒绝", desc: "已拒绝老人的视频通话" };
}

function connectMonitorDevice() {
  const device = monitorDevices.value.find((d) => d.id === selectedMonitorId.value);
  if (!device) {
    toast.value = { icon: "!", title: "请选择设备", desc: "请先选择要连接的监控摄像头。" };
    return;
  }
  if (device.status === "offline") {
    toast.value = { icon: "!", title: "设备离线", desc: `${device.name} 当前离线，请检查设备电源与网络。` };
    return;
  }
  if (monitorState.status === "connecting" || monitorState.status === "connected") return;

  monitorState.status = "connecting";
  monitorConnectTimer = window.setTimeout(() => {
    monitorState.status = "connected";
    monitorState.connectedDeviceId = device.id;
    monitorPreviewTick.value = Date.now();
    startMonitorLiveRefresh();
    toast.value = {
      icon: "📡",
      title: "监控已连接",
      desc: `正在查看 ${device.location} · ${device.name} 实况画面。`
    };
  }, 1200);
}

function disconnectMonitor() {
  resetMonitorView();
}

function refreshMonitorFrame() {
  if (monitorState.status !== "connected") return;
  monitorPreviewTick.value = Date.now();
  toast.value = { icon: "↻", title: "画面已刷新", desc: "已拉取最新监控画面。" };
}

async function openRemoteVisit() {
  if (!elderId.value) {
    toast.value = { icon: "!", title: "请先绑定老人", desc: "切换并绑定老人账号后再远程探视。" };
    onSwitchElder();
    return;
  }
  visitMode.value = "elder";
  showRemoteVisitModal.value = true;
  resetVideoCall();
  resetMonitorView();
  reloadMonitorDevices();
  await nextTick();
}

function closeRemoteVisit() {
  showRemoteVisitModal.value = false;
  disconnectMonitor();
  finishVideoCall(undefined, true);
  resetVideoCall();
  stopLocalMedia();
  visitMode.value = "elder";
}

function openFamilyModal() {
  showFamilyModal.value = true;
  familyTab.value = "chat";
  startFamilySyncPoll();
}

function closeFamilyModal() {
  showFamilyModal.value = false;
  stopFamilySyncPoll();
  if (!showRemoteVisitModal.value) {
    finishVideoCall(undefined, true);
    resetVideoCall();
    stopLocalMedia();
  }
}

// Vitals modal
const showVitalsModal = ref(false);
const vVitals = reactive({
  hr: 78,
  sbp: 125,
  dbp: 80,
  spo2: 96,
  glucose: 5.6
});

function vitalsStorageKey() {
  return elderId.value ? `child.vitals.${elderId.value}` : "";
}

function syncModalFromVitals() {
  vVitals.hr = vitals.hr;
  vVitals.sbp = vitals.sbp;
  vVitals.dbp = vitals.dbp;
  vVitals.spo2 = vitals.spo2;
}

function saveVitalsToLocal() {
  const key = vitalsStorageKey();
  if (!key) return;
  localStorage.setItem(
    key,
    JSON.stringify({
      hr: vitals.hr,
      sbp: vitals.sbp,
      dbp: vitals.dbp,
      spo2: vitals.spo2,
      glucose: vVitals.glucose
    })
  );
}

function loadVitalsFromLocal() {
  syncModalFromVitals();
  const key = vitalsStorageKey();
  if (!key) return;
  const raw = localStorage.getItem(key);
  if (!raw) return;
  try {
    const parsed = JSON.parse(raw) as {
      hr?: number;
      sbp?: number;
      dbp?: number;
      spo2?: number;
      glucose?: number;
    };
    if (typeof parsed.hr === "number") vitals.hr = parsed.hr;
    if (typeof parsed.sbp === "number") vitals.sbp = parsed.sbp;
    if (typeof parsed.dbp === "number") vitals.dbp = parsed.dbp;
    if (typeof parsed.spo2 === "number") vitals.spo2 = parsed.spo2;
    if (typeof parsed.glucose === "number") vVitals.glucose = parsed.glucose;
    syncModalFromVitals();
  } catch {
    // ignore malformed cache
  }
}

function openVitalsModal() {
  syncModalFromVitals();
  showVitalsModal.value = true;
}

async function submitVitalsFromChild() {
  vitals.hr = Math.max(30, Math.min(220, Number(vVitals.hr) || vitals.hr));
  vitals.sbp = Math.max(70, Math.min(260, Number(vVitals.sbp) || vitals.sbp));
  vitals.dbp = Math.max(40, Math.min(180, Number(vVitals.dbp) || vitals.dbp));
  vitals.spo2 = Math.max(60, Math.min(100, Number(vVitals.spo2) || vitals.spo2));
  syncModalFromVitals();
  saveVitalsToLocal();
  showVitalsModal.value = false;
  let desc = "顶部指标卡已更新并按当前老人保存。";
  if (elderId.value) {
    try {
      const result = await postMeasurement({
        deviceId: `child-manual-${elderId.value}`,
        elderId: elderId.value,
        timestampMillis: Date.now(),
        heartRate: vitals.hr || null,
        systolic: vitals.sbp || null,
        diastolic: vitals.dbp || null
      });
      desc = result.alarmCreated
        ? "已同步至平台并触发体征告警，请关注告警列表"
        : "已同步至平台，家属端与机构端可查看最新体征";
    } catch (e) {
      desc = e instanceof Error ? e.message : "平台同步失败，数据仅保存在本机";
    }
  }
  toast.value = { icon: "✓", title: "健康数据已同步", desc };
}

// Service modal
const showServiceModal = ref(false);
function getDefaultDatetimeLocal() {
  const d = new Date(Date.now() + 1000 * 60 * 60);
  const pad = (n: number) => String(n).padStart(2, "0");
  const yyyy = d.getFullYear();
  const mm = pad(d.getMonth() + 1);
  const dd = pad(d.getDate());
  const hh = pad(d.getHours());
  const mi = pad(d.getMinutes());
  return `${yyyy}-${mm}-${dd}T${hh}:${mi}`;
}

const vService = reactive({
  serviceType: "NURSING",
  appointmentTime: getDefaultDatetimeLocal(),
  notes: "特护理需求..."
});

async function confirmServiceOrder() {
  if (!elderId.value) {
    toast.value = { icon: "!", title: "未绑定老人", desc: "请先切换并绑定老人账号" };
    return;
  }
  const appt = new Date(vService.appointmentTime);
  if (Number.isNaN(appt.getTime())) {
    toast.value = { icon: "!", title: "提示", desc: "请选择有效的预约时间" };
    return;
  }
  try {
    await createServiceOrder(elderId.value, {
      serviceType: vService.serviceType,
      appointmentTimeMillis: appt.getTime(),
      notes: vService.notes?.trim() || "",
      bookedByRole: "CHILD",
      bookedByUserId: childUserId.value,
      bookedByName: childProfile.value?.name || childSessionUserName() || ""
    });
    showServiceModal.value = false;
    toast.value = { icon: "✓", title: "预约已提交", desc: "已同步至服务机构，请等待派单" };
  } catch (e: unknown) {
    toast.value = { icon: "!", title: "预约失败", desc: formatServiceOrderError(e) };
  }
}

function openVolunteer() {
  toast.value = { icon: "❤️", title: "帮扶志愿者", desc: "查看志愿者信息" };
}

function openMyButler() {
  toast.value = { icon: "👤", title: "我的管家", desc: "查看管家信息" };
}

const showOrdersModal = ref(false);
const childOrderList = ref<
  { id: string; service: string; time: string; status: string; statusClass: string }[]
>([]);

function formatOrderTime(iso: string) {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

async function loadChildServiceOrders() {
  if (!elderId.value) {
    childOrderList.value = [];
    return;
  }
  try {
    const orders = await listServiceOrders(elderId.value);
    childOrderList.value = orders.map((o: ServiceOrderDto) => ({
      id: o.id.slice(0, 8).toUpperCase(),
      service: o.serviceTypeLabel || o.serviceType,
      time: formatOrderTime(o.appointmentTime),
      status: o.statusLabel,
      statusClass: serviceOrderStatusClass(o.status)
    }));
  } catch {
    childOrderList.value = [];
  }
}

async function openMyOrders() {
  showOrdersModal.value = true;
  await loadChildServiceOrders();
}

function openMemberAccount() {
  toast.value = { icon: "💳", title: "会员账户", desc: "查看会员账户信息" };
}

function toggleSetting(type: string) {
  toast.value = {
    icon: "⚙️",
    title: `${type === "alert" ? "告警推送" : "完善信息"}已切换`,
    desc: "设置已更新"
  };
}

async function loadPortalNews() {
  newsLoading.value = true;
  try {
    const raw = await fetchNews();
    newsList.value = Array.isArray(raw) ? raw : [];
  } catch {
    newsList.value = [];
  } finally {
    newsLoading.value = false;
  }
}

function openPortalNews(item: PortalNews) {
  portalModals.value?.openNews(item);
}

// Load on mount
onMounted(async () => {
  const role = localStorage.getItem("elder.role");
  if (role && role !== "CHILD") {
    toast.value = { icon: "!", title: "请使用子女账号登录", desc: "当前登录身份不是子女端" };
  }
  childUserId.value = childSessionUserId();
  loadLinkedEldersFromLS();
  await Promise.all([loadProfile(), loadChildProfile(), loadPortalNews()]);
  await loadAlarms();
  startAlarmPolling();
  loadVitalsFromLocal();
  startBannerTimer();
  const eid = elderId.value;
  if (eid) {
    unsubscribeChildVideo = subscribeFamilyVideoSession(eid, (session) => {
      if (session?.status === "ringing" && session.initiatorRole === "ELDER") {
        incomingElderVideoCall.value = session;
        return;
      }
      if (session?.initiatorRole === "ELDER" && session.status !== "ringing") {
        if (incomingElderVideoCall.value?.callId === session.callId) {
          incomingElderVideoCall.value = null;
        }
      }
      if (
        session?.status === "ended" &&
        session.initiatorRole === "ELDER" &&
        (videoState.status === "connected" || videoState.status === "dialing")
      ) {
        finishVideoCall("老人已挂断", false);
      }
    });
  }
});

onBeforeUnmount(() => {
  stopFamilySyncPoll();
  stopAlarmPolling();
  stopBannerTimer();
  clearSignalPoll();
  clearVideoTimers();
  clearMonitorTimers();
  stopLocalMedia();
  unsubscribeChildVideo?.();
  unsubscribeChildVideo = null;
  if (elderId.value && activeVideoCallId) {
    void patchFamilyVideoSession(elderId.value, activeVideoCallId, "ended");
  }
});
</script>

<style scoped>
.app-container.child-app {
  max-width: 480px;
  margin: 0 auto;
  min-height: 100vh;
  min-height: 100dvh;
  background: #f5f7fa;
  padding-bottom: 70px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  color: #333;
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  max-width: 480px;
  width: 100%;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-top: 1px solid #eee;
  display: flex;
  justify-content: space-around;
  padding: 8px 0 14px;
  z-index: 100;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.04);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  color: #999;
  font-size: 11px;
}

.nav-item.active {
  color: #2196f3;
}

.nav-icon {
  font-size: 22px;
  margin-bottom: 2px;
}

.home-page {
  padding: 14px 14px 0;
}

.health-page {
  padding: 0 14px 14px;
}

.notice-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff8e1;
  border: 1px solid #ffe082;
  border-radius: 12px;
  padding: 10px 14px;
  margin-bottom: 12px;
  cursor: pointer;
}

.notice-left,
.notice-right {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 700;
}

.notice-count {
  color: #e65100;
}

.notice-bar-urgent {
  background: #fef2f2;
  border-color: #fca5a5;
}

.sos-response-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  padding: 12px 14px;
  border-radius: 12px;
  background: linear-gradient(135deg, #dc2626, #b91c1c);
  color: #fff;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(220, 38, 38, 0.35);
  animation: child-sos-pulse 2s ease-in-out infinite;
}

@keyframes child-sos-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.01); }
}

.sos-banner-icon { font-size: 28px; flex-shrink: 0; }
.sos-banner-body { flex: 1; min-width: 0; }
.sos-banner-title { font-size: 15px; font-weight: 800; }
.sos-banner-desc { font-size: 12px; margin-top: 4px; opacity: 0.95; line-height: 1.4; }
.sos-banner-time { font-size: 11px; margin-top: 6px; opacity: 0.85; }
.sos-banner-action { font-size: 13px; font-weight: 700; flex-shrink: 0; }

.home-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.search-bar {
  flex: 1;
  background: white;
  border: 1px solid #e8ecf0;
  border-radius: 24px;
  padding: 8px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02);
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  background: transparent;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.ai-assistant {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #ede9fe;
  border: 1px solid #c4b5fd;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 0;
  transition: background 0.2s;
}

.ai-assistant:hover {
  background: #ddd6fe;
}

.ai-icon {
  font-size: 20px;
  line-height: 1;
}

.switch-chip {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid #bbdefb;
  background: #e3f2fd;
  font-size: 18px;
  cursor: pointer;
}

/* 智能助手浮窗 */
.ai-chat-window {
  position: fixed;
  bottom: 88px;
  right: 16px;
  width: min(340px, calc(100vw - 32px));
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  border: 1px solid #e8ecf0;
  z-index: 150;
  display: flex;
  flex-direction: column;
  max-height: min(420px, 58vh);
}

.ai-chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f2f5;
  background: linear-gradient(135deg, #ede9fe, #e3f2fd);
  border-radius: 16px 16px 0 0;
}

.ai-chat-title {
  font-weight: 600;
  font-size: 14px;
  color: #1a3347;
}

.ai-chat-close {
  background: none;
  border: none;
  font-size: 20px;
  color: #999;
  cursor: pointer;
  line-height: 1;
}

.ai-chat-body {
  flex: 1;
  padding: 12px 16px;
  overflow-y: auto;
  max-height: 240px;
  background: #fafbfc;
}

.ai-message {
  background: #ede9fe;
  padding: 10px 14px;
  border-radius: 12px 12px 12px 4px;
  font-size: 13px;
  color: #1a3347;
  margin-bottom: 10px;
  line-height: 1.5;
}

.chat-msg {
  margin-bottom: 8px;
}

.chat-msg.user {
  text-align: right;
}

.chat-msg.user .msg-bubble {
  background: #7c3aed;
  color: #fff;
  border-radius: 12px 12px 4px 12px;
  display: inline-block;
  padding: 8px 14px;
  font-size: 13px;
  text-align: left;
  max-width: 92%;
}

.chat-msg.ai .msg-bubble,
.msg-bubble.ai-thinking {
  background: #f0f2f5;
  color: #333;
  border-radius: 12px 12px 12px 4px;
  display: inline-block;
  padding: 8px 14px;
  font-size: 13px;
  max-width: 92%;
}

.ai-quick-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 8px 12px;
  border-top: 1px solid #f0f2f5;
  background: #fff;
}

.ai-quick-chip {
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  color: #475569;
  cursor: pointer;
  font-family: inherit;
}

.ai-quick-chip:hover:not(:disabled) {
  border-color: #c4b5fd;
  background: #f5f3ff;
}

.ai-quick-chip:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.ai-chat-footer {
  display: flex;
  gap: 8px;
  padding: 10px 16px 14px;
  border-top: 1px solid #f0f2f5;
}

.ai-input {
  flex: 1;
  border: 1px solid #e8ecf0;
  border-radius: 20px;
  padding: 8px 14px;
  font-size: 13px;
  outline: none;
  font-family: inherit;
}

.ai-input:focus {
  border-color: #a78bfa;
}

.ai-send-btn {
  background: #7c3aed;
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 13px;
  cursor: pointer;
  font-family: inherit;
  white-space: nowrap;
}

.ai-send-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.banner-carousel {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 14px;
  height: 160px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.banner-fade-enter-active,
.banner-fade-leave-active {
  transition: opacity 0.35s ease;
}

.banner-fade-enter-from,
.banner-fade-leave-to {
  opacity: 0;
}

.carousel-dots {
  position: absolute;
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 6px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
}

.dot.active {
  background: #2196f3;
  width: 18px;
  border-radius: 3px;
}

.care-cta-container {
  display: flex;
  justify-content: center;
  margin: 6px 0 16px;
}

.care-big-btn {
  width: 110px;
  height: 110px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(145deg, #1976d2, #42a5f5);
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 24px rgba(25, 118, 210, 0.35);
}

.care-icon {
  font-size: 34px;
}

.care-text {
  font-size: 14px;
  font-weight: 700;
}

.guardian-strip {
  margin-bottom: 14px;
  padding: 14px;
  background: linear-gradient(135deg, #1a9b6a, #2e7d32);
  color: white;
  border: none;
  border-radius: clamp(14px, 1.6vw, 20px);
  box-shadow: 0 2px 8px rgba(26, 123, 74, 0.2);
}

.guardian-strip-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.guardian-name {
  font-size: 16px;
  font-weight: 800;
}

.guardian-desc {
  margin-top: 4px;
  font-size: 12px;
  opacity: 0.92;
}

.guardian-tag {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
}

.child-news-section {
  margin-bottom: 14px;
  padding: 14px;
}

.child-news-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.child-news-title {
  font-size: 15px;
  font-weight: 800;
  color: #1a3347;
}

.child-news-count {
  font-size: 12px;
  color: #999;
}

.portal-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.portal-card {
  display: flex;
  gap: 12px;
  padding: 10px;
  border: 1px solid #f0f2f5;
  border-radius: 12px;
  cursor: pointer;
  background: #fafbfc;
}

.portal-card:active {
  background: #f0f4f8;
}

.portal-card-thumb {
  width: 64px;
  height: 64px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
  background: #e3f2fd;
  display: flex;
  align-items: center;
  justify-content: center;
}

.portal-card-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.portal-card-emoji {
  font-size: 28px;
}

.portal-card-body {
  flex: 1;
  min-width: 0;
}

.portal-card-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a3347;
  line-height: 1.4;
}

.portal-card-summary {
  margin: 4px 0 0;
  font-size: 12px;
  color: #666;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.portal-card-sub {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.portal-empty {
  text-align: center;
  color: #999;
  padding: 12px 0;
  font-size: 13px;
}

.guardian-empty {
  margin-bottom: 14px;
  text-align: center;
  padding: 20px;
}

.hot-section {
  margin-bottom: 14px;
}

.hot-title {
  font-size: 15px;
  font-weight: 800;
  margin-bottom: 10px;
  color: #333;
}

.hot-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.hot-item {
  background: white;
  border-radius: 14px;
  padding: 14px 8px;
  text-align: center;
  border: 1px solid #f0f2f5;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
}

.hot-icon {
  font-size: 26px;
  margin-bottom: 6px;
}

.hot-name {
  font-size: 11px;
  color: #666;
  font-weight: 700;
}

.grid-container {
  background: white;
  border-radius: 16px;
  padding: 18px 14px;
  margin-bottom: 14px;
  border: 1px solid #f0f2f5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.grid-row-4col {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px 6px;
}

.grid-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}

.grid-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  margin-bottom: 5px;
}

.blue-bg {
  background: #e3f2fd;
}
.green-bg {
  background: #e8f5e9;
}
.pink-bg {
  background: #fce4ec;
}
.purple-bg {
  background: #f3e5f5;
}
.orange-bg {
  background: #fff3e0;
}
.blue-light-bg {
  background: #e8eaf6;
}
.red-bg {
  background: #ffebee;
}

.grid-label {
  font-size: 11px;
  color: #666;
  text-align: center;
  line-height: 1.25;
}

.health-header {
  background: linear-gradient(180deg, #e3f2fd 0%, #f5f7fa 100%);
  border-radius: 0 0 24px 24px;
  padding: 20px 16px 16px;
  margin: 0 -14px 16px;
  text-align: center;
}

.health-title {
  font-size: 14px;
  color: #666;
  font-weight: 700;
  margin-bottom: 12px;
}

.health-avatar-ring {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: linear-gradient(135deg, #64b5f6, #1976d2);
  padding: 4px;
  margin: 0 auto 10px;
}

.health-avatar-inner {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
}

.health-name {
  font-size: 20px;
  font-weight: 800;
  color: #1a3347;
}

.health-sub {
  margin-top: 6px;
  font-size: 13px;
  color: #666;
}

.health-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 14px;
  flex-wrap: wrap;
}

.health-action-btn {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 999px;
  padding: 8px 14px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.health-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-bottom: 14px;
}

.health-card {
  background: white;
  border-radius: 14px;
  padding: 14px;
  border: 1px solid #f0f2f5;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
}

.health-card .card-label {
  font-size: 12px;
  color: #999;
}

.health-card .card-value {
  font-size: 22px;
  font-weight: 800;
  color: #1a3347;
  margin: 6px 0;
}

.health-card .card-range {
  font-size: 11px;
  color: #888;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  gap: 8px;
}

.section-title,
.section-title-inline {
  font-size: 15px;
  font-weight: 800;
  color: #1a3347;
}

.section-title-inline {
  margin-bottom: 12px;
}

.health-quick-actions {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 12px;
  margin: 14px 0;
  padding: 14px 8px;
  background: white;
  border-radius: 14px;
  border: 1px solid #f0f2f5;
}

.quick-icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 72px;
  cursor: pointer;
}

.quick-icon-circle {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  margin-bottom: 6px;
}

.quick-icon-circle.blue {
  background: #e3f2fd;
}
.quick-icon-circle.green {
  background: #e8f5e9;
}
.quick-icon-circle.purple-light {
  background: #f3e5f5;
}
.quick-icon-circle.orange {
  background: #fff3e0;
}

.quick-icon-label {
  font-size: 11px;
  color: #666;
  font-weight: 700;
  text-align: center;
}

.steps-section {
  background: white;
  border-radius: 14px;
  padding: 14px;
  margin-bottom: 14px;
  border: 1px solid #f0f2f5;
}

.steps-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.steps-title {
  font-weight: 800;
  color: #2e7d32;
}

.steps-content {
  display: flex;
  justify-content: space-around;
}

.step-label {
  font-size: 12px;
  color: #888;
  text-align: center;
}

.step-number {
  font-size: 18px;
  font-weight: 800;
  color: #1b5e20;
  text-align: center;
  margin-top: 4px;
}

.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.top-title {
  font-weight: 1000;
  font-size: 18px;
}
.switch-btn {
  border: 1px solid rgba(25, 118, 210, 0.25);
  background: white;
  color: #1565c0;
  font-weight: 900;
  padding: 10px 14px;
  border-radius: 14px;
  cursor: pointer;
}

.health-banner {
  border-radius: 20px;
  padding: 14px 14px 12px;
  background: linear-gradient(90deg, #1a9b6a 0%, #19a07d 100%);
  color: white;
}
.banner-name {
  font-size: 18px;
  font-weight: 1000;
}
.banner-desc {
  margin-top: 6px;
  font-weight: 800;
  opacity: 0.95;
  font-size: 13px;
}
.banner-tags {
  margin-top: 8px;
}
.tag {
  display: inline-block;
  background: rgba(255,255,255,0.2);
  border-radius: 999px;
  padding: 6px 10px;
  font-weight: 900;
  font-size: 12px;
}
.banner-right {
  flex-shrink: 0;
}
.mini-btn {
  background: rgba(255,255,255,0.18);
  border-radius: 14px;
  padding: 8px 12px;
  font-weight: 1000;
  font-size: 13px;
}
.child-loc-map {
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
}

.health-overview-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a3347;
  margin-bottom: 12px;
}

.health-overview-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.health-item {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 12px;
  text-align: center;
}

.health-item-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.health-item-label {
  font-size: 12px;
  color: #999;
}

.health-item-value {
  font-size: 18px;
  font-weight: 700;
  color: #1a3347;
  margin-top: 4px;
}

.unit {
  font-size: 13px;
  font-weight: 600;
  color: #666;
}

.health-record-list {
  display: grid;
  gap: 8px;
}

.health-record-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.record-time {
  font-size: 12px;
  color: #999;
}

.record-data {
  font-size: 13px;
  color: #333;
  font-weight: 500;
}

.mine-header {
  background: linear-gradient(180deg, #E3F2FD, #f5f7fa);
  padding: 30px 20px 20px;
  border-radius: 0 0 24px 24px;
  margin: -14px -14px 16px;
}

.mine-avatar-section {
  display: flex;
  align-items: center;
  gap: 14px;
}

.mine-avatar-circle {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #f0f4f8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #666;
}

.mine-name {
  font-size: 18px;
  font-weight: 500;
  color: #1a3347;
}

.mine-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #666;
}

.mine-page {
  padding: 0 14px;
}

.mine-page .mine-header {
  margin: 0 -14px 0;
}

.menu-section-label {
  padding: 4px 4px 8px;
  font-size: 12px;
  font-weight: 600;
  color: #888;
}

.menu-icon.red {
  color: #f44336;
}
.menu-icon.pink {
  color: #e91e63;
}
.menu-icon.green {
  color: #4caf50;
}

.mine-info-card {
  margin-bottom: 14px;
}

.mine-card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  gap: 8px;
}

.mine-card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a3347;
}

.mine-card-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.info-rows {
  display: grid;
  gap: 10px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  font-size: 13px;
}

.info-label {
  color: #999;
  flex-shrink: 0;
}

.info-value {
  color: #333;
  text-align: right;
  font-weight: 500;
  word-break: break-word;
}

.muted.small {
  padding: 8px 0;
  font-size: 13px;
}

.profile-form {
  display: grid;
  gap: 12px;
}

.form-group {
  display: grid;
  gap: 6px;
}

.form-group label {
  font-size: 13px;
  font-weight: 600;
  color: #666;
}

.wide-input2.full,
.wide-select2.full {
  width: 100%;
}

.profile-notes {
  resize: vertical;
  min-height: 56px;
}

.menu-list {
  background: white;
  border: 1px solid #f0f2f5;
  border-radius: 14px;
  margin-bottom: 14px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02);
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.menu-icon {
  font-size: 18px;
}

.menu-text {
  font-size: 14px;
  color: #333;
}

.menu-arrow {
  color: #ccc;
  font-size: 18px;
}

.menu-icon.blue {
  color: #2196f3;
}
.menu-icon.orange {
  color: #ff9800;
}
.menu-icon.blue-light {
  color: #3f51b5;
}
.menu-icon.red-light {
  color: #f44336;
}

.settings-section {
  background: white;
  border: 1px solid #f0f2f5;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02);
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #f5f5f5;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.setting-icon {
  font-size: 18px;
}

.setting-text {
  font-size: 14px;
  color: #333;
}

.setting-icon.red {
  color: #f44336;
}
.setting-icon.blue {
  color: #2196f3;
}

.toggle-switch {
  width: 44px;
  height: 24px;
  background: #ddd;
  border-radius: 12px;
  position: relative;
  cursor: pointer;
}

.toggle-switch.active {
  background: #4caf50;
}

.toggle-switch::after {
  content: "";
  width: 20px;
  height: 20px;
  background: white;
  border-radius: 50%;
  position: absolute;
  top: 2px;
  left: 2px;
  transition: transform 0.3s;
}

.toggle-switch.active::after {
  transform: translateX(20px);
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  max-width: 480px;
  width: 100%;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-top: 1px solid #eee;
  display: flex;
  justify-content: space-around;
  padding: 8px 0 14px;
  z-index: 100;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.04);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  color: #999;
  font-size: 11px;
  transition: color 0.3s;
}

.nav-item.active {
  color: #2196f3;
}

.nav-icon {
  font-size: 22px;
  margin-bottom: 2px;
}

.nav-label {
  font-size: 11px;
}

.order-item-simple {
  border: 1px solid #f0f2f5;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 10px;
}

.order-item-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.order-item-id {
  font-size: 12px;
  color: #999;
  font-weight: 600;
}

.order-item-status {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 8px;
}

.order-item-status.pending {
  background: #fff3e0;
  color: #e65100;
}
.order-item-status.processing {
  background: #e3f2fd;
  color: #1565c0;
}
.order-item-status.completed {
  background: #e8f5e9;
  color: #2e7d32;
}

.order-item-service {
  font-weight: 600;
  color: #1a3347;
}

.order-item-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
.banner-metrics {
  margin-top: 12px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.metric {
  border-radius: 18px;
  padding: clamp(10px, 1.2vw, 14px);
  background: rgba(255,255,255,0.12);
  border: 1px solid rgba(255,255,255,0.12);
}
.m-title {
  font-weight: 900;
  font-size: 13px;
  opacity: 0.95;
}
.m-value {
  margin-top: 6px;
  font-weight: 1000;
  font-size: 22px;
  line-height: 1.1;
}
.m-hint {
  margin-top: 6px;
  font-weight: 800;
  font-size: 12px;
  opacity: 0.9;
}

.card {
  background: white;
  border-radius: clamp(14px, 1.6vw, 20px);
  border: 1px solid rgba(0,0,0,0.06);
  box-shadow: 0 1px 8px rgba(0,0,0,0.03);
}

.trend-card {
  margin-top: 12px;
  padding: clamp(12px, 1.4vw, 16px);
}
.trend-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 12px;
}
.trend-title {
  font-weight: 1000;
  font-size: 15px;
}
.link-btn {
  border: 0;
  background: transparent;
  color: #1e88e5;
  font-weight: 1000;
  cursor: pointer;
}
.trend-chart {
  margin-top: 12px;
}

.alarm-center {
  margin-top: 12px;
  padding: clamp(12px, 1.4vw, 16px);
}
.alarm-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 12px;
}
.alarm-title {
  font-weight: 1000;
  font-size: 16px;
}
.alarm-tabs {
  margin-top: 10px;
  display: grid;
  grid-template-columns: 1fr 1fr 1.2fr;
  gap: 10px;
}
.tab {
  border: 0;
  background: transparent;
  padding: 10px 0;
  cursor: pointer;
  font-weight: 1000;
  color: rgba(0,0,0,0.45);
  border-bottom: 2px solid transparent;
}
.tab.active {
  color: #1e88e5;
  border-bottom-color: rgba(30,136,229,0.85);
}
.alarm-body {
  margin-top: 10px;
}
.empty {
  color: rgba(0,0,0,0.55);
  font-weight: 900;
  padding: 16px;
}
.alarm-item {
  border-radius: 16px;
  border: 1px solid rgba(211, 47, 47, 0.25);
  background: rgba(255, 152, 152, 0.08);
  padding: 12px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.alarm-item-sos {
  border-color: #dc2626;
  background: #fef2f2;
  box-shadow: 0 0 0 1px rgba(220, 38, 38, 0.15);
}

.alarm-sos-badge {
  display: inline-block;
  margin-left: 6px;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 10px;
  background: #dc2626;
  color: #fff;
  vertical-align: middle;
}

.alarm-status-line {
  margin-top: 6px;
  font-size: 12px;
  font-weight: 700;
  color: #b45309;
}

.confirm-btn-sos {
  background: #dc2626;
}
.alarm-time {
  font-weight: 900;
  color: rgba(0,0,0,0.45);
  font-size: 12px;
}
.alarm-subtitle {
  margin-top: 6px;
  font-weight: 1000;
  font-size: 14px;
  color: #c62828;
}
.alarm-desc {
  margin-top: 6px;
  font-weight: 800;
  color: rgba(0,0,0,0.65);
  font-size: 13px;
  line-height: 1.3;
}
.confirm-btn {
  border: 0;
  background: #1e88e5;
  color: white;
  font-weight: 1000;
  border-radius: 14px;
  padding: 10px 14px;
  cursor: pointer;
}
.rule-box {
  margin-top: 10px;
  border-radius: 16px;
  padding: 12px;
  border: 1px solid rgba(0,0,0,0.06);
  background: rgba(30,136,229,0.04);
}
.rule-title {
  font-weight: 1000;
  color: rgba(0,0,0,0.6);
  font-size: 13px;
}
.rule-text {
  margin-top: 6px;
  font-weight: 900;
  color: rgba(0,0,0,0.55);
  font-size: 13px;
}

.action-tiles {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 10px;
}
.tile {
  border: 1px solid rgba(25,118,210,0.15);
  background: white;
  border-radius: 18px;
  padding: clamp(12px, 1.3vw, 16px) 10px;
  font-weight: 1000;
  cursor: pointer;
  color: #1565c0;
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  z-index: 100;
}
.modal-card {
  width: 380px;
  max-width: 92%;
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  overflow: visible;
  border: none;
}

/* 与老人端一致的紧凑弹窗（首页远程守护服务入口） */
.modal-card.compact-modal,
.modal-card.service-modal,
.modal-card.orders-modal,
.modal-card.vitals-modal {
  width: 380px;
  max-width: 92%;
  padding: 24px;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  overflow: visible;
  border: none;
}

.modal-card.compact-modal .modal-top,
.modal-card.service-modal .modal-top,
.modal-card.orders-modal .modal-top,
.modal-card.vitals-modal .modal-top,
.modal-card.family-modal .modal-top,
.modal-card.profile-modal .modal-top {
  padding: 0;
  border-bottom: none;
  margin-bottom: 16px;
}

.modal-card.compact-modal .modal-title,
.modal-card.service-modal .modal-title,
.modal-card.orders-modal .modal-title,
.modal-card.vitals-modal .modal-title,
.modal-card.family-modal .modal-title,
.modal-card.profile-modal .modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a3347;
}

.modal-card.compact-modal .modal-close,
.modal-card.service-modal .modal-close,
.modal-card.orders-modal .modal-close,
.modal-card.vitals-modal .modal-close,
.modal-card.family-modal .modal-close,
.modal-card.profile-modal .modal-close {
  font-size: 24px;
}

.modal-card.compact-modal .modal-body {
  padding: 0;
}

.modal-card.compact-modal .modal-actions,
.modal-card.vitals-modal .modal-actions {
  display: flex;
  gap: 10px;
  margin-top: 0;
}

.modal-card.compact-modal .btn-cancel,
.modal-card.compact-modal .btn-confirm,
.modal-card.vitals-modal .btn-cancel,
.modal-card.vitals-modal .btn-confirm {
  flex: 1;
  padding: 12px;
  border-radius: 12px;
  border: none;
  font-weight: 500;
  cursor: pointer;
}

.modal-card.compact-modal .btn-cancel,
.modal-card.vitals-modal .btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.modal-card.compact-modal .btn-confirm,
.modal-card.vitals-modal .btn-confirm {
  background: #2196f3;
  color: #fff;
}

.modal-card.family-modal {
  width: 92%;
  max-width: 420px;
  max-height: 90vh;
  overflow: auto;
  padding: 24px;
}

.modal-card.family-modal .family-tabs2 {
  padding: 0;
  margin-bottom: 0;
  border-bottom: 1px solid #f0f2f5;
}

.modal-card.family-modal .modal-body {
  padding: 12px 0 0;
}

.modal-card.family-modal .chat-wrap2 {
  min-height: 240px;
}

.modal-card.profile-modal {
  max-width: 420px;
}

.modal-card.weekly-modal .modal-body {
  padding: 0;
}

.modal-card.weekly-modal svg {
  max-height: 180px;
}

.modal-card.vitals-modal .vitals-form {
  padding: 4px 0 12px;
}

.modal-card.vitals-modal .form-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.modal-card.vitals-modal .form-label {
  font-weight: 500;
  color: #666;
  font-size: 14px;
  min-width: 80px;
}

.modal-card.vitals-modal .stepper {
  display: grid;
  grid-template-columns: 44px 1fr 44px;
  gap: 8px;
  align-items: center;
  width: 180px;
}

.modal-card.vitals-modal .step-btn {
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  padding: 6px 0;
  background: white;
  font-weight: 600;
  cursor: pointer;
}

.modal-card.vitals-modal .step-value {
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  padding: 6px 0;
  text-align: center;
  font-weight: 500;
  background: #f8f9fa;
}

.modal-card.vitals-modal .bp-input {
  display: flex;
  gap: 8px;
  align-items: center;
  width: 180px;
}

.modal-card.vitals-modal .bp-input input {
  flex: 1;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 14px;
  width: 70px;
}

.modal-card.vitals-modal .wide-input {
  width: 180px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 14px;
}

.modal-card.service-modal .service-form {
  padding: 4px 0 12px;
}

.modal-card.service-modal .form-row,
.modal-card.orders-modal .form-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.modal-card.service-modal .form-label {
  font-weight: 500;
  color: #666;
  font-size: 14px;
  min-width: 80px;
}

.modal-card.service-modal .wide-input,
.modal-card.service-modal .wide-select {
  width: 180px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 14px;
  font-weight: 500;
}

.modal-card.service-modal .modal-actions,
.modal-card.orders-modal .modal-actions {
  display: flex;
  gap: 10px;
  margin-top: 0;
}

.modal-card.service-modal .btn-cancel,
.modal-card.service-modal .btn-confirm,
.modal-card.orders-modal .btn-confirm {
  flex: 1;
  padding: 12px;
  border-radius: 12px;
  border: none;
  font-weight: 500;
  cursor: pointer;
}

.modal-card.service-modal .btn-cancel,
.modal-card.orders-modal .btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.modal-card.service-modal .btn-confirm,
.modal-card.orders-modal .btn-confirm {
  background: #2196f3;
  color: #fff;
}

.modal-card.orders-modal .order-list {
  max-height: 300px;
  overflow-y: auto;
  padding: 8px 0;
}

.modal-card.orders-modal .order-item {
  display: block;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.modal-card.orders-modal .order-item:last-child {
  border-bottom: none;
}

.modal-card.orders-modal .order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.modal-card.orders-modal .order-id {
  font-size: 12px;
  color: #999;
}

.modal-card.orders-modal .order-status {
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 10px;
}

.modal-card.orders-modal .order-status.completed {
  background: #e8f5e9;
  color: #4caf50;
}

.modal-card.orders-modal .order-status.processing {
  background: #fff3e0;
  color: #ff9800;
}

.modal-card.orders-modal .order-status.pending {
  background: #e3f2fd;
  color: #2196f3;
}

.modal-card.orders-modal .order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-card.orders-modal .order-service {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.modal-card.orders-modal .order-time {
  font-size: 12px;
  color: #999;
}

.modal-card.orders-modal .no-data {
  text-align: center;
  color: #999;
  padding: 24px 0;
  font-size: 14px;
}

.modal-top {
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border-bottom: 1px solid rgba(0,0,0,0.06);
}
.modal-title {
  font-weight: 1000;
  font-size: 16px;
}
.modal-close {
  border: 0;
  background: transparent;
  font-size: 20px;
  cursor: pointer;
  color: rgba(0,0,0,0.45);
}
.modal-body {
  padding: 14px 16px 16px;
}
.modal-body.small-pad {
  padding: 12px 16px 16px;
}
.modal-cta {
  margin-top: 12px;
  display: flex;
  justify-content: center;
}

.primary-btn {
  border: 0;
  background: #1976d2;
  color: white;
  font-weight: 1000;
  border-radius: 14px;
  padding: 12px 16px;
  cursor: pointer;
}

/* Family tabs */
.family-tabs2 {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 10px;
  padding: 10px 16px 0;
}
.tab2 {
  border: 0;
  background: transparent;
  padding: 10px 0;
  font-weight: 1000;
  cursor: pointer;
  color: rgba(0,0,0,0.45);
  border-bottom: 2px solid transparent;
}
.tab2.active {
  color: #1e88e5;
  border-bottom-color: rgba(30,136,229,0.85);
}

.family-video-banner {
  padding: 10px 0;
  display: flex;
  justify-content: center;
}
.video-btn {
  border: 0;
  background: #43a047;
  color: white;
  font-weight: 1000;
  border-radius: 14px;
  padding: 12px 16px;
  cursor: pointer;
}
.video-btn.ghost {
  background: #f5f5f5;
  color: rgba(0,0,0,0.72);
  border: 1px solid rgba(0,0,0,0.1);
}
.video-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Chat */
.chat-wrap2 {
  padding-top: 10px;
  display: flex;
  flex-direction: column;
  min-height: 360px;
}
.chat-list2 {
  flex: 1;
  overflow: auto;
  padding-right: 6px;
}
.chat-item2 {
  margin-bottom: 12px;
}
.chat-item2.mine {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}
.bubble2 {
  max-width: 520px;
  border-radius: 14px;
  background: rgba(0,0,0,0.03);
  padding: 12px 14px;
  font-weight: 800;
  color: rgba(0,0,0,0.72);
}
.chat-item2.mine .bubble2 {
  background: rgba(30,136,229,0.08);
  color: rgba(0,0,0,0.72);
}
.meta2 {
  margin-top: 6px;
  font-weight: 800;
  font-size: 12px;
  color: rgba(0,0,0,0.45);
}
.chat-input2 {
  margin-top: 10px;
  display: grid;
  grid-template-columns: 1fr 86px;
  gap: 12px;
  align-items: center;
}
.chat-input-field {
  border: 1px solid rgba(0,0,0,0.15);
  border-radius: 14px;
  padding: 12px 12px;
  font-weight: 800;
}
.send-btn {
  border: 0;
  background: #1e88e5;
  color: white;
  font-weight: 1000;
  border-radius: 14px;
  padding: 12px 10px;
  cursor: pointer;
}

.album-placeholder2, .video-placeholder2 {
  padding: 30px 0;
  text-align: center;
  color: rgba(0,0,0,0.55);
  font-weight: 900;
}
.album-tools {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: space-between;
}
.album-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}
.album-photo-card {
  border: 1px solid rgba(0,0,0,0.08);
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  text-align: left;
}
.album-photo {
  width: 100%;
  height: 120px;
  object-fit: cover;
  display: block;
  cursor: pointer;
}
.album-meta {
  padding: 8px;
}
.album-name {
  font-weight: 900;
  color: rgba(0,0,0,0.72);
  font-size: 12px;
}
.album-time {
  margin-top: 4px;
  color: rgba(0,0,0,0.45);
  font-size: 11px;
  font-weight: 800;
}
.album-del {
  width: calc(100% - 16px);
  margin: 0 8px 8px;
  border: 1px solid rgba(198,40,40,0.2);
  background: rgba(198,40,40,0.06);
  color: #c62828;
  border-radius: 10px;
  padding: 6px 8px;
  font-weight: 900;
  cursor: pointer;
}
.video-stage {
  border: 1px solid rgba(0,0,0,0.08);
  border-radius: 14px;
  background: linear-gradient(120deg, #f4f7fb, #eef5ff);
  padding: 14px;
  min-height: 220px;
  position: relative;
  text-align: left;
}
.video-stage.connected {
  border-color: rgba(67,160,71,0.35);
}
.video-main {
  height: 170px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.video-title {
  font-size: 12px;
  color: rgba(0,0,0,0.5);
  font-weight: 800;
}
.video-avatar {
  margin-top: 8px;
  width: 64px;
  height: 64px;
  border-radius: 999px;
  background: #1976d2;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 1000;
  font-size: 28px;
}
.video-status {
  margin-top: 10px;
  color: rgba(0,0,0,0.65);
  font-weight: 900;
}
.video-local {
  position: absolute;
  right: 10px;
  bottom: 10px;
  width: 120px;
  height: 80px;
  border-radius: 10px;
  border: 1px solid rgba(255,255,255,0.4);
  background: rgba(0,0,0,0.35);
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
}
.video-local.off {
  background: rgba(0,0,0,0.6);
}

.visit-modal {
  width: 92%;
  max-width: 420px;
  padding: 0;
  overflow: hidden;
  border-radius: 18px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12);
}

.visit-modal .modal-top {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  margin-bottom: 0;
}

.visit-body {
  padding-top: 4px;
}

.visit-mode-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 12px;
}

.visit-mode-tab {
  border: 1px solid #e0e6ed;
  border-radius: 12px;
  padding: 10px 8px;
  background: #f8fafc;
  font-size: 13px;
  font-weight: 800;
  color: #666;
  cursor: pointer;
}

.visit-mode-tab.active {
  border-color: #1976d2;
  background: #e3f2fd;
  color: #1565c0;
}

.visit-hint {
  margin-bottom: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #e3f2fd;
  color: #1565c0;
  font-size: 13px;
  font-weight: 700;
  text-align: center;
}

.monitor-hint {
  background: #f3e5f5;
  color: #6a1b9a;
}

.monitor-device-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 220px;
  overflow-y: auto;
  margin-bottom: 12px;
}

.monitor-device-card {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e8ecf0;
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
  text-align: left;
}

.monitor-device-card.active {
  border-color: #7b1fa2;
  background: #f9f4fc;
  box-shadow: 0 0 0 1px rgba(123, 31, 162, 0.15);
}

.monitor-device-card.offline {
  opacity: 0.72;
}

.monitor-device-icon {
  font-size: 22px;
  flex-shrink: 0;
}

.monitor-device-info {
  flex: 1;
  min-width: 0;
}

.monitor-device-name {
  font-size: 14px;
  font-weight: 800;
  color: #1a3347;
}

.monitor-device-meta {
  margin-top: 2px;
  font-size: 11px;
  color: #888;
}

.monitor-device-status {
  font-size: 11px;
  font-weight: 800;
  padding: 4px 8px;
  border-radius: 999px;
  flex-shrink: 0;
}

.monitor-device-status.online {
  background: #e8f5e9;
  color: #2e7d32;
}

.monitor-device-status.offline {
  background: #f5f5f5;
  color: #999;
}

.monitor-live-stage {
  position: relative;
  border-radius: 14px;
  overflow: hidden;
  background: #111;
  min-height: 220px;
  margin-bottom: 12px;
}

.monitor-live-img {
  width: 100%;
  height: 220px;
  object-fit: cover;
  display: block;
}

.monitor-live-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(211, 47, 47, 0.9);
  color: #fff;
  font-size: 11px;
  font-weight: 800;
  padding: 4px 8px;
  border-radius: 6px;
}

.monitor-live-label {
  position: absolute;
  left: 10px;
  bottom: 10px;
  color: #fff;
  font-size: 13px;
  font-weight: 800;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.6);
}

.visit-stage {
  min-height: 240px;
}

.visit-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 12px;
}

.video-local-stream {
  position: absolute;
  right: 10px;
  bottom: 10px;
  width: 120px;
  height: 80px;
  border-radius: 10px;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.85);
  background: #111;
  z-index: 2;
}

.video-local-stream.off {
  opacity: 0.35;
  filter: grayscale(1);
}

.video-remote-stream {
  width: 100%;
  max-height: 240px;
  object-fit: cover;
  background: #111;
  border-radius: 8px;
}

.video-remote-stream.hidden {
  display: none;
}

.incoming-call-overlay {
  position: fixed;
  inset: 0;
  z-index: 2500;
  background: rgba(15, 23, 42, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.incoming-call-card {
  width: min(360px, 100%);
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
}

.incoming-call-icon { font-size: 40px; margin-bottom: 8px; }
.incoming-call-title { font-size: 18px; font-weight: 800; color: #1e293b; }
.incoming-call-sub { margin-top: 8px; font-size: 13px; color: #64748b; }
.incoming-call-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 20px;
}

.incoming-decline,
.incoming-accept {
  border: 0;
  border-radius: 10px;
  padding: 12px;
  font-weight: 700;
  cursor: pointer;
}

.incoming-decline { background: #f1f5f9; color: #475569; }
.incoming-accept { background: #2196f3; color: #fff; }

.video-remote-live {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #2e7d32;
  font-weight: 800;
  font-size: 14px;
}

.video-remote-pulse {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: rgba(46, 125, 50, 0.15);
  border: 2px solid #43a047;
  animation: visit-pulse 1.4s ease-in-out infinite;
}

@keyframes visit-pulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.08);
    opacity: 0.75;
  }
}

.video-controls {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}
.control-btn {
  border: 1px solid rgba(0,0,0,0.12);
  background: #fff;
  border-radius: 12px;
  padding: 10px 8px;
  font-weight: 900;
  cursor: pointer;
}
.control-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}
.album-preview {
  width: 860px;
}
.album-preview-img {
  width: 100%;
  max-height: 70vh;
  object-fit: contain;
  border-radius: 12px;
}

/* Vitals form */
.form-row2 {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}
.f-label {
  font-weight: 1000;
  font-size: 14px;
  color: rgba(0,0,0,0.65);
  min-width: 120px;
}
.stepper2 {
  display: grid;
  grid-template-columns: 50px 1fr 50px;
  gap: 10px;
  align-items: center;
  width: 240px;
}
.stepper-btn {
  border: 1px solid rgba(0,0,0,0.12);
  background: white;
  border-radius: 12px;
  padding: 10px 0;
  cursor: pointer;
  font-weight: 1000;
}
.stepper-value {
  border: 1px solid rgba(0,0,0,0.12);
  background: #f7f8fa;
  border-radius: 12px;
  padding: 10px 0;
  text-align: center;
  font-weight: 1000;
}
.bp-row {
  display: flex;
  gap: 10px;
  align-items: center;
  width: 240px;
}
.bp-input2 {
  width: 100%;
  border: 1px solid rgba(0,0,0,0.15);
  border-radius: 12px;
  padding: 10px 10px;
  font-weight: 800;
}
.slash {
  font-weight: 1000;
  color: rgba(0,0,0,0.55);
}
.wide-input2 {
  width: 100%;
  border: 1px solid rgba(0,0,0,0.15);
  border-radius: 12px;
  padding: 10px 12px;
  font-weight: 800;
}
.wide-select2 {
  width: 100%;
  border: 1px solid rgba(0,0,0,0.15);
  border-radius: 12px;
  padding: 10px 12px;
  font-weight: 800;
}
.modal-actions2 {
  margin-top: 10px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.btn-cancel2 {
  border: 1px solid rgba(0,0,0,0.12);
  background: white;
  border-radius: 14px;
  padding: 12px 10px;
  cursor: pointer;
  font-weight: 1000;
  color: rgba(0,0,0,0.65);
}
.btn-confirm2 {
  border: 0;
  background: #1e88e5;
  color: white;
  border-radius: 14px;
  padding: 12px 10px;
  cursor: pointer;
  font-weight: 1000;
}

/* Toast */
.toast {
  position: fixed;
  left: 50%;
  bottom: 20px;
  transform: translateX(-50%);
  width: 720px;
  max-width: calc(100% - 24px);
  background: white;
  border-radius: 18px;
  border: 1px solid rgba(0,0,0,0.06);
  box-shadow: 0 10px 30px rgba(0,0,0,0.15);
  z-index: 120;
  padding: 12px 14px;
  display: flex;
  gap: 12px;
  align-items: flex-start;
}
.toast-left {
  width: 26px;
  height: 26px;
  border-radius: 999px;
  background: rgba(76,175,80,0.15);
  color: #43a047;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 1000;
}
.toast-right {
  flex: 1;
}
.toast-title {
  font-weight: 1000;
  color: rgba(0,0,0,0.7);
}
.toast-desc {
  margin-top: 6px;
  font-weight: 800;
  color: rgba(0,0,0,0.55);
  font-size: 13px;
  line-height: 1.4;
}
.toast-close {
  border: 0;
  background: transparent;
  font-size: 18px;
  cursor: pointer;
  color: rgba(0,0,0,0.45);
}

.muted {
  color: rgba(0,0,0,0.55);
  font-weight: 800;
}

.elder-list {
  display: grid;
  gap: 10px;
}

.elder-choice {
  border: 1px solid rgba(0,0,0,0.08);
  background: white;
  border-radius: 16px;
  padding: 12px 14px;
  cursor: pointer;
  text-align: left;
}

.elder-choice.active {
  border-color: rgba(30,136,229,0.6);
  box-shadow: 0 0 0 2px rgba(30,136,229,0.12);
}

.elder-choice-name {
  font-weight: 1000;
  font-size: 16px;
}

.elder-choice-sub {
  margin-top: 6px;
  color: rgba(0,0,0,0.55);
  font-weight: 800;
  font-size: 12px;
}

@media (max-width: 1024px) {
  .child-page {
    padding: 0 10px 40px;
  }
}

@media (max-width: 768px) {
  .banner-metrics,
  .action-tiles,
  .alarm-tabs,
  .family-tabs2,
  .video-controls,
  .modal-actions2,
  .health-overview-grid {
    grid-template-columns: 1fr;
  }

  .modal-card,
  .modal-card.compact-modal,
  .modal-card.service-modal,
  .modal-card.orders-modal,
  .modal-card.vitals-modal,
  .modal-card.family-modal,
  .album-preview {
    width: 90%;
    max-width: 92%;
  }

  .visit-modal {
    width: 92%;
    max-width: 420px;
  }

  .modal-card.service-modal .form-row,
  .modal-card.vitals-modal .form-row {
    flex-direction: column;
    align-items: stretch;
  }

  .modal-card.service-modal .form-label,
  .modal-card.vitals-modal .form-label {
    min-width: auto;
  }

  .modal-card.service-modal .wide-input,
  .modal-card.service-modal .wide-select,
  .modal-card.vitals-modal .wide-input,
  .modal-card.vitals-modal .stepper,
  .modal-card.vitals-modal .bp-input {
    width: 100%;
  }

  .chat-input2 {
    grid-template-columns: 1fr;
  }

  .album-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .video-local {
    position: static;
    width: 100%;
    height: 60px;
    margin-top: 10px;
  }
}

@media (max-width: 480px) {
  .top-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }

  .form-row2 {
    flex-direction: column;
    align-items: stretch;
  }

  .f-label {
    min-width: 0;
  }

  .stepper2,
  .bp-row {
    width: 100%;
  }

  .album-grid {
    grid-template-columns: 1fr;
  }

  .toast {
    width: calc(100% - 16px);
    left: 8px;
    transform: none;
  }
}
</style>

