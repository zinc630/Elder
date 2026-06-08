<template>
  <div class="app-container">
    <!-- ===== 底部导航 ===== -->
    <div class="bottom-nav">
      <div class="nav-item" :class="{ active: currentTab === 'home' }" @click="currentTab = 'home'">
        <span class="nav-icon">🏠</span>
        <span class="nav-label">首页</span>
      </div>
      <div class="nav-item" :class="{ active: currentTab === 'health' }" @click="currentTab = 'health'">
        <span class="nav-icon">❤️</span>
        <span class="nav-label">健康</span>
      </div>
      <div class="nav-item" :class="{ active: currentTab === 'mine' }" @click="currentTab = 'mine'">
        <span class="nav-icon">👤</span>
        <span class="nav-label">我的</span>
      </div>
    </div>

    <!-- ============================================================ -->
    <!-- ===== 首页 TAB ===== -->
    <!-- ============================================================ -->
    <div v-show="currentTab === 'home'" class="home-page">
      <div class="notice-bar">
        <div class="notice-left">
          <span class="notice-icon">📢</span>
          <span class="notice-text">系统通知</span>
        </div>
        <div class="notice-right">
          <span class="notice-count">0条未读</span>
          <span class="notice-arrow">›</span>
        </div>
      </div>

      <div class="home-header">
        <div class="search-bar" @click="focusSearch">
          <span class="search-icon">🔍</span>
          <input
              v-model="searchKeyword"
              class="search-input"
              placeholder="搜索服务或功能"
              @focus="showSearchResults = true"
              @blur="hideSearchResults"
              @input="onSearchInput"
          />
        </div>
        <div class="header-right">
          <div class="ai-assistant" @click="toggleAiChat" title="智能助手">
            <span class="ai-icon">🤖</span>
          </div>
          <span class="avatar-mini">👤</span>
        </div>
      </div>

      <!-- 搜索结果浮层 -->
      <div v-if="showSearchResults && searchResults.length > 0" class="search-results-dropdown">
        <div v-for="result in searchResults" :key="result.id" class="search-result-item" @click="onSearchResultClick(result)">
          <span class="result-icon">{{ result.icon }}</span>
          <span class="result-text">{{ result.name }}</span>
        </div>
      </div>
      <div v-else-if="showSearchResults && searchKeyword && searchResults.length === 0" class="search-results-dropdown">
        <div class="no-result">未找到相关服务</div>
      </div>

      <!-- 轮播图 -->
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

      <!-- SOS 紧急呼叫按钮 -->
      <div class="sos-container">
        <button class="sos-big-btn" @click="onSOS">
          <div class="sos-icon">🆘</div>
          <div class="sos-text">紧急呼叫</div>
        </button>
      </div>

      <section class="hot-section elder-a11y-block" aria-label="常用服务">
        <h2 class="hot-title elder-a11y-title">常用服务</h2>
        <p class="elder-a11y-hint">点一下即可办理</p>
        <div class="hot-grid elder-a11y-hot-grid">
          <button type="button" class="hot-item elder-a11y-hot-item" @click="onServiceOrder('ACCOMPANY')">
            <span class="hot-icon elder-a11y-hot-icon" aria-hidden="true">🏥</span>
            <span class="hot-name elder-a11y-hot-name">健康体检</span>
          </button>
          <button type="button" class="hot-item elder-a11y-hot-item" @click="onServiceOrder('HOUSEKEEPING')">
            <span class="hot-icon elder-a11y-hot-icon" aria-hidden="true">🧹</span>
            <span class="hot-name elder-a11y-hot-name">家政保洁</span>
          </button>
          <button type="button" class="hot-item elder-a11y-hot-item" @click="onServiceOrder('NURSING')">
            <span class="hot-icon elder-a11y-hot-icon" aria-hidden="true">🍽️</span>
            <span class="hot-name elder-a11y-hot-name">助餐上门</span>
          </button>
        </div>
      </section>

      <!-- 功能宫格（适老化大按钮） -->
      <section class="grid-container elder-a11y-block" aria-label="全部功能">
        <h2 class="elder-a11y-section-title">全部功能</h2>
        <div class="grid-row-elder">
          <button type="button" class="grid-item elder-a11y-grid-item" @click="onHealthReport">
            <span class="grid-icon green-bg elder-a11y-grid-icon" aria-hidden="true">📊</span>
            <span class="grid-label elder-a11y-grid-label">健康上报</span>
          </button>
          <button type="button" class="grid-item elder-a11y-grid-item" @click="onFamily">
            <span class="grid-icon pink-bg elder-a11y-grid-icon" aria-hidden="true">💬</span>
            <span class="grid-label elder-a11y-grid-label">亲情互动</span>
          </button>
          <button type="button" class="grid-item elder-a11y-grid-item" @click="onPortalActivity">
            <span class="grid-icon purple-bg elder-a11y-grid-icon" aria-hidden="true">🎨</span>
            <span class="grid-label elder-a11y-grid-label">文娱活动</span>
          </button>
          <button type="button" class="grid-item elder-a11y-grid-item" @click="onPortalCourse">
            <span class="grid-icon orange-bg elder-a11y-grid-icon" aria-hidden="true">📚</span>
            <span class="grid-label elder-a11y-grid-label">学习中心</span>
          </button>
          <button type="button" class="grid-item elder-a11y-grid-item" @click="onPortalNews">
            <span class="grid-icon blue-light-bg elder-a11y-grid-icon" aria-hidden="true">📰</span>
            <span class="grid-label elder-a11y-grid-label">新闻资讯</span>
          </button>
          <button type="button" class="grid-item elder-a11y-grid-item" @click="onReportLocation">
            <span class="grid-icon red-bg elder-a11y-grid-icon" aria-hidden="true">📍</span>
            <span class="grid-label elder-a11y-grid-label">上报位置</span>
          </button>
        </div>
      </section>
    </div>

    <!-- ============================================================ -->
    <!-- ===== 健康 TAB ===== -->
    <!-- ============================================================ -->
    <div v-show="currentTab === 'health'" class="health-page">
      <div class="health-header">
        <div class="health-title">健康数据</div>
        <div class="health-avatar-wrapper">
          <div class="health-avatar-ring">
            <div class="health-avatar-inner">
              <span class="avatar-icon">👤</span>
            </div>
          </div>
        </div>
        <div class="health-name">{{ elderName }}</div>

        <div class="health-actions">
          <div class="health-action-btn" @click="onHealthReport">
            <span class="icon">📋</span> 健康档案
          </div>
          <div class="health-action-btn" @click="onHealthAlarms">
            <span class="icon">🔔</span> 健康告警
          </div>
          <div class="health-action-btn" @click="onFamily">
            <span class="icon">💊</span> 吃药提醒
          </div>
        </div>
      </div>

      <div class="health-cards">
        <div class="health-card">
          <div class="card-label">体重 KG</div>
          <div class="card-value">{{ vitalsForm.weight || '--' }}</div>
          <div class="card-range">标准 60-71kg</div>
        </div>
        <div class="health-card">
          <div class="card-label">身高 CM</div>
          <div class="card-value">{{ vitalsForm.height || '--' }}</div>
          <div class="card-range">身均 167.1cm</div>
        </div>
        <div class="health-card">
          <div class="card-label">BMI</div>
          <div class="card-value">{{ vitalsForm.bmi || '--' }}</div>
          <div class="card-range">正常 18.5-23.9</div>
        </div>
        <div class="health-card">
          <div class="card-label">体温 ℃</div>
          <div class="card-value">{{ vitalsForm.temperature || '--' }}</div>
          <div class="card-range">正常 36.3-37.2</div>
        </div>
      </div>

      <!-- 安全预警 -->
      <section class="card safety-card">
        <div class="section-head">
          <div class="section-title">安全预警 & 关怀提醒</div>
        </div>
        <div class="warning-row">
          <div class="warning-badge" aria-hidden="true">!</div>
          <div class="warning-text">
            <div class="warning-title">跌倒预警</div>
            <div class="warning-detail">近期心率/血压波动较大，可能存在跌倒风险。当前网络：{{ networkLabel }}。</div>
          </div>
        </div>
        <div class="safety-actions">
          <button class="danger-btn" @click="onCallFamily">一键联系家属</button>
          <button class="ok-btn" @click="onSafetyGuide">查看处置指南</button>
        </div>
      </section>

      <section class="health-quick-a11y elder-a11y-block" aria-label="健康快捷功能">
        <h2 class="elder-a11y-section-title">健康服务</h2>
        <p class="elder-a11y-hint">点一下即可办理</p>
        <div class="grid-row-elder health-quick-grid">
          <button type="button" class="grid-item elder-a11y-grid-item" @click="onDeviceBind">
            <span class="grid-icon purple-bg elder-a11y-grid-icon" aria-hidden="true">📋</span>
            <span class="grid-label elder-a11y-grid-label">一体机绑定</span>
          </button>
          <button type="button" class="grid-item elder-a11y-grid-item" @click="onFindHospital">
            <span class="grid-icon blue-bg elder-a11y-grid-icon" aria-hidden="true">🏥</span>
            <span class="grid-label elder-a11y-grid-label">找医院</span>
          </button>
          <button type="button" class="grid-item elder-a11y-grid-item" @click="onFindDoctor">
            <span class="grid-icon green-bg elder-a11y-grid-icon" aria-hidden="true">👨‍⚕️</span>
            <span class="grid-label elder-a11y-grid-label">找医生</span>
          </button>
          <button type="button" class="grid-item elder-a11y-grid-item" @click="onHealthAssessment">
            <span class="grid-icon blue-light-bg elder-a11y-grid-icon" aria-hidden="true">📊</span>
            <span class="grid-label elder-a11y-grid-label">健康评估</span>
          </button>
          <button type="button" class="grid-item elder-a11y-grid-item" @click="onAlertSettings">
            <span class="grid-icon red-bg elder-a11y-grid-icon" aria-hidden="true">❗</span>
            <span class="grid-label elder-a11y-grid-label">预警设置</span>
          </button>
        </div>
      </section>

      <div class="steps-section">
        <div class="steps-header">
          <span class="steps-title">计步</span>
          <span class="steps-plus">+</span>
        </div>
        <div class="steps-content">
          <div class="step-item">
            <div class="step-label">前一天</div>
            <div class="step-number">{{ stepsYesterday || '--' }}</div>
          </div>
          <div class="step-item">
            <div class="step-label">今日</div>
            <div class="step-number">{{ steps || '--' }}</div>
          </div>
          <div class="step-item">
            <div class="step-label">排行榜</div>
            <div class="step-number">{{ rank || '--' }}名</div>
          </div>
        </div>
      </div>
    </div>

    <!-- ============================================================ -->
    <!-- ===== 我的 TAB（完整功能） ===== -->
    <!-- ============================================================ -->
    <div v-show="currentTab === 'mine'" class="mine-page">
      <div class="mine-header">
        <div class="mine-avatar-section">
          <div class="mine-avatar-circle">👤</div>
          <div class="mine-name">{{ elderName }}</div>
        </div>
      </div>

      <div class="menu-list">
        <!-- 个人信息 -->
        <div class="menu-item" @click="openProfile">
          <div class="menu-left">
            <span class="menu-icon blue">👤</span>
            <span class="menu-text">个人信息</span>
          </div>
          <span class="menu-arrow">›</span>
        </div>

        <!-- 修改密码 -->
        <div class="menu-item" @click="openChangePassword">
          <div class="menu-left">
            <span class="menu-icon red">🔒</span>
            <span class="menu-text">修改密码</span>
          </div>
          <span class="menu-arrow">›</span>
        </div>

        <!-- 地址管理 -->
        <div class="menu-item" @click="openAddressManage">
          <div class="menu-left">
            <span class="menu-icon pink">📍</span>
            <span class="menu-text">地址管理</span>
          </div>
          <span class="menu-arrow">›</span>
        </div>

        <!-- 已关联用户 -->
        <div class="menu-item" @click="openRelatedUsers">
          <div class="menu-left">
            <span class="menu-icon green">👥</span>
            <span class="menu-text">已关联用户</span>
          </div>
          <span class="menu-arrow">›</span>
        </div>

        <!-- 帮扶志愿者 -->
        <div class="menu-item" @click="openVolunteer">
          <div class="menu-left">
            <span class="menu-icon orange">❤️</span>
            <span class="menu-text">帮扶志愿者</span>
          </div>
          <span class="menu-arrow">›</span>
        </div>

        <!-- 我的管家 -->
        <div class="menu-item" @click="openMyButler">
          <div class="menu-left">
            <span class="menu-icon blue-light">👤</span>
            <span class="menu-text">我的管家</span>
          </div>
          <span class="menu-arrow">›</span>
        </div>

        <!-- 我的订单 -->
        <div class="menu-item" @click="openMyOrders">
          <div class="menu-left">
            <span class="menu-icon red-light">📋</span>
            <span class="menu-text">我的订单</span>
          </div>
          <span class="menu-arrow">›</span>
        </div>

        <!-- 会员账户 -->
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
            <span class="setting-text">告警提示</span>
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

    <!-- ============================================================ -->
    <!-- ===== AI 助手 ===== -->
    <!-- ============================================================ -->
    <div v-if="showAiChat" class="ai-chat-window">
      <div class="ai-chat-header">
        <span class="ai-chat-title">🤖 智能助手</span>
        <button class="ai-chat-close" @click="toggleAiChat">×</button>
      </div>
      <div class="ai-chat-body">
        <div class="ai-message">您好！我是您的智能健康助手，有什么可以帮您的吗？</div>
        <div v-for="msg in aiMessages" :key="msg.id" :class="['chat-msg', msg.role]">
          <div class="msg-bubble">{{ msg.content }}</div>
        </div>
      </div>
      <div class="ai-chat-footer">
        <input v-model="aiInput" class="ai-input" placeholder="请输入您的问题..." @keyup.enter="sendAiMessage" />
        <button class="ai-send-btn" :disabled="aiBusy" @click="sendAiMessage">发送</button>
      </div>
    </div>

    <!-- ============================================================ -->
    <!-- ===== 功能弹窗 ===== -->
    <!-- ============================================================ -->

    <!-- SOS 确认 -->
    <div v-if="showSosConfirm" class="modal-overlay">
      <div class="modal-card">
        <div class="modal-top">
          <div class="modal-title">紧急求助</div>
          <button class="modal-close" @click="closeSosConfirm">×</button>
        </div>
        <div class="modal-body">
          <div class="modal-warning">
            <div class="modal-warning-badge">!</div>
            <div class="modal-warning-text">确认一键求助吗？系统将立即通知家属及服务机构！</div>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="closeSosConfirm">取消</button>
          <button class="btn-confirm" @click="confirmSOS">确认呼叫</button>
        </div>
      </div>
    </div>

    <!-- 个人信息 -->
    <div v-if="showProfileModal" class="modal-overlay">
      <div class="modal-card profile-modal">
        <div class="modal-top">
          <div class="modal-title">个人信息</div>
          <button class="modal-close" @click="showProfileModal = false">×</button>
        </div>
        <div v-if="profileLoading" class="no-data">加载中...</div>
        <div v-else class="profile-form">
          <div class="form-group">
            <label>账号 ID</label>
            <input :value="profileForm.elderId" class="form-input" readonly />
          </div>
          <div class="form-group">
            <label>姓名</label>
            <input v-model="profileForm.name" class="form-input" placeholder="请输入姓名" />
          </div>
          <div class="form-group">
            <label>年龄</label>
            <input v-model.number="profileForm.age" type="number" min="1" max="120" class="form-input" placeholder="请输入年龄" />
          </div>
          <div class="form-group">
            <label>性别</label>
            <select v-model="profileForm.gender" class="form-input">
              <option value="男">男</option>
              <option value="女">女</option>
              <option value="其他">其他</option>
            </select>
          </div>
          <div class="form-group">
            <label>健康备注</label>
            <textarea v-model="profileForm.keyHealthNotes" class="form-input profile-notes" rows="3" placeholder="如高血压、糖尿病等" />
          </div>
          <p class="profile-hint">服务住址请在「地址管理」中编辑。</p>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showProfileModal = false">取消</button>
          <button class="btn-confirm" :disabled="profileSaving" @click="saveProfile">
            {{ profileSaving ? "保存中..." : "保存" }}
          </button>
        </div>
      </div>
    </div>

    <!-- 修改密码 -->
    <div v-if="showChangePasswordModal" class="modal-overlay">
      <div class="modal-card password-modal">
        <div class="modal-top">
          <div class="modal-title">修改密码</div>
          <button class="modal-close" @click="showChangePasswordModal = false">×</button>
        </div>
        <div class="password-form">
          <div class="form-group">
            <label>原密码</label>
            <input v-model="passwordForm.oldPassword" type="password" class="form-input" placeholder="请输入原密码" />
          </div>
          <div class="form-group">
            <label>新密码</label>
            <input v-model="passwordForm.newPassword" type="password" class="form-input" placeholder="请输入新密码" />
          </div>
          <div class="form-group">
            <label>确认密码</label>
            <input v-model="passwordForm.confirmPassword" type="password" class="form-input" placeholder="请再次输入新密码" />
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showChangePasswordModal = false">取消</button>
          <button class="btn-confirm" @click="confirmChangePassword">确认修改</button>
        </div>
      </div>
    </div>

    <!-- 地址管理（sys_user.address） -->
    <div v-if="showAddressModal" class="modal-overlay">
      <div class="modal-card address-modal">
        <div class="modal-top">
          <div class="modal-title">地址管理</div>
          <button class="modal-close" @click="showAddressModal = false">×</button>
        </div>
        <div v-if="addressLoading" class="no-data">加载中...</div>
        <div v-else class="address-form">
          <p class="profile-hint">住址保存在账号档案中，用于上门服务与定位参考。</p>
          <div class="form-group">
            <label>详细地址</label>
            <textarea
              v-model="profileForm.address"
              class="form-input profile-notes"
              rows="4"
              placeholder="省市区、街道、小区、门牌号"
            />
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showAddressModal = false">取消</button>
          <button class="btn-confirm" :disabled="addressSaving || addressLoading" @click="saveAddress">
            {{ addressSaving ? "保存中..." : "保存地址" }}
          </button>
        </div>
      </div>
    </div>

    <!-- 已关联用户 -->
    <div v-if="showRelatedUsersModal" class="modal-overlay">
      <div class="modal-card related-modal">
        <div class="modal-top">
          <div class="modal-title">已关联用户</div>
          <button class="modal-close" @click="showRelatedUsersModal = false">×</button>
        </div>
        <div class="related-list">
          <div v-for="user in relatedUsers" :key="user.id" class="related-item">
            <div class="related-avatar">{{ user.name.charAt(0) }}</div>
            <div class="related-info">
              <div class="related-name">{{ user.name }}</div>
              <div class="related-relation">{{ user.relation }}</div>
              <div class="related-phone">{{ user.phone }}</div>
            </div>
            <div class="related-actions">
              <button class="related-delete" @click="deleteRelatedUser(user.id)">移除</button>
            </div>
          </div>
          <div v-if="relatedUsers.length === 0" class="no-data">暂无关联用户</div>
        </div>
        <div class="modal-actions">
          <button class="btn-confirm" @click="addRelatedUser">添加关联用户</button>
        </div>
      </div>
    </div>

    <!-- 帮扶志愿者 -->
    <div v-if="showVolunteerModal" class="modal-overlay">
      <div class="modal-card volunteer-modal">
        <div class="modal-top">
          <div class="modal-title">帮扶志愿者</div>
          <button class="modal-close" @click="showVolunteerModal = false">×</button>
        </div>
        <div class="volunteer-content">
          <div v-if="volunteerInfo" class="volunteer-card">
            <div class="volunteer-avatar">{{ volunteerInfo.name.charAt(0) }}</div>
            <div class="volunteer-detail">
              <div class="volunteer-name">{{ volunteerInfo.name }}</div>
              <div class="volunteer-org">{{ volunteerInfo.organization }}</div>
              <div class="volunteer-phone">{{ volunteerInfo.phone }}</div>
              <div class="volunteer-desc">{{ volunteerInfo.description }}</div>
            </div>
          </div>
          <div v-else class="no-data">暂无帮扶志愿者</div>
        </div>
        <div class="modal-actions">
          <button class="btn-confirm" @click="showVolunteerModal = false">确定</button>
        </div>
      </div>
    </div>

    <!-- 我的管家 -->
    <div v-if="showButlerModal" class="modal-overlay">
      <div class="modal-card butler-modal">
        <div class="modal-top">
          <div class="modal-title">我的管家</div>
          <button class="modal-close" @click="showButlerModal = false">×</button>
        </div>
        <div class="butler-content">
          <div v-if="butlerInfo" class="butler-card">
            <div class="butler-avatar">{{ butlerInfo.name.charAt(0) }}</div>
            <div class="butler-detail">
              <div class="butler-name">{{ butlerInfo.name }}</div>
              <div class="butler-title">{{ butlerInfo.title }}</div>
              <div class="butler-phone">{{ butlerInfo.phone }}</div>
              <div class="butler-desc">{{ butlerInfo.description }}</div>
            </div>
            <div class="butler-actions">
              <button class="btn-confirm" @click="callButler">呼叫管家</button>
            </div>
          </div>
          <div v-else class="no-data">暂无管家信息</div>
        </div>
      </div>
    </div>

    <!-- 我的订单 -->
    <div v-if="showOrdersModal" class="modal-overlay">
      <div class="modal-card orders-modal">
        <div class="modal-top">
          <div class="modal-title">我的订单</div>
          <button class="modal-close" @click="showOrdersModal = false">×</button>
        </div>
        <div class="order-list">
          <div v-for="order in orderList" :key="order.id" class="order-item">
            <div class="order-header">
              <span class="order-id">订单号: {{ order.id }}</span>
              <span class="order-status" :class="order.statusClass">{{ order.status }}</span>
            </div>
            <div class="order-body">
              <div class="order-service">{{ order.service }}</div>
              <div class="order-time">{{ order.time }}</div>
            </div>
          </div>
          <div v-if="orderList.length === 0" class="no-data">暂无订单</div>
        </div>
        <div class="modal-actions">
          <button class="btn-confirm" @click="showOrdersModal = false">关闭</button>
        </div>
      </div>
    </div>

    <!-- 会员账户 -->
    <div v-if="showMemberModal" class="modal-overlay">
      <div class="modal-card member-modal">
        <div class="modal-top">
          <div class="modal-title">会员账户</div>
          <button class="modal-close" @click="showMemberModal = false">×</button>
        </div>
        <div class="member-content">
          <div class="member-level">
            <div class="level-badge">{{ memberInfo.level }}</div>
            <div class="level-name">{{ memberInfo.levelName }}</div>
          </div>
          <div class="member-stats">
            <div class="stat-item">
              <div class="stat-label">余额</div>
              <div class="stat-value">¥ {{ memberInfo.balance }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">积分</div>
              <div class="stat-value">{{ memberInfo.points }} 分</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">优惠券</div>
              <div class="stat-value">{{ memberInfo.coupons }} 张</div>
            </div>
          </div>
          <div class="member-actions">
            <button class="btn-confirm" @click="showMemberModal = false">关闭</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 一体机绑定 -->
    <div v-if="showDeviceBindModal" class="modal-overlay">
      <div class="modal-card device-bind-modal">
        <div class="modal-top">
          <div class="modal-title">一体机绑定</div>
          <button class="modal-close" @click="showDeviceBindModal = false">×</button>
        </div>
        <div class="device-bind-body">
          <p class="modal-hint">扫描机身二维码或输入设备编号，完成健康一体机绑定后即可自动同步体征数据。</p>
          <div class="form-row">
            <div class="form-label">设备编号</div>
            <input v-model="deviceSn" class="wide-input" placeholder="例如 YT-2026-001" />
          </div>
          <div v-if="deviceBindStatus" class="bind-status" :class="deviceBindStatus.type">
            {{ deviceBindStatus.text }}
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showDeviceBindModal = false">取消</button>
          <button class="btn-confirm" @click="confirmDeviceBind">确认绑定</button>
        </div>
      </div>
    </div>

    <!-- 找医院 -->
    <div v-if="showHospitalModal" class="modal-overlay">
      <div class="modal-card hospital-modal">
        <div class="modal-top">
          <div class="modal-title">附近医院</div>
          <button class="modal-close" @click="showHospitalModal = false">×</button>
        </div>
        <p v-if="medicalRegionHint" class="modal-hint">{{ medicalRegionHint }}</p>
        <div v-if="hospitalLoading" class="no-data">正在根据您的住址匹配附近医院...</div>
        <div v-else-if="hospitalList.length === 0" class="no-data">暂无推荐，请先在「地址管理」中填写住址</div>
        <div v-else class="resource-list">
          <div v-for="h in hospitalList" :key="h.id" class="resource-item">
            <div class="resource-name">{{ h.name }}</div>
            <div class="resource-meta">{{ h.address }} · {{ h.distance }}</div>
            <div class="resource-phone">{{ h.phone }}</div>
            <button class="resource-call-btn" @click="callPhone(h.phone)">拨打电话</button>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-confirm" @click="showHospitalModal = false">关闭</button>
        </div>
      </div>
    </div>

    <!-- 找医生 -->
    <div v-if="showDoctorModal" class="modal-overlay">
      <div class="modal-card doctor-modal">
        <div class="modal-top">
          <div class="modal-title">附近门诊 / 医生</div>
          <button class="modal-close" @click="showDoctorModal = false">×</button>
        </div>
        <p v-if="medicalRegionHint" class="modal-hint">{{ medicalRegionHint }}</p>
        <div v-if="doctorLoading" class="no-data">正在根据您的住址匹配附近门诊...</div>
        <div v-else-if="doctorList.length === 0" class="no-data">暂无推荐，请先在「地址管理」中填写住址</div>
        <div v-else class="resource-list">
          <div v-for="d in doctorList" :key="d.id" class="resource-item">
            <div class="resource-name">{{ d.name }} · {{ d.title }}</div>
            <div class="resource-meta">{{ d.hospital }} · {{ d.specialty }}</div>
            <div class="resource-phone">{{ d.phone }}</div>
            <button class="resource-call-btn" @click="callPhone(d.phone)">预约咨询</button>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-confirm" @click="showDoctorModal = false">关闭</button>
        </div>
      </div>
    </div>

    <!-- 健康评估 -->
    <div v-if="showAssessmentModal" class="modal-overlay">
      <div class="modal-card assessment-modal">
        <div class="modal-top">
          <div class="modal-title">{{ assessmentModalTitle }}</div>
          <button class="modal-close" @click="showAssessmentModal = false">×</button>
        </div>
        <div class="assessment-body">
          <div class="assessment-score">
            <div class="score-value">{{ healthAssessment.score }}</div>
            <div class="score-label">综合健康分</div>
            <div class="score-level" :class="healthAssessment.levelClass">{{ healthAssessment.level }}</div>
          </div>
          <div class="assessment-items">
            <div v-for="item in healthAssessment.items" :key="item.name" class="assessment-row">
              <span class="assessment-name">{{ item.name }}</span>
              <span class="assessment-status" :class="item.statusClass">{{ item.status }}</span>
            </div>
          </div>
          <p class="assessment-advice">{{ healthAssessment.advice }}</p>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showAssessmentModal = false">关闭</button>
          <button class="btn-confirm" @click="openVitalsFromAssessment">录入最新数据</button>
        </div>
      </div>
    </div>

    <!-- 预警设置 -->
    <div v-if="showAlertSettingsModal" class="modal-overlay">
      <div class="modal-card alert-settings-modal">
        <div class="modal-top">
          <div class="modal-title">预警设置</div>
          <button class="modal-close" @click="showAlertSettingsModal = false">×</button>
        </div>
        <p class="modal-hint">开启后，系统将按规则监测体征与行为，异常时推送至您与家属端。</p>
        <div class="alert-settings-list">
          <div v-for="rule in alertSettingRules" :key="rule.key" class="setting-item">
            <div class="setting-left">
              <span class="setting-icon" :class="rule.iconClass">{{ rule.icon }}</span>
              <div class="setting-text-wrap">
                <span class="setting-text">{{ rule.label }}</span>
                <span class="setting-desc">{{ rule.desc }}</span>
              </div>
            </div>
            <div
              class="toggle-switch"
              :class="{ active: alertSettings[rule.key] }"
              @click="toggleAlertRule(rule.key)"
            />
          </div>
        </div>
        <div class="modal-actions alert-settings-actions">
          <button class="btn-cancel" type="button" @click="openAlarmCenter">查看告警记录</button>
          <button class="btn-confirm" type="button" @click="saveAlertSettings">保存设置</button>
        </div>
      </div>
    </div>

    <!-- 健康上报弹窗 -->
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
              <button class="step-btn" @click="vitalsForm.hr = Math.max(0, (vitalsForm.hr || 0) - 1)">－</button>
              <div class="step-value">{{ vitalsForm.hr || 0 }}</div>
              <button class="step-btn" @click="vitalsForm.hr = (vitalsForm.hr || 0) + 1">＋</button>
            </div>
          </div>
          <div class="form-row">
            <div class="form-label">血压</div>
            <div class="bp-input">
              <input v-model.number="vitalsForm.sbp" type="number" placeholder="收缩压" />
              <span>/</span>
              <input v-model.number="vitalsForm.dbp" type="number" placeholder="舒张压" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-label">血氧 (%)</div>
            <div class="stepper">
              <button class="step-btn" @click="vitalsForm.spo2 = Math.max(0, (vitalsForm.spo2 || 0) - 1)">－</button>
              <div class="step-value">{{ vitalsForm.spo2 || 0 }}</div>
              <button class="step-btn" @click="vitalsForm.spo2 = (vitalsForm.spo2 || 0) + 1">＋</button>
            </div>
          </div>
          <div class="form-row">
            <div class="form-label">体重 (kg)</div>
            <input v-model.number="vitalsForm.weight" type="number" class="wide-input" />
          </div>
          <div class="form-row">
            <div class="form-label">身高 (cm)</div>
            <input v-model.number="vitalsForm.height" type="number" class="wide-input" @input="updateBmi" />
          </div>
          <div class="form-row">
            <div class="form-label">BMI</div>
            <div class="step-value bmi-readonly">{{ vitalsForm.bmi || "--" }}</div>
          </div>
          <div class="form-row">
            <div class="form-label">体温 (℃)</div>
            <input v-model.number="vitalsForm.temperature" type="number" step="0.1" class="wide-input" />
          </div>
          <div class="form-row">
            <div class="form-label">血糖 (mmol/L)</div>
            <input v-model.number="vitalsForm.glucose" type="number" class="wide-input" />
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showVitalsModal = false">取消</button>
          <button class="btn-confirm" @click="submitVitals">提交并同步家属</button>
        </div>
      </div>
    </div>

    <!-- 服务预约弹窗 -->
    <div v-if="showServiceOrderModal" class="modal-overlay">
      <div class="modal-card service-modal">
        <div class="modal-top">
          <div class="modal-title">预约养老服务</div>
          <button class="modal-close" @click="showServiceOrderModal = false">×</button>
        </div>
        <div class="service-form">
          <div class="form-row">
            <div class="form-label">服务类型</div>
            <select v-model="service.serviceType" class="wide-select">
              <option value="NURSING">助餐（送餐上门）</option>
              <option value="HOUSEKEEPING">家政保洁</option>
              <option value="ACCOMPANY">陪诊陪护</option>
            </select>
          </div>
          <div class="form-row">
            <div class="form-label">预约时间</div>
            <input v-model="service.appointmentTime" type="datetime-local" class="wide-input" />
          </div>
          <div class="form-row">
            <div class="form-label">备注</div>
            <input v-model="service.notes" class="wide-input" placeholder="特殊需求..." />
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showServiceOrderModal = false">取消</button>
          <button class="btn-confirm" @click="confirmServiceOrder">确认预约</button>
        </div>
      </div>
    </div>

    <!-- 亲情互动 -->
    <div v-if="showFamilyModal" class="modal-overlay" role="dialog" aria-modal="true">
      <div class="modal-card family-modal">
        <div class="modal-top">
          <div class="modal-title">亲情互动 · 与子女连线</div>
          <button class="modal-close" @click="closeFamilyModal" aria-label="关闭">×</button>
        </div>
        <div class="family-tabs">
          <button class="tab" :class="{ active: familyTab === 'chat' }" @click="familyTab = 'chat'">聊天记录</button>
          <button class="tab" :class="{ active: familyTab === 'album' }" @click="familyTab = 'album'">家庭相册</button>
          <button class="tab" :class="{ active: familyTab === 'video' }" @click="familyTab = 'video'">视频通话</button>
        </div>
        <div class="family-body">
          <div v-if="familyTab === 'chat'" class="chat-wrap">
            <div class="chat-list">
              <div v-for="m in chatMessages" :key="m.id" class="chat-item" :class="{ mine: m.mine }">
                <div class="chat-bubble">{{ m.text }}</div>
                <div class="chat-meta">{{ m.meta }}</div>
              </div>
            </div>
            <div class="chat-input-row">
              <input v-model="familyChatInput" class="chat-input" placeholder="输入文字/语音..." />
              <button class="chat-send" @click="sendFamilyMessage">发送</button>
            </div>
          </div>
          <div v-else-if="familyTab === 'album'" class="album-wrap">
            <div class="album-tools">
              <button class="video-action" :disabled="albumUploading" @click="triggerAlbumUpload">
                {{ albumUploading ? "上传中..." : "上传照片" }}
              </button>
              <input ref="albumInputRef" type="file" accept="image/*" style="display: none" @change="onAlbumFileChange" />
              <div class="album-desc">可上传家庭合照，方便老人端回看</div>
            </div>
            <div v-if="albumItems.length === 0" class="album-empty">暂无照片，请先上传</div>
            <div v-else class="album-grid">
              <div v-for="item in albumItems" :key="item.id" class="album-item">
                <img class="album-photo" :src="item.url" :alt="item.name" @click="albumPreview = item" />
                <div class="album-item-name">{{ item.name }}</div>
                <div class="album-item-time">{{ item.createdAt }}</div>
                <button class="album-del-btn" @click="removeAlbumItem(item.id)">删除</button>
              </div>
            </div>
          </div>
          <div v-else class="video-wrap">
            <div class="video-call-actions">
              <button class="video-action" :disabled="videoState.status === 'dialing' || videoState.status === 'connected'" @click="startVideoCall">
                {{ videoState.status === "dialing" ? "呼叫中..." : videoState.status === "connected" ? "通话进行中" : "发起视频通话" }}
              </button>
              <button class="video-action ghost" :disabled="videoState.status === 'idle'" @click="endVideoCall">挂断</button>
            </div>
            <div class="video-stage visit-stage family-video-stage" :class="{ connected: videoState.status === 'connected' }">
              <div class="video-main">
                <div class="video-title">子女端画面</div>
                <video
                  ref="remoteVideoRef"
                  class="video-remote-stream"
                  :class="{ hidden: videoState.status !== 'connected' }"
                  autoplay
                  playsinline
                />
                <div v-if="videoState.status !== 'connected'" class="video-avatar">{{ callPeerInitial }}</div>
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
            <div class="video-controls">
              <button class="video-ctl" :disabled="videoState.status !== 'connected'" @click="toggleVideoMute">{{ videoState.muted ? "取消静音" : "静音" }}</button>
              <button class="video-ctl" :disabled="videoState.status !== 'connected'" @click="toggleVideoCamera">{{ videoState.cameraOn ? "关闭摄像头" : "开启摄像头" }}</button>
              <button class="video-ctl" :disabled="videoState.status !== 'connected'" @click="videoState.speakerOn = !videoState.speakerOn">{{ videoState.speakerOn ? "免提开" : "免提关" }}</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="albumPreview" class="modal-overlay" role="dialog" aria-modal="true">
      <div class="modal-card family-modal">
        <div class="modal-top">
          <div class="modal-title">{{ albumPreview.name }}</div>
          <button class="modal-close" @click="albumPreview = null" aria-label="关闭">×</button>
        </div>
        <div class="family-body">
          <img class="album-preview-img" :src="albumPreview.url" :alt="albumPreview.name" />
        </div>
      </div>
    </div>

    <!-- 子女端远程探视来电 -->
    <div v-if="incomingVideoCall" class="incoming-call-overlay" role="dialog" aria-modal="true">
      <div class="incoming-call-card">
        <div class="incoming-call-icon">📹</div>
        <div class="incoming-call-title">{{ familyCallDisplayName(incomingVideoCall) }} 发起视频通话</div>
        <div class="incoming-call-sub">是否接听视频通话？</div>
        <div class="incoming-call-actions">
          <button type="button" class="incoming-btn decline" @click="declineIncomingVideoCall">拒绝</button>
          <button type="button" class="incoming-btn accept" @click="acceptIncomingVideoCall">接听</button>
        </div>
      </div>
    </div>

    <!-- 文娱活动 / 学习中心 / 新闻资讯 列表弹窗 -->
    <div
      v-if="showPortalListModal"
      class="modal-overlay portal-list-overlay"
      role="dialog"
      aria-modal="true"
      @click.self="closePortalListModal"
    >
      <div class="modal-card portal-list-modal" @click.stop>
        <div class="modal-top">
          <div class="modal-title">{{ portalSectionTitle }}</div>
          <button type="button" class="modal-close" aria-label="关闭" @click="closePortalListModal">×</button>
        </div>
        <p class="portal-list-modal-hint">点选条目查看详情</p>
        <div class="portal-list portal-list-modal-body">
          <button
            v-for="item in portalListItems"
            :key="item.id"
            type="button"
            class="portal-card elder-a11y-portal-card"
            @click="onPortalListItemClick(item)"
          >
            <div class="portal-card-thumb">
              <img v-if="item.imageUrl" :src="resolveMediaUrl(item.imageUrl)" :alt="item.title" />
              <span v-else class="portal-card-emoji">{{ item.icon }}</span>
            </div>
            <div class="portal-card-body">
              <div class="portal-card-title elder-a11y-portal-title">{{ item.title }}</div>
              <div v-if="item.subtitle" class="portal-card-sub elder-a11y-portal-sub">{{ item.subtitle }}</div>
            </div>
          </button>
          <div v-if="portalListItems.length === 0" class="portal-empty elder-a11y-portal-empty">暂无内容</div>
        </div>
        <div class="modal-actions portal-list-modal-foot">
          <button type="button" class="btn-confirm" @click="closePortalListModal">关闭</button>
        </div>
      </div>
    </div>

    <PortalHomeModals ref="portalModals" @refresh="loadPortalContent" @toast="(m: string) => showToastMessage('提示', m)" />

    <!-- Toast 提示 -->
    <div v-if="showToast" class="toast">
      <div class="toast-icon">{{ toastIcon }}</div>
      <div class="toast-content">
        <div class="toast-title">{{ toastTitle }}</div>
        <div class="toast-desc">{{ toastDesc }}</div>
      </div>
      <button class="toast-close" @click="showToast = false">×</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from "vue";
import {
  FAMILY_CHAT_POLL_MS,
  deleteFamilyAlbum,
  listFamilyAlbum,
  listFamilyMessages,
  mapFamilyAlbumToUi,
  mapFamilyChatToUi,
  sendFamilyMessage as postFamilyMessage,
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
import { useRouter } from "vue-router";
import PortalHomeModals from "../../components/portal/PortalHomeModals.vue";
import {
  fetchActivities,
  fetchCourses,
  fetchNews,
  fetchActivity,
  fetchLifeServices,
  type PortalActivity,
  type PortalCourse,
  type PortalNews
} from "../../api/portal";
import { resolveMediaUrl } from "../../utils/mediaUrl";
import { reportElderLocation } from "../../api/location";
import {
  getElderProfile,
  listNearbyDoctors,
  listNearbyHospitals,
  upsertElderProfile,
  type NearbyDoctorDto,
  type NearbyHospitalDto
} from "../../api/elder";
import { triggerSosAlarm } from "../../api/alarm";
import {
  createServiceOrder,
  formatServiceOrderError,
  listServiceOrders,
  serviceOrderStatusClass,
  type ServiceOrderDto
} from "../../api/serviceOrder";
import { deepSeekChat } from "../../api/deepseek";
import { postMeasurement } from "../../api/measurement";

const router = useRouter();
const currentTab = ref<"home" | "health" | "mine">("home");

const bannerSlides = [
  { src: "/images/banner-home.png", alt: "智慧养老 · 银发智盾" },
  { src: "/images/banner-home-2.png", alt: "湖畔相伴 · 幸福晚年" },
  { src: "/images/banner-home-3.png", alt: "公园漫步 · 健康生活" }
];
const bannerIndex = ref(0);
let bannerTimer: ReturnType<typeof setInterval> | null = null;

function setBanner(i: number) {
  bannerIndex.value = i;
  startBannerTimer();
}

function startBannerTimer() {
  if (bannerTimer !== null) clearInterval(bannerTimer);
  bannerTimer = setInterval(() => {
    bannerIndex.value = (bannerIndex.value + 1) % bannerSlides.length;
  }, 5000);
}

function stopBannerTimer() {
  if (bannerTimer !== null) {
    clearInterval(bannerTimer);
    bannerTimer = null;
  }
}

const elderProfile = ref<{ name: string } | null>(null);
const elderName = computed(() => elderProfile.value?.name || localStorage.getItem("elder.userName") || "长者");
const elderId = computed(() => localStorage.getItem("elder.id") ?? "");

async function refreshElderDisplayName() {
  if (!elderId.value) return;
  try {
    const p = await getElderProfile(elderId.value);
    elderProfile.value = { name: p.name };
    localStorage.setItem("elder.userName", p.name);
    localStorage.setItem("elder.accountName", p.name);
  } catch {
    elderProfile.value = null;
  }
}

const portalModals = ref<InstanceType<typeof PortalHomeModals> | null>(null);
const activities = ref<PortalActivity[]>([]);
const courses = ref<PortalCourse[]>([]);
const newsList = ref<PortalNews[]>([]);
const portalMode = ref<"activity" | "course" | "news" | null>(null);
const showPortalListModal = ref(false);

type SearchItem = { id: string; name: string; icon: string; action: string };
const searchKeyword = ref("");
const showSearchResults = ref(false);
const searchResults = ref<SearchItem[]>([]);

const allFunctions: SearchItem[] = [
  { id: "2", name: "健康上报", icon: "📊", action: "health" },
  { id: "3", name: "亲情互动", icon: "💬", action: "family" },
  { id: "4", name: "文娱活动", icon: "🎨", action: "activity" },
  { id: "5", name: "学习中心", icon: "📚", action: "course" },
  { id: "6", name: "新闻资讯", icon: "📰", action: "news" },
  { id: "7", name: "上报位置", icon: "📍", action: "location" },
  { id: "8", name: "健康档案", icon: "📋", action: "health" },
  { id: "9", name: "吃药提醒", icon: "💊", action: "family" },
  { id: "10", name: "健康评估", icon: "📊", action: "health" },
  { id: "11", name: "个人信息", icon: "👤", action: "profile" },
  { id: "12", name: "修改密码", icon: "🔒", action: "password" },
  { id: "13", name: "地址管理", icon: "📍", action: "address" },
  { id: "14", name: "我的订单", icon: "📋", action: "orders" }
];

const portalSectionTitle = computed(() => {
  if (portalMode.value === "activity") return "文娱活动";
  if (portalMode.value === "course") return "学习中心";
  if (portalMode.value === "news") return "新闻资讯";
  return "";
});

const portalListItems = computed(() => {
  if (portalMode.value === "activity") {
    return activities.value.map((a) => ({
      id: a.id,
      title: a.title,
      subtitle: a.timeLabel,
      icon: a.icon,
      imageUrl: a.imageUrl,
      raw: a
    }));
  }
  if (portalMode.value === "course") {
    return courses.value.map((c) => ({
      id: c.id,
      title: c.title,
      subtitle: c.category,
      icon: c.icon,
      imageUrl: c.imageUrl,
      raw: c
    }));
  }
  if (portalMode.value === "news") {
    return newsList.value.map((n) => ({
      id: n.id,
      title: n.title,
      subtitle: n.publishDate,
      icon: n.icon,
      imageUrl: n.imageUrl,
      raw: n
    }));
  }
  return [];
});

async function loadPortalContent() {
  try {
    const [a, c, n] = await Promise.all([fetchActivities(), fetchCourses(), fetchNews()]);
    activities.value = a;
    courses.value = c;
    newsList.value = n;
    await fetchLifeServices();
  } catch {
    /* 后端未启动时保留空列表 */
  }
}

function openPortalListModal(mode: "activity" | "course" | "news") {
  portalMode.value = mode;
  showPortalListModal.value = true;
}

function closePortalListModal() {
  showPortalListModal.value = false;
  portalMode.value = null;
}

function onPortalActivity() {
  if (activities.value.length === 1) {
    void portalModals.value?.openActivity(activities.value[0]);
    return;
  }
  openPortalListModal("activity");
}

function onPortalCourse() {
  if (courses.value.length === 1) {
    void portalModals.value?.openCourse(courses.value[0]);
    return;
  }
  openPortalListModal("course");
}

function onPortalNews() {
  if (newsList.value.length === 1) {
    void portalModals.value?.openNews(newsList.value[0]);
    return;
  }
  openPortalListModal("news");
}

function onPortalListItemClick(item: { id: string }) {
  const mode = portalMode.value;
  closePortalListModal();
  if (mode === "activity") {
    const act = activities.value.find((a) => a.id === item.id);
    if (act) void portalModals.value?.openActivity(act);
  } else if (mode === "course") {
    void portalModals.value?.openCourse({ id: item.id });
  } else if (mode === "news") {
    void portalModals.value?.openNews({ id: item.id });
  }
}

function onSearchInput() {
  const keyword = searchKeyword.value.trim().toLowerCase();
  if (!keyword) {
    searchResults.value = [];
    return;
  }
  searchResults.value = allFunctions.filter(
    (item) => item.name.includes(keyword) || item.name.toLowerCase().includes(keyword)
  );
}

function focusSearch() {
  showSearchResults.value = true;
  onSearchInput();
}

function hideSearchResults() {
  setTimeout(() => {
    showSearchResults.value = false;
  }, 200);
}

function onSearchResultClick(result: SearchItem) {
  showSearchResults.value = false;
  searchKeyword.value = "";
  switch (result.action) {
    case "service":
      onServiceOrder();
      break;
    case "health":
      onHealthReport();
      break;
    case "family":
      onFamily();
      break;
    case "location":
      onReportLocation();
      break;
    case "activity":
      onPortalActivity();
      break;
    case "course":
      onPortalCourse();
      break;
    case "news":
      onPortalNews();
      break;
    case "profile":
      openProfile();
      break;
    case "password":
      openChangePassword();
      break;
    case "address":
      openAddressManage();
      break;
    case "orders":
      openMyOrders();
      break;
  }
}

const showAiChat = ref(false);
const aiInput = ref("");
const aiBusy = ref(false);
const aiMessages = ref<Array<{ id: number; role: string; content: string }>>([]);

function toggleAiChat() {
  showAiChat.value = !showAiChat.value;
}

async function sendAiMessage() {
  const text = aiInput.value.trim();
  if (!text || aiBusy.value) return;
  aiMessages.value.push({ id: Date.now(), role: "user", content: text });
  aiInput.value = "";
  aiBusy.value = true;
  try {
    const turns = aiMessages.value.map(m => ({
      role: (m.role === "user" ? "user" : "assistant") as "user" | "assistant",
      text: m.content
    }));
    const reply = await deepSeekChat(turns);
    aiMessages.value.push({ id: Date.now() + 1, role: "ai", content: reply });
  } catch (e: unknown) {
    aiMessages.value.push({
      id: Date.now() + 1,
      role: "ai",
      content: e instanceof Error ? e.message : "助手暂时不可用，请稍后再试。"
    });
  } finally {
    aiBusy.value = false;
  }
}

const vitalsForm = reactive({
  hr: 78,
  sbp: 125,
  dbp: 80,
  spo2: 96,
  glucose: 5.6,
  weight: 65,
  height: 167,
  bmi: 23.3,
  temperature: 36.5
});

const steps = ref(3534);
const stepsYesterday = ref(3200);
const rank = ref(15);

const networkLabel = computed(() => (navigator.onLine ? "在线" : "离线"));
const showSosConfirm = ref(false);
const showVitalsModal = ref(false);
const showServiceOrderModal = ref(false);
const showFamilyModal = ref(false);
const incomingVideoCall = ref<FamilyVideoCallSession | null>(null);
const activeFamilyCallId = ref("");
let unsubscribeVideoSignal: (() => void) | null = null;
const familyTab = ref<"chat" | "album" | "video">("chat");
const familyChatInput = ref("");
const chatMessages = ref<{ id: string; mine: boolean; text: string; meta: string }[]>([]);
let familySyncPollTimer: number | undefined;

async function loadFamilyChat() {
  const eid = elderId.value;
  if (!eid) return;
  try {
    chatMessages.value = mapFamilyChatToUi(await listFamilyMessages(eid, 100), "ELDER");
  } catch {
    /* keep */
  }
}

async function loadFamilyAlbum() {
  const eid = elderId.value;
  if (!eid) return;
  try {
    albumItems.value = mapFamilyAlbumToUi(await listFamilyAlbum(eid));
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

const showToast = ref(false);
const toastTitle = ref("");
const toastDesc = ref("");
const toastIcon = ref("✓");

function showToastMessage(title: string, desc: string, icon = "✓") {
  toastTitle.value = title;
  toastDesc.value = desc;
  toastIcon.value = icon;
  showToast.value = true;
  setTimeout(() => {
    showToast.value = false;
  }, 3000);
}

function vitalsStorageKey() {
  return elderId.value ? `elder.vitals.${elderId.value}` : "";
}

function updateBmi() {
  const w = Number(vitalsForm.weight);
  const h = Number(vitalsForm.height) / 100;
  if (w > 0 && h > 0) {
    vitalsForm.bmi = Math.round((w / (h * h)) * 10) / 10;
  }
}

function saveVitalsToLocal() {
  const key = vitalsStorageKey();
  if (!key) return;
  localStorage.setItem(key, JSON.stringify({ ...vitalsForm }));
}

function loadVitalsFromLocal() {
  const key = vitalsStorageKey();
  if (!key) return;
  const raw = localStorage.getItem(key);
  if (!raw) return;
  try {
    const parsed = JSON.parse(raw) as Partial<typeof vitalsForm>;
    Object.assign(vitalsForm, parsed);
    updateBmi();
  } catch {
    /* ignore */
  }
}

function onSOS() {
  showSosConfirm.value = true;
}

function closeSosConfirm() {
  showSosConfirm.value = false;
}

async function confirmSOS() {
  showSosConfirm.value = false;
  if (!elderId.value) {
    showToastMessage("未登录", "请先登录长者账号", "❌");
    return;
  }
  try {
    await triggerSosAlarm(elderId.value);
    showToastMessage("SOS 求助已触发", "已发送告警至家属及养老服务机构", "⚠️");
  } catch (e: unknown) {
    showToastMessage("触发失败", (e as { message?: string })?.message ?? "请稍后重试", "❌");
  }
}

type ServiceTypeOption = "NURSING" | "HOUSEKEEPING" | "ACCOMPANY";

function onServiceOrder(preset?: ServiceTypeOption) {
  const types: ServiceTypeOption[] = ["NURSING", "HOUSEKEEPING", "ACCOMPANY"];
  if (preset && types.includes(preset)) service.serviceType = preset;
  showServiceOrderModal.value = true;
}

function onHealthReport() {
  showVitalsModal.value = true;
}

function onFamily() {
  showFamilyModal.value = true;
  familyTab.value = "chat";
  startFamilySyncPoll();
}

function onReportLocation() {
  const id = elderId.value;
  if (!id) {
    showToastMessage("未登录", "未找到长者账号，请重新登录", "❌");
    return;
  }
  if (!navigator.geolocation) {
    showToastMessage("定位失败", "当前浏览器不支持定位", "❌");
    return;
  }
  navigator.geolocation.getCurrentPosition(
    async (pos) => {
      try {
        await reportElderLocation(id, {
          lat: pos.coords.latitude,
          lng: pos.coords.longitude,
          accuracyMeters: pos.coords.accuracy,
          source: "elder_app_browser"
        });
        showToastMessage("位置已上报", "家属与机构端地图将显示最新坐标", "📍");
      } catch (e: unknown) {
        showToastMessage("上报失败", (e as { message?: string })?.message ?? "请检查网络后重试", "❌");
      }
    },
    () => showToastMessage("获取位置失败", "请在系统设置中授权定位权限", "❌"),
    { enableHighAccuracy: true, timeout: 20000, maximumAge: 0 }
  );
}


const showDeviceBindModal = ref(false);
const showHospitalModal = ref(false);
const showDoctorModal = ref(false);
const showAssessmentModal = ref(false);
const assessmentModalTitle = ref("健康评估报告");
const deviceSn = ref("");
const deviceBindStatus = ref<{ type: string; text: string } | null>(null);

const hospitalList = ref<NearbyHospitalDto[]>([]);
const doctorList = ref<NearbyDoctorDto[]>([]);
const hospitalLoading = ref(false);
const doctorLoading = ref(false);
const medicalRegionHint = ref("");

function buildMedicalRegionHint(
  res: { source: string; userAddress: string; regionLabel: string },
  fallbackAddress: string
) {
  const a = (res.userAddress || fallbackAddress || "").trim();
  const sourceLabel =
    res.source === "amap"
      ? "高德地图 · 按住址周边实时检索"
      : "演示数据（请配置高德 Key 或完善住址后重试）";
  if (!a) {
    return `${sourceLabel}；未填写住址，建议在「地址管理」中完善。`;
  }
  const region = res.regionLabel ? `（${res.regionLabel}）` : "";
  return `${sourceLabel}${region} · 住址：${a}`;
}

async function loadNearbyHospitals() {
  if (!elderId.value) {
    showToastMessage("未登录", "请先登录长者账号", "❌");
    return;
  }
  hospitalLoading.value = true;
  hospitalList.value = [];
  try {
    if (!profileForm.address?.trim()) {
      await loadProfileFromServer();
    }
    const res = await listNearbyHospitals(elderId.value);
    hospitalList.value = res.items ?? [];
    medicalRegionHint.value = buildMedicalRegionHint(res, profileForm.address);
  } catch (e: unknown) {
    showToastMessage("加载失败", (e as { message?: string })?.message ?? "无法获取医院列表", "❌");
  } finally {
    hospitalLoading.value = false;
  }
}

async function loadNearbyDoctors() {
  if (!elderId.value) {
    showToastMessage("未登录", "请先登录长者账号", "❌");
    return;
  }
  doctorLoading.value = true;
  doctorList.value = [];
  try {
    if (!profileForm.address?.trim()) {
      await loadProfileFromServer();
    }
    const res = await listNearbyDoctors(elderId.value);
    doctorList.value = res.items ?? [];
    medicalRegionHint.value = buildMedicalRegionHint(res, profileForm.address);
  } catch (e: unknown) {
    showToastMessage("加载失败", (e as { message?: string })?.message ?? "无法获取医生列表", "❌");
  } finally {
    doctorLoading.value = false;
  }
}

const healthAssessment = computed(() => {
  const hr = vitalsForm.hr || 0;
  const sbp = vitalsForm.sbp || 0;
  const spo2 = vitalsForm.spo2 || 0;
  let score = 85;
  if (hr > 100 || hr < 60) score -= 8;
  if (sbp > 140) score -= 10;
  if (spo2 < 94) score -= 12;
  score = Math.max(55, Math.min(98, score));
  const level = score >= 85 ? "良好" : score >= 70 ? "一般" : "需关注";
  const levelClass = score >= 85 ? "ok" : score >= 70 ? "warn" : "risk";
  return {
    score,
    level,
    levelClass,
    items: [
      { name: "心率", status: hr >= 60 && hr <= 100 ? "正常" : "偏高/偏低", statusClass: hr >= 60 && hr <= 100 ? "ok" : "warn" },
      { name: "血压", status: sbp <= 140 ? "正常" : "偏高", statusClass: sbp <= 140 ? "ok" : "warn" },
      { name: "血氧", status: spo2 >= 94 ? "正常" : "偏低", statusClass: spo2 >= 94 ? "ok" : "risk" },
      { name: "活动量", status: steps.value >= 3000 ? "达标" : "偏少", statusClass: steps.value >= 3000 ? "ok" : "warn" }
    ],
    advice: score >= 85
      ? "整体状态良好，请保持规律作息与适量运动。"
      : "建议关注血压与血氧变化，必要时联系家属或医生复诊。"
  };
});

function onDeviceBind() {
  showDeviceBindModal.value = true;
  deviceBindStatus.value = null;
}

function confirmDeviceBind() {
  const sn = deviceSn.value.trim();
  if (!sn) {
    deviceBindStatus.value = { type: "error", text: "请输入设备编号" };
    return;
  }
  deviceBindStatus.value = { type: "success", text: `设备 ${sn} 绑定成功，体征将自动同步` };
  setTimeout(() => {
    showDeviceBindModal.value = false;
    showToastMessage("绑定成功", "健康一体机已关联您的账号", "✅");
  }, 800);
}

async function onFindHospital() {
  showHospitalModal.value = true;
  await loadNearbyHospitals();
}

async function onFindDoctor() {
  showDoctorModal.value = true;
  await loadNearbyDoctors();
}

function onHealthAssessment() {
  loadVitalsFromLocal();
  assessmentModalTitle.value = "健康评估报告";
  showAssessmentModal.value = true;
}

const showAlertSettingsModal = ref(false);

type AlertSettingKey =
  | "fallAlert"
  | "heartRateAlert"
  | "spo2Alert"
  | "sosAlert"
  | "inactiveAlert"
  | "notifyFamily";

const alertSettings = reactive<Record<AlertSettingKey, boolean>>({
  fallAlert: true,
  heartRateAlert: true,
  spo2Alert: true,
  sosAlert: true,
  inactiveAlert: true,
  notifyFamily: true
});

const alertSettingRules: {
  key: AlertSettingKey;
  label: string;
  desc: string;
  icon: string;
  iconClass: string;
}[] = [
  { key: "fallAlert", label: "跌倒预警", desc: "检测到跌倒风险或异常姿态时提醒", icon: "🚨", iconClass: "red" },
  { key: "heartRateAlert", label: "心率异常", desc: "心率 >100 或 <60 次/分时提醒", icon: "❤️", iconClass: "pink" },
  { key: "spo2Alert", label: "血氧偏低", desc: "血氧饱和度低于 94% 时提醒", icon: "🫁", iconClass: "blue" },
  { key: "sosAlert", label: "SOS 联动", desc: "一键呼救时同步通知家属与机构", icon: "🆘", iconClass: "red" },
  { key: "inactiveAlert", label: "久未活动", desc: "超过 30 分钟无活动时提醒", icon: "⏱️", iconClass: "orange" },
  { key: "notifyFamily", label: "通知家属", desc: "预警同时推送至子女端", icon: "👨‍👩‍👧", iconClass: "green" }
];

function alertSettingsStorageKey() {
  return elderId.value ? `elder.alertSettings.${elderId.value}` : "";
}

function loadAlertSettingsFromLocal() {
  const key = alertSettingsStorageKey();
  if (!key) return;
  const raw = localStorage.getItem(key);
  if (!raw) return;
  try {
    const parsed = JSON.parse(raw) as Partial<Record<AlertSettingKey, boolean>>;
    for (const rule of alertSettingRules) {
      if (typeof parsed[rule.key] === "boolean") {
        alertSettings[rule.key] = parsed[rule.key]!;
      }
    }
  } catch {
    /* ignore */
  }
}

function saveAlertSettingsToLocal() {
  const key = alertSettingsStorageKey();
  if (!key) return;
  localStorage.setItem(key, JSON.stringify({ ...alertSettings }));
}

function onAlertSettings() {
  loadAlertSettingsFromLocal();
  showAlertSettingsModal.value = true;
}

function toggleAlertRule(key: AlertSettingKey) {
  alertSettings[key] = !alertSettings[key];
}

function saveAlertSettings() {
  saveAlertSettingsToLocal();
  showAlertSettingsModal.value = false;
  const enabled = alertSettingRules.filter((r) => alertSettings[r.key]).length;
  showToastMessage("预警设置已保存", `已开启 ${enabled} 项监测规则`, "✅");
}

function openAlarmCenter() {
  showAlertSettingsModal.value = false;
  router.push("/elder/alarms");
}

function openVitalsFromAssessment() {
  showAssessmentModal.value = false;
  showVitalsModal.value = true;
}

function callPhone(phone: string) {
  showToastMessage("拨打电话", phone, "📞");
}

function onHealthAlarms() {
  router.push("/elder/alarms");
}

function onCallFamily() {
  showToastMessage("一键联系家属", "正在呼叫紧急联系人...", "📞");
}

function onSafetyGuide() {
  showToastMessage("处置指南", "正在加载安全处置指南...", "📖");
}

async function submitVitals() {
  vitalsForm.hr = Math.max(30, Math.min(220, Number(vitalsForm.hr) || vitalsForm.hr));
  vitalsForm.sbp = Math.max(70, Math.min(260, Number(vitalsForm.sbp) || vitalsForm.sbp));
  vitalsForm.dbp = Math.max(40, Math.min(180, Number(vitalsForm.dbp) || vitalsForm.dbp));
  vitalsForm.spo2 = Math.max(60, Math.min(100, Number(vitalsForm.spo2) || vitalsForm.spo2));
  vitalsForm.glucose = Math.max(1, Math.min(30, Number(vitalsForm.glucose) || vitalsForm.glucose));
  updateBmi();
  saveVitalsToLocal();
  showVitalsModal.value = false;
  let desc = "数据已保存至本机，请保持良好生活习惯";
  if (elderId.value) {
    try {
      const result = await postMeasurement({
        deviceId: `manual-${elderId.value}`,
        elderId: elderId.value,
        timestampMillis: Date.now(),
        heartRate: vitalsForm.hr || null,
        systolic: vitalsForm.sbp || null,
        diastolic: vitalsForm.dbp || null
      });
      desc = result.alarmCreated
        ? "数据已同步至平台，系统已生成体征告警，家属端将收到通知"
        : "数据已同步至平台与家属端";
    } catch (e) {
      desc = e instanceof Error ? e.message : "服务端同步失败，数据仅保存在本机";
    }
  }
  showToastMessage("健康数据已提交", desc, "✅");
}

async function confirmServiceOrder() {
  if (!elderId.value) {
    showToastMessage("未登录", "请先登录长者账号", "❌");
    return;
  }
  const appt = new Date(service.appointmentTime);
  if (Number.isNaN(appt.getTime())) {
    showToastMessage("提示", "请选择有效的预约时间", "⚠️");
    return;
  }
  try {
    await createServiceOrder(elderId.value, {
      serviceType: service.serviceType,
      appointmentTimeMillis: appt.getTime(),
      notes: service.notes?.trim() || "",
      bookedByRole: "ELDER",
      bookedByUserId: elderId.value,
      bookedByName: profileForm.name || elderProfile.value?.name || localStorage.getItem("elder.userName") || ""
    });
    showServiceOrderModal.value = false;
    await loadServiceOrders();
    showToastMessage("预约成功", "已同步至服务机构，请等待派单", "✅");
  } catch (e: unknown) {
    showToastMessage("预约失败", formatServiceOrderError(e), "❌");
  }
}

async function sendFamilyMessage() {
  const text = familyChatInput.value.trim();
  const eid = elderId.value;
  if (!text || !eid) return;
  const name =
    profileForm.name ||
    elderProfile.value?.name ||
    localStorage.getItem("elder.userName") ||
    "老人";
  try {
    await postFamilyMessage(eid, {
      senderRole: "ELDER",
      senderUserId: eid,
      senderName: name,
      content: text
    });
    familyChatInput.value = "";
    await loadFamilyChat();
  } catch (e: unknown) {
    showToastMessage("发送失败", (e as { message?: string })?.message ?? "请稍后重试", "❌");
  }
}

type AlbumItem = { id: string; name: string; url: string; createdAt: string };
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
    await uploadFamilyAlbum(eid, file, "ELDER", eid);
    await loadFamilyAlbum();
    showToastMessage("上传成功", "照片已同步至家庭相册", "✅");
  } catch (e: unknown) {
    showToastMessage("上传失败", (e as { message?: string })?.message ?? "请重试", "❌");
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
    showToastMessage("删除失败", (e as { message?: string })?.message ?? "请重试", "❌");
  }
}

const videoState = reactive({
  status: "idle" as "idle" | "dialing" | "connected" | "ended",
  muted: false,
  cameraOn: true,
  speakerOn: true,
  durationSec: 0
});
const localVideoRef = ref<HTMLVideoElement | null>(null);
const remoteVideoRef = ref<HTMLVideoElement | null>(null);
let localMediaStream: MediaStream | null = null;
let remoteMediaStream: MediaStream | null = null;
let familyRtc: FamilyVideoRtc | null = null;
let elderSignalPollTimer: number | null = null;
let videoCallTimer: number | null = null;
const callPeerName = ref("子女");

const callPeerInitial = computed(() => {
  const n = callPeerName.value.trim();
  return n ? n.slice(0, 1) : "子";
});

const videoStatusText = computed(() => {
  if (videoState.status === "dialing") return "正在呼叫家属...";
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
}

function clearElderSignalPoll() {
  if (elderSignalPollTimer != null) {
    window.clearInterval(elderSignalPollTimer);
    elderSignalPollTimer = null;
  }
}

function stopLocalMedia() {
  familyRtc?.stop();
  familyRtc = null;
  if (localMediaStream) {
    localMediaStream.getTracks().forEach((t) => t.stop());
    localMediaStream = null;
  }
  remoteMediaStream = null;
  bindStreamToVideo(localVideoRef.value, null);
  bindStreamToVideo(remoteVideoRef.value, null);
}

async function bindLocalVideoStream() {
  await nextTick();
  bindStreamToVideo(localVideoRef.value, localMediaStream);
  applyStreamTrackState(localMediaStream, videoState.muted, videoState.cameraOn);
}

async function bindRemoteVideoStream() {
  if (!remoteMediaStream) return;
  await nextTick();
  bindStreamToVideo(remoteVideoRef.value, remoteMediaStream);
}

function rememberCallPeer(session: FamilyVideoCallSession | { childName?: string }) {
  const name = session.childName?.trim();
  if (name) callPeerName.value = name;
}

async function ensureLocalMedia() {
  if (localMediaStream) {
    await bindLocalVideoStream();
    return;
  }
  localMediaStream = await openLocalCamera();
  await bindLocalVideoStream();
}

function toggleVideoMute() {
  videoState.muted = !videoState.muted;
  applyStreamTrackState(localMediaStream, videoState.muted, videoState.cameraOn);
}

function toggleVideoCamera() {
  videoState.cameraOn = !videoState.cameraOn;
  applyStreamTrackState(localMediaStream, videoState.muted, videoState.cameraOn);
}

async function beginConnectedTimer() {
  if (videoState.status === "connected") return;
  clearVideoTimers();
  videoState.status = "connected";
  videoCallTimer = window.setInterval(() => {
    videoState.durationSec += 1;
  }, 1000);
  await bindLocalVideoStream();
  await bindRemoteVideoStream();
}

async function startFamilyRtc(role: "caller" | "callee") {
  const eid = elderId.value;
  const callId = activeFamilyCallId.value;
  if (!eid || !callId || !localMediaStream) return;
  familyRtc?.stop();
  familyRtc = new FamilyVideoRtc(eid, callId, role, (remote) => {
    remoteMediaStream = remote;
    void bindRemoteVideoStream();
  });
  await familyRtc.start(localMediaStream);
}

function startElderSignalPoll() {
  clearElderSignalPoll();
  const eid = elderId.value;
  const callId = activeFamilyCallId.value;
  if (!eid || !callId) return;
  elderSignalPollTimer = window.setInterval(() => {
    void (async () => {
      const session = await readFamilyVideoSession(eid);
      if (!session || session.callId !== callId) return;
      if (session.status === "connected" && videoState.status === "dialing") {
        rememberCallPeer(session);
        await beginConnectedTimer();
        await nextTick();
        await startFamilyRtc(session.initiatorRole === "ELDER" ? "caller" : "callee");
        await bindRemoteVideoStream();
        showToastMessage("已接通", `${familyCallDisplayName(session)} 已接听`, "📹");
      }
      if (session.status === "declined" && videoState.status === "dialing") {
        endVideoCall();
        showToastMessage("未接通", "对方暂未接听", "📹");
      }
      if (session.status === "ended" && (videoState.status === "connected" || videoState.status === "dialing")) {
        endVideoCall();
        showToastMessage("通话结束", "对方已挂断", "📹");
      }
    })();
  }, 600);
}

async function startVideoCall() {
  if (videoState.status === "dialing" || videoState.status === "connected") return;
  const eid = elderId.value;
  if (!eid) {
    showToastMessage("未登录", "请先登录长者账号", "❌");
    return;
  }
  clearVideoTimers();
  clearElderSignalPoll();
  videoState.durationSec = 0;
  videoState.status = "dialing";
  try {
    await ensureLocalMedia();
    const session = await writeFamilyVideoSession({
      elderId: eid,
      childId: localStorage.getItem("elder.linkedChildId") || "",
      childName: "家属",
      elderDisplayName: elderName.value,
      initiatorRole: "ELDER"
    });
    rememberCallPeer(session);
    activeFamilyCallId.value = session.callId;
    showFamilyModal.value = true;
    familyTab.value = "video";
    await nextTick();
    startElderSignalPoll();
    showToastMessage("正在呼叫", "已向子女端发起视频通话", "📹");
  } catch (e: unknown) {
    videoState.status = "idle";
    showToastMessage("呼叫失败", (e as { message?: string })?.message ?? "请稍后重试", "❌");
  }
}

function endVideoCall() {
  clearVideoTimers();
  clearElderSignalPoll();
  videoState.status = "ended";
  const eid = elderId.value;
  const callId = activeFamilyCallId.value || incomingVideoCall.value?.callId;
  if (eid && callId) {
    void patchFamilyVideoSession(eid, callId, "ended").then(() => clearFamilyVideoSession(eid, callId));
  }
  activeFamilyCallId.value = "";
  incomingVideoCall.value = null;
  stopLocalMedia();
}

async function acceptIncomingVideoCall() {
  const session = incomingVideoCall.value;
  if (!session) return;
  rememberCallPeer(session);
  activeFamilyCallId.value = session.callId;
  incomingVideoCall.value = null;
  showFamilyModal.value = true;
  familyTab.value = "video";
  videoState.durationSec = 0;
  await nextTick();
  try {
    await patchFamilyVideoSession(session.elderId, session.callId, "connected");
    await ensureLocalMedia();
    await beginConnectedTimer();
    await startFamilyRtc("callee");
    await bindRemoteVideoStream();
    showToastMessage("已接听", `${familyCallDisplayName(session)} 的视频通话已接通`, "📹");
  } catch (e: unknown) {
    showToastMessage("接听失败", (e as { message?: string })?.message ?? "请检查摄像头权限", "❌");
  }
}

async function declineIncomingVideoCall() {
  const session = incomingVideoCall.value;
  if (!session) return;
  incomingVideoCall.value = null;
  await patchFamilyVideoSession(session.elderId, session.callId, "declined");
  window.setTimeout(() => void clearFamilyVideoSession(session.elderId, session.callId), 1500);
  showToastMessage("已拒绝", "已拒绝本次远程探视", "📹");
}

function resetVideoCall() {
  clearVideoTimers();
  videoState.status = "idle";
  videoState.durationSec = 0;
  videoState.muted = false;
  videoState.cameraOn = true;
  videoState.speakerOn = true;
}

function closeFamilyModal() {
  showFamilyModal.value = false;
  stopFamilySyncPoll();
  endVideoCall();
  resetVideoCall();
}

const showProfileModal = ref(false);
const profileLoading = ref(false);
const profileSaving = ref(false);
const profileForm = reactive({
  elderId: "",
  name: "",
  age: 72,
  gender: "男",
  address: "",
  keyHealthNotes: ""
});

async function loadProfileFromServer() {
  if (!elderId.value) {
    showToastMessage("未登录", "请先登录长者账号", "❌");
    return false;
  }
  profileLoading.value = true;
  try {
    const p = await getElderProfile(elderId.value);
    profileForm.elderId = p.elderId;
    profileForm.name = p.name ?? "";
    profileForm.age = p.age ?? 70;
    profileForm.gender = p.gender || "男";
    profileForm.address = p.address ?? "";
    profileForm.keyHealthNotes = p.keyHealthNotes ?? "";
    localStorage.setItem("elder.userName", profileForm.name);
    return true;
  } catch (e: unknown) {
    showToastMessage("加载失败", (e as { message?: string })?.message ?? "无法读取个人信息", "❌");
    return false;
  } finally {
    profileLoading.value = false;
  }
}

async function openProfile() {
  showProfileModal.value = true;
  await loadProfileFromServer();
}

async function saveProfile() {
  if (!elderId.value) return;
  if (!profileForm.name.trim()) {
    showToastMessage("提示", "请输入姓名", "⚠️");
    return;
  }
  if (!profileForm.age || profileForm.age < 1) {
    showToastMessage("提示", "请输入有效年龄", "⚠️");
    return;
  }
  profileSaving.value = true;
  try {
    const saved = await upsertElderProfile(elderId.value, {
      name: profileForm.name.trim(),
      gender: profileForm.gender,
      address: profileForm.address?.trim() || "未填写",
      age: profileForm.age,
      keyHealthNotes: profileForm.keyHealthNotes?.trim() || ""
    });
    profileForm.name = saved.name;
    profileForm.address = saved.address;
    elderProfile.value = { name: saved.name };
    localStorage.setItem("elder.userName", saved.name);
    showProfileModal.value = false;
    showToastMessage("保存成功", "个人信息已同步至数据库", "✅");
  } catch (e: unknown) {
    showToastMessage("保存失败", (e as { message?: string })?.message ?? "请稍后重试", "❌");
  } finally {
    profileSaving.value = false;
  }
}

const showChangePasswordModal = ref(false);
const passwordForm = reactive({ oldPassword: "", newPassword: "", confirmPassword: "" });

function openChangePassword() {
  passwordForm.oldPassword = "";
  passwordForm.newPassword = "";
  passwordForm.confirmPassword = "";
  showChangePasswordModal.value = true;
}

function confirmChangePassword() {
  if (!passwordForm.oldPassword) {
    showToastMessage("提示", "请输入原密码", "⚠️");
    return;
  }
  if (!passwordForm.newPassword) {
    showToastMessage("提示", "请输入新密码", "⚠️");
    return;
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    showToastMessage("提示", "两次输入的密码不一致", "⚠️");
    return;
  }
  showChangePasswordModal.value = false;
  showToastMessage("密码修改成功", "请使用新密码重新登录", "✅");
}

const showAddressModal = ref(false);
const addressLoading = ref(false);
const addressSaving = ref(false);

async function openAddressManage() {
  if (!elderId.value) {
    showToastMessage("未登录", "请先登录长者账号", "❌");
    return;
  }
  showAddressModal.value = true;
  addressLoading.value = true;
  try {
    await loadProfileFromServer();
  } finally {
    addressLoading.value = false;
  }
}

async function saveAddress() {
  if (!elderId.value) return;
  const addr = profileForm.address?.trim();
  if (!addr) {
    showToastMessage("提示", "请输入详细地址", "⚠️");
    return;
  }
  if (!profileForm.name.trim()) {
    await loadProfileFromServer();
  }
  if (!profileForm.name.trim()) {
    showToastMessage("提示", "请先在个人信息中填写姓名", "⚠️");
    return;
  }
  addressSaving.value = true;
  try {
    const saved = await upsertElderProfile(elderId.value, {
      name: profileForm.name.trim(),
      gender: profileForm.gender,
      address: addr,
      age: profileForm.age,
      keyHealthNotes: profileForm.keyHealthNotes?.trim() || ""
    });
    profileForm.address = saved.address;
    showAddressModal.value = false;
    showToastMessage("保存成功", "住址已保存至数据库", "✅");
  } catch (e: unknown) {
    showToastMessage("保存失败", (e as { message?: string })?.message ?? "请稍后重试", "❌");
  } finally {
    addressSaving.value = false;
  }
}

const showRelatedUsersModal = ref(false);
const relatedUsers = ref([
  { id: 1, name: "张明", relation: "儿子", phone: "139****5678" },
  { id: 2, name: "张丽", relation: "女儿", phone: "138****9012" }
]);

function openRelatedUsers() {
  showRelatedUsersModal.value = true;
}

function deleteRelatedUser(id: number) {
  if (confirm("确定要移除该关联用户吗？")) {
    relatedUsers.value = relatedUsers.value.filter((user) => user.id !== id);
    showToastMessage("已移除关联用户", "关联关系已解除", "✅");
  }
}

function addRelatedUser() {
  const name = prompt("请输入关联用户姓名：");
  if (name) {
    relatedUsers.value.push({ id: Date.now(), name, relation: "亲友", phone: "请完善" });
    showToastMessage("关联用户已添加", "请完善用户信息", "✅");
  }
}

const showVolunteerModal = ref(false);
const volunteerInfo = ref({
  name: "李华",
  organization: "阳光志愿者协会",
  phone: "188****3456",
  description: "擅长老年人心理疏导、健康咨询，每周三下午提供上门服务"
});

function openVolunteer() {
  showVolunteerModal.value = true;
}

const showButlerModal = ref(false);
const butlerInfo = ref({
  name: "王管家",
  title: "资深养老管家",
  phone: "177****7890",
  description: "从事养老服务8年，擅长老年人生活照料、健康管理、活动组织"
});

function openMyButler() {
  showButlerModal.value = true;
}

function callButler() {
  showToastMessage("呼叫管家", "正在为您联系王管家...", "📞");
}

const showOrdersModal = ref(false);
const orderList = ref<
  { id: string; service: string; time: string; status: string; statusClass: string }[]
>([]);

function formatOrderTime(iso: string) {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function mapOrders(orders: ServiceOrderDto[]) {
  return orders.map((o) => ({
    id: o.id.slice(0, 8).toUpperCase(),
    service: o.serviceTypeLabel || o.serviceType,
    time: formatOrderTime(o.appointmentTime),
    status: o.statusLabel,
    statusClass: serviceOrderStatusClass(o.status)
  }));
}

async function loadServiceOrders() {
  if (!elderId.value) return;
  try {
    orderList.value = mapOrders(await listServiceOrders(elderId.value));
  } catch {
    orderList.value = [];
  }
}

async function openMyOrders() {
  showOrdersModal.value = true;
  await loadServiceOrders();
}

const showMemberModal = ref(false);
const memberInfo = reactive({
  level: "👑",
  levelName: "黄金会员",
  balance: 568.5,
  points: 2340,
  coupons: 3
});

function openMemberAccount() {
  showMemberModal.value = true;
}

function toggleSetting(type: "alert" | "info") {
  const status = type === "alert" ? "告警提示" : "完善信息";
  showToastMessage(`${status}已切换`, "当前状态：已开启", "⚙️");
}

const service = reactive({
  serviceType: "NURSING",
  appointmentTime: "",
  notes: ""
});

onMounted(() => {
  const now = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  service.appointmentTime = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours() + 1)}:${pad(now.getMinutes())}`;
  void refreshElderDisplayName();
  loadPortalContent();
  loadVitalsFromLocal();
  loadAlertSettingsFromLocal();
  startBannerTimer();

  const eid = elderId.value;
  if (eid) {
    watch(
      () => videoState.status,
      async (status) => {
        if (status === "connected") {
          await bindLocalVideoStream();
          await bindRemoteVideoStream();
        }
      }
    );
    watch(
      () => remoteVideoRef.value,
      () => {
        void bindRemoteVideoStream();
      }
    );
    unsubscribeVideoSignal = subscribeFamilyVideoSession(eid, (session) => {
      if (session?.status === "ringing" && session.initiatorRole === "CHILD") {
        rememberCallPeer(session);
        incomingVideoCall.value = session;
        return;
      }
      if (session?.status === "ended" || session?.status === "declined") {
        if (incomingVideoCall.value?.callId === session.callId) incomingVideoCall.value = null;
        if (activeFamilyCallId.value === session.callId) activeFamilyCallId.value = "";
        if (videoState.status === "connected" || videoState.status === "dialing") {
          endVideoCall();
          showToastMessage(
            "探视结束",
            session.status === "declined" ? "子女端已取消呼叫" : "子女端已结束远程探视",
            "📹"
          );
        }
      }
    });
  }
});

onBeforeUnmount(() => {
  stopFamilySyncPoll();
  stopBannerTimer();
  clearVideoTimers();
  clearElderSignalPoll();
  stopLocalMedia();
  unsubscribeVideoSignal?.();
  unsubscribeVideoSignal = null;
});

</script>

<style scoped>
/* ===== 全局重置 & 白色主题 ===== */
* { box-sizing: border-box; }

.app-container {
  max-width: 480px;
  margin: 0 auto;
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 70px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  color: #333;
}

/* ===== 底部导航 ===== */
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
  box-shadow: 0 -2px 10px rgba(0,0,0,0.04);
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
  color: #2196F3;
}

.nav-icon {
  font-size: 22px;
  margin-bottom: 2px;
}

/* ===== 首页 ===== */
.home-page {
  padding: 0 14px;
  padding-top: 14px;
}

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
  color: #999;
  font-size: 14px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.02);
  cursor: pointer;
}

.search-icon { font-size: 16px; }

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  color: #333;
  background: transparent;
}

.search-input::placeholder {
  color: #bbb;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ai-assistant {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #E3F2FD;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s;
  border: 1px solid #BBDEFB;
}

.ai-assistant:hover {
  background: #BBDEFB;
}

.ai-icon {
  font-size: 20px;
}

.avatar-mini {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.search-results-dropdown {
  background: white;
  border: 1px solid #e8ecf0;
  border-radius: 12px;
  padding: 8px 0;
  margin-top: -8px;
  margin-bottom: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
  max-height: 300px;
  overflow-y: auto;
  position: relative;
  z-index: 50;
}

.search-result-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.15s;
}

.search-result-item:hover {
  background: #f5f7fa;
}

.result-icon { font-size: 18px; }
.result-text { font-size: 14px; color: #333; }

.no-result {
  padding: 16px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.banner-carousel {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 14px;
  height: 160px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
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
  transition: all 0.3s;
  cursor: pointer;
}

.dot.active {
  background: #2196F3;
  width: 18px;
  border-radius: 3px;
}

.sos-container {
  display: flex;
  justify-content: center;
  margin: 6px 0 18px;
}

.sos-big-btn {
  position: relative;
  width: 110px;
  height: 110px;
  border-radius: 50%;
  background: #FF3D00;
  border: none;
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 24px rgba(255, 61, 0, 0.3);
  transition: all 0.2s;
  z-index: 1;
}

.sos-big-btn::before {
  content: '';
  position: absolute;
  top: -8px;
  left: -8px;
  right: -8px;
  bottom: -8px;
  border-radius: 50%;
  border: 2px solid rgba(255, 61, 0, 0.25);
  animation: sos-glow 2s ease-in-out infinite;
  z-index: -1;
}

.sos-big-btn::after {
  content: '';
  position: absolute;
  top: -16px;
  left: -16px;
  right: -16px;
  bottom: -16px;
  border-radius: 50%;
  border: 1.5px solid rgba(255, 61, 0, 0.12);
  animation: sos-glow 2s ease-in-out infinite 0.6s;
  z-index: -1;
}

@keyframes sos-glow {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.08); opacity: 0.3; }
  100% { transform: scale(1); opacity: 1; }
}

.sos-big-btn:active {
  transform: scale(0.92);
}

.sos-icon { font-size: 36px; margin-bottom: 2px; }
.sos-text { font-size: 15px; font-weight: 600; letter-spacing: 1px; }

.grid-container {
  background: white;
  border-radius: 16px;
  padding: 18px 14px;
  margin-bottom: 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  border: 1px solid #f0f2f5;
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
  transition: transform 0.2s;
}

.grid-item:active {
  transform: scale(0.92);
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

.blue-bg { background: #E3F2FD; color: #2196F3; }
.green-bg { background: #E8F5E9; color: #4CAF50; }
.pink-bg { background: #FCE4EC; color: #E91E63; }
.purple-bg { background: #F3E5F5; color: #9C27B0; }
.orange-bg { background: #FFF3E0; color: #FF9800; }
.blue-light-bg { background: #E8EAF6; color: #3F51B5; }
.red-bg { background: #FFEBEE; color: #F44336; }

.grid-label {
  font-size: 11px;
  color: #666;
  text-align: center;
  line-height: 1.25;
}

.notice-bar {
  background: white;
  border: 1px solid #f0f2f5;
  border-radius: 14px;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.02);
}

.notice-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.notice-icon { font-size: 20px; color: #2196F3; }
.notice-text { font-size: 14px; color: #666; }

.notice-right {
  color: #999;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.hot-section {
  background: white;
  border: 1px solid #f0f2f5;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 14px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.02);
}

.hot-title {
  color: #2196F3;
  font-weight: 600;
  font-size: 15px;
  text-align: center;
  margin-bottom: 12px;
  position: relative;
}

.hot-title::before, .hot-title::after {
  content: "";
  position: absolute;
  top: 50%;
  width: 30px;
  height: 1px;
  background: #f0f2f5;
}

.hot-title::before { left: 10%; }
.hot-title::after { right: 10%; }

.hot-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.hot-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 0;
  border-radius: 12px;
  background: #f8f9fa;
  cursor: pointer;
}

.hot-item:active {
  background: #f0f2f5;
}

.hot-icon { font-size: 24px; margin-bottom: 4px; }
.hot-name { font-size: 12px; color: #666; }

/* ===== 首页服务区适老化 ===== */
.elder-a11y-block {
  border: 2px solid #d0e3f7;
  border-radius: 18px;
  padding: 18px 14px 16px;
  background: #fff;
}

.elder-a11y-title,
.elder-a11y-section-title {
  margin: 0 0 6px;
  text-align: center;
  font-size: 20px;
  font-weight: 800;
  color: #0d3b66;
  letter-spacing: 0.5px;
}

.elder-a11y-section-title {
  margin-bottom: 14px;
}

.elder-a11y-hint {
  margin: 0 0 14px;
  text-align: center;
  font-size: 15px;
  font-weight: 600;
  color: #455a64;
  line-height: 1.4;
}

.elder-a11y-hot-grid {
  gap: 12px;
}

.elder-a11y-hot-item {
  min-height: 96px;
  padding: 14px 8px;
  border: 2px solid #cfe3f7;
  border-radius: 14px;
  background: #f3f9ff;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
}

.elder-a11y-hot-item:focus-visible {
  outline: 3px solid #1976d2;
  outline-offset: 2px;
}

.elder-a11y-hot-item:active {
  transform: scale(0.98);
  background: #e3f2fd;
}

.elder-a11y-hot-icon {
  font-size: 36px;
  margin-bottom: 8px;
  line-height: 1;
}

.elder-a11y-hot-name {
  font-size: 17px;
  font-weight: 800;
  color: #1a3347;
  line-height: 1.35;
  text-align: center;
}

.grid-row-elder {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px 12px;
}

.elder-a11y-grid-item {
  min-height: 88px;
  padding: 12px 8px 10px;
  border: 2px solid #e8ecf0;
  border-radius: 14px;
  background: #fafbfc;
  cursor: pointer;
  transition: transform 0.15s, border-color 0.15s;
}

.elder-a11y-grid-item:focus-visible {
  outline: 3px solid #1976d2;
  outline-offset: 2px;
}

.elder-a11y-grid-item:active {
  transform: scale(0.98);
  border-color: #90caf9;
  background: #f5faff;
}

.elder-a11y-grid-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  font-size: 28px;
  margin-bottom: 8px;
  border: 2px solid rgba(0, 0, 0, 0.06);
}

.elder-a11y-grid-label {
  font-size: 17px;
  font-weight: 800;
  color: #1a3347;
  line-height: 1.35;
  letter-spacing: 0.3px;
}

.elder-a11y-block .hot-title::before,
.elder-a11y-block .hot-title::after {
  display: none;
}

.elder-a11y-block button {
  width: 100%;
  font-family: inherit;
  appearance: none;
  -webkit-tap-highlight-color: transparent;
}

.elder-a11y-hot-item,
.elder-a11y-grid-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* ===== 健康页 ===== */
.health-header {
  background: linear-gradient(180deg, #E3F2FD, #f5f7fa);
  padding: 20px 16px 30px;
  text-align: center;
  border-radius: 0 0 24px 24px;
}

.health-title { font-size: 18px; font-weight: 600; color: #1a3347; margin-bottom: 16px; }

.health-avatar-ring {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 2px solid rgba(33, 150, 243, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 8px;
  background: rgba(255,255,255,0.6);
}

.health-avatar-inner {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #f0f4f8;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-icon { font-size: 32px; color: #666; }
.health-name { font-size: 16px; color: #1a3347; margin-bottom: 16px; }

.health-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.health-action-btn {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  color: #2196F3;
  border: 1px solid rgba(33, 150, 243, 0.15);
  background: rgba(255, 255, 255, 0.6);
  cursor: pointer;
}

.health-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  padding: 16px 16px;
}

.health-card {
  background: white;
  border: 1px solid #f0f2f5;
  border-radius: 14px;
  padding: 14px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.02);
}

.card-label { font-size: 12px; color: #999; font-weight: 500; }
.card-value { font-size: 20px; font-weight: 600; color: #1a3347; margin: 4px 0; }
.card-range { font-size: 11px; color: #999; }

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a3347;
}

.safety-card {
  background: white;
  border: 1px solid #f0f2f5;
  border-radius: 16px;
  padding: 16px;
  margin: 0 16px 16px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.02);
}

.warning-row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  border-radius: 12px;
  padding: 12px;
  background: #FFF3E0;
  margin-bottom: 12px;
}

.warning-badge {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #FF9800;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.warning-title {
  font-weight: 600;
  font-size: 14px;
  color: #E65100;
}

.warning-detail {
  font-size: 13px;
  color: #666;
  margin-top: 4px;
}

.safety-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 12px;
}

.danger-btn, .ok-btn {
  padding: 12px;
  border-radius: 12px;
  border: none;
  font-weight: 500;
  font-size: 14px;
  cursor: pointer;
}

.danger-btn {
  background: #FF3D00;
  color: white;
}

.ok-btn {
  background: #E3F2FD;
  color: #2196F3;
}

.health-quick-a11y {
  margin: 0 16px 16px;
}

.health-quick-a11y .elder-a11y-hint {
  margin-bottom: 12px;
}

.health-quick-grid {
  padding: 0 2px;
}

.steps-section {
  background: #E8F5E9;
  border-radius: 14px;
  margin: 0 16px 16px;
  padding: 14px;
}

.steps-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.steps-title { font-weight: 600; font-size: 14px; color: #2E7D32; }
.steps-plus {
  background: rgba(255,255,255,0.6);
  width: 24px;
  height: 24px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #2E7D32;
  cursor: pointer;
}

.steps-content {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 10px;
}

.step-item { text-align: center; }
.step-label { font-size: 11px; color: #558B2F; }
.step-number { font-size: 14px; font-weight: 500; color: #1B5E20; margin-top: 2px; }

/* ===== 我的页面 ===== */
.mine-header {
  background: linear-gradient(180deg, #E3F2FD, #f5f7fa);
  padding: 30px 20px 20px;
  border-radius: 0 0 24px 24px;
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

.mine-name { font-size: 18px; font-weight: 500; color: #1a3347; }

.menu-list {
  background: white;
  border: 1px solid #f0f2f5;
  border-radius: 14px;
  margin: 16px 14px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0,0,0,0.02);
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
}

.menu-item:last-child { border-bottom: none; }

.menu-left { display: flex; align-items: center; gap: 12px; }
.menu-icon { font-size: 18px; }
.menu-text { font-size: 14px; color: #333; }
.menu-arrow { color: #ccc; font-size: 18px; }

.menu-icon.blue { color: #2196F3; }
.menu-icon.red { color: #F44336; }
.menu-icon.pink { color: #E91E63; }
.menu-icon.green { color: #4CAF50; }
.menu-icon.orange { color: #FF9800; }
.menu-icon.blue-light { color: #3F51B5; }
.menu-icon.red-light { color: #F44336; }

.settings-section {
  background: white;
  border: 1px solid #f0f2f5;
  border-radius: 14px;
  margin: 10px 14px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0,0,0,0.02);
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #f5f5f5;
}

.setting-item:last-child { border-bottom: none; }
.setting-left { display: flex; align-items: center; gap: 12px; }
.setting-icon { font-size: 18px; }
.setting-text { font-size: 14px; color: #333; }

.setting-icon.red { color: #F44336; }
.setting-icon.blue { color: #2196F3; }

.toggle-switch {
  width: 44px;
  height: 24px;
  background: #ddd;
  border-radius: 12px;
  position: relative;
  cursor: pointer;
}

.toggle-switch.active { background: #4CAF50; }

.toggle-switch::after {
  content: '';
  width: 20px;
  height: 20px;
  background: white;
  border-radius: 50%;
  position: absolute;
  top: 2px;
  left: 2px;
  transition: transform 0.3s;
}

.toggle-switch.active::after { transform: translateX(20px); }

/* ===== AI 助手弹窗 ===== */
.ai-chat-window {
  position: fixed;
  bottom: 80px;
  right: 16px;
  width: 320px;
  max-width: 90%;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.15);
  border: 1px solid #e8ecf0;
  z-index: 150;
  display: flex;
  flex-direction: column;
  max-height: 400px;
}

.ai-chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f2f5;
  background: #E3F2FD;
  border-radius: 16px 16px 0 0;
}

.ai-chat-title { font-weight: 600; font-size: 14px; color: #1a3347; }
.ai-chat-close { background: none; border: none; font-size: 20px; color: #999; cursor: pointer; }

.ai-chat-body {
  flex: 1;
  padding: 12px 16px;
  overflow-y: auto;
  max-height: 240px;
  background: #fafbfc;
}

.ai-message {
  background: #E3F2FD;
  padding: 10px 14px;
  border-radius: 12px 12px 12px 4px;
  font-size: 13px;
  color: #1a3347;
  margin-bottom: 10px;
}

.chat-msg {
  margin-bottom: 8px;
}

.chat-msg.user {
  text-align: right;
}

.chat-msg.user .msg-bubble {
  background: #2196F3;
  color: white;
  border-radius: 12px 12px 4px 12px;
  display: inline-block;
  padding: 8px 14px;
  font-size: 13px;
}

.chat-msg.ai .msg-bubble {
  background: #f0f2f5;
  color: #333;
  border-radius: 12px 12px 12px 4px;
  display: inline-block;
  padding: 8px 14px;
  font-size: 13px;
}

.ai-chat-footer {
  display: flex;
  gap: 8px;
  padding: 10px 16px;
  border-top: 1px solid #f0f2f5;
}

.ai-input {
  flex: 1;
  border: 1px solid #e8ecf0;
  border-radius: 20px;
  padding: 8px 14px;
  font-size: 13px;
  outline: none;
}

.ai-send-btn {
  padding: 8px 16px;
  border-radius: 20px;
  border: none;
  background: #2196F3;
  color: white;
  font-size: 13px;
  cursor: pointer;
}

/* ===== 弹窗通用样式 ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
}

.modal-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  width: 340px;
  max-width: 92%;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}

.profile-modal, .password-modal, .address-modal, .related-modal,
.device-bind-modal, .hospital-modal, .doctor-modal, .assessment-modal,
.alert-settings-modal,
.volunteer-modal, .butler-modal, .orders-modal, .member-modal {
  max-width: 420px;
}
.alert-settings-list {
  border: 1px solid #f0f2f5;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 4px;
}
.alert-settings-list .setting-item {
  padding: 12px 14px;
  margin: 0;
  border-radius: 0;
  border-bottom: 1px solid #f5f5f5;
  background: #fafbfc;
}
.alert-settings-list .setting-item:last-child {
  border-bottom: none;
}
.setting-text-wrap {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.setting-desc {
  font-size: 11px;
  color: #999;
  line-height: 1.3;
}
.setting-icon.pink { color: #E91E63; }
.setting-icon.orange { color: #FF9800; }
.setting-icon.green { color: #4CAF50; }
.alert-settings-actions {
  flex-direction: column;
  gap: 8px;
}
.alert-settings-actions .btn-cancel,
.alert-settings-actions .btn-confirm {
  width: 100%;
}
.modal-hint {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 12px;
}
.bind-status {
  margin-top: 10px;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 13px;
}
.bind-status.success { background: #e8f8ef; color: #1a7f4b; }
.bind-status.error { background: #fdecea; color: #c0392b; }
.resource-list {
  max-height: 50vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.resource-item {
  padding: 12px;
  border-radius: 12px;
  background: #f7f9fc;
  border: 1px solid #eef2f7;
}
.resource-name { font-weight: 600; font-size: 15px; color: #1a1a2e; }
.resource-meta { font-size: 12px; color: #888; margin-top: 4px; }
.resource-phone { font-size: 13px; color: #4a6cf7; margin-top: 4px; }
.resource-call-btn {
  margin-top: 8px;
  padding: 6px 14px;
  border: none;
  border-radius: 8px;
  background: #4a6cf7;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
}
.assessment-score {
  text-align: center;
  padding: 16px 0;
}
.score-value { font-size: 48px; font-weight: 700; color: #4a6cf7; line-height: 1; }
.score-label { font-size: 13px; color: #888; margin-top: 4px; }
.score-level {
  display: inline-block;
  margin-top: 8px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}
.score-level.ok { background: #e8f8ef; color: #1a7f4b; }
.score-level.warn { background: #fff4e5; color: #b86e00; }
.score-level.risk { background: #fdecea; color: #c0392b; }
.assessment-items { display: flex; flex-direction: column; gap: 8px; margin: 12px 0; }
.assessment-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 12px;
  background: #f7f9fc;
  border-radius: 8px;
  font-size: 14px;
}
.assessment-status.ok { color: #1a7f4b; }
.assessment-status.warn { color: #b86e00; }
.assessment-status.risk { color: #c0392b; }
.assessment-advice {
  font-size: 13px;
  color: #555;
  line-height: 1.6;
  padding: 10px 12px;
  background: #f0f4ff;
  border-radius: 8px;
}


.vitals-modal, .service-modal {
  width: 380px;
}

.modal-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.modal-title { font-size: 18px; font-weight: 600; color: #1a3347; }
.modal-close { background: none; border: none; font-size: 24px; color: #999; cursor: pointer; }

.modal-actions {
  display: flex;
  gap: 10px;
}

.btn-cancel, .btn-confirm {
  flex: 1;
  padding: 12px;
  border-radius: 12px;
  border: none;
  font-weight: 500;
  cursor: pointer;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-confirm {
  background: #2196F3;
  color: white;
}

/* ===== 个人信息弹窗 ===== */
.profile-content {
  padding: 8px 0;
}

.profile-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.profile-item:last-child {
  border-bottom: none;
}

.profile-label {
  color: #999;
  font-weight: 500;
}

.profile-value {
  color: #333;
  font-weight: 500;
}

.profile-actions {
  margin-top: 16px;
}

.profile-form { padding: 4px 0 8px; }
.profile-notes { resize: vertical; min-height: 72px; }
.profile-hint {
  font-size: 12px;
  color: #888;
  line-height: 1.5;
  margin: 4px 0 0;
}
.address-editor {
  margin-top: 12px;
  padding: 12px;
  border-radius: 12px;
  background: #f7f9fc;
  border: 1px solid #eef2f7;
}
.addr-default-check {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #555;
  margin: 8px 0 4px;
}
.address-editor-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}
.address-editor-actions .btn-cancel,
.address-editor-actions .btn-confirm {
  flex: 1;
}


/* ===== 修改密码弹窗 ===== */
.password-form {
  padding: 8px 0;
}

.form-group {
  margin-bottom: 14px;
}

.form-group label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 14px;
}

.form-input:focus {
  outline: none;
  border-color: #2196F3;
}

/* ===== 地址管理弹窗 ===== */
.address-list, .related-list, .order-list {
  max-height: 300px;
  overflow-y: auto;
  padding: 8px 0;
}

.address-item, .related-item, .order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.address-item:last-child, .related-item:last-child, .order-item:last-child {
  border-bottom: none;
}

.addr-info {
  flex: 1;
}

.addr-name {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.addr-detail {
  font-size: 13px;
  color: #666;
  margin-top: 2px;
}

.addr-default {
  display: inline-block;
  background: #E3F2FD;
  color: #2196F3;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  margin-top: 4px;
}

.addr-actions {
  display: flex;
  gap: 8px;
}

.addr-edit, .addr-delete {
  padding: 4px 12px;
  border: 1px solid #e8ecf0;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  font-size: 12px;
}

.addr-delete {
  color: #F44336;
  border-color: #FFCDD2;
}

/* ===== 关联用户弹窗 ===== */
.related-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.related-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #E3F2FD;
  color: #2196F3;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 18px;
}

.related-info {
  flex: 1;
}

.related-name {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.related-relation {
  font-size: 13px;
  color: #666;
}

.related-phone {
  font-size: 12px;
  color: #999;
}

.related-delete {
  padding: 4px 12px;
  border: 1px solid #FFCDD2;
  border-radius: 6px;
  background: #FFEBEE;
  color: #F44336;
  cursor: pointer;
  font-size: 12px;
}

/* ===== 志愿者弹窗 ===== */
.volunteer-card, .butler-card {
  padding: 12px 0;
}

.volunteer-avatar, .butler-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #E3F2FD;
  color: #2196F3;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 24px;
  margin: 0 auto 12px;
}

.volunteer-detail, .butler-detail {
  text-align: center;
}

.volunteer-name, .butler-name {
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

.volunteer-org, .butler-title {
  font-size: 14px;
  color: #666;
  margin-top: 2px;
}

.volunteer-phone, .butler-phone {
  font-size: 14px;
  color: #2196F3;
  margin-top: 4px;
}

.volunteer-desc, .butler-desc {
  font-size: 13px;
  color: #666;
  margin-top: 8px;
  line-height: 1.5;
}

.butler-actions {
  margin-top: 16px;
}

/* ===== 订单弹窗 ===== */
.order-item {
  display: block;
  padding: 12px 0;
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.order-id {
  font-size: 12px;
  color: #999;
}

.order-status {
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 10px;
}

.order-status.completed {
  background: #E8F5E9;
  color: #4CAF50;
}

.order-status.processing {
  background: #FFF3E0;
  color: #FF9800;
}

.order-status.pending {
  background: #E3F2FD;
  color: #2196F3;
}

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-service {
  font-size: 14px;
  color: #333;
}

.order-time {
  font-size: 12px;
  color: #999;
}

.order-amount {
  font-weight: 600;
  font-size: 16px;
  color: #1a3347;
}

/* ===== 会员账户弹窗 ===== */
.member-content {
  text-align: center;
  padding: 12px 0;
}

.member-level {
  margin-bottom: 20px;
}

.level-badge {
  font-size: 48px;
  margin-bottom: 4px;
}

.level-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a3347;
}

.member-stats {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 12px;
  margin-bottom: 20px;
}

.stat-item {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 12px;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #1a3347;
  margin-top: 4px;
}

/* ===== 健康上报弹窗 ===== */
.vitals-form, .service-form {
  padding: 4px 0 12px;
}

.form-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.form-label {
  font-weight: 500;
  color: #666;
  font-size: 14px;
  min-width: 80px;
}

.stepper {
  display: grid;
  grid-template-columns: 44px 1fr 44px;
  gap: 8px;
  align-items: center;
  width: 180px;
}

.step-btn {
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  padding: 6px 0;
  background: white;
  font-weight: 600;
  cursor: pointer;
}

.step-value {
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  padding: 6px 0;
  text-align: center;
  font-weight: 500;
  background: #f8f9fa;
}

.bp-input {
  display: flex;
  gap: 8px;
  align-items: center;
  width: 180px;
}

.bp-input input {
  flex: 1;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 14px;
  width: 70px;
}

.wide-input {
  width: 180px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 14px;
}

.wide-select {
  width: 180px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 14px;
}

/* ===== Toast ===== */
.toast {
  position: fixed;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 420px;
  background: white;
  border-radius: 16px;
  padding: 14px 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 300;
  box-shadow: 0 8px 30px rgba(0,0,0,0.12);
  border: 1px solid #f0f2f5;
}

.toast-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  background: #4CAF50;
  color: white;
}

.toast-content { flex: 1; }
.toast-title { font-weight: 600; font-size: 14px; color: #1a3347; }
.toast-desc { font-size: 12px; color: #999; margin-top: 2px; }
.toast-close { background: none; border: none; color: #ccc; font-size: 18px; cursor: pointer; }

/* ===== 通用工具 ===== */
.no-data {
  text-align: center;
  color: #999;
  padding: 20px 0;
  font-size: 14px;
}

/* ===== 响应式 ===== */
@media (max-width: 380px) {
  .grid-row-4col {
    gap: 12px 4px;
  }
  .grid-icon {
    width: 38px;
    height: 38px;
    font-size: 17px;
  }
  .grid-label {
    font-size: 10px;
  }
  .elder-a11y-grid-icon {
    width: 52px;
    height: 52px;
    font-size: 26px;
  }
  .elder-a11y-grid-label,
  .elder-a11y-hot-name {
    font-size: 16px;
  }
  .elder-a11y-title,
  .elder-a11y-section-title {
    font-size: 19px;
  }
  .health-cards {
    grid-template-columns: 1fr 1fr;
    gap: 6px;
  }
  .steps-content {
    grid-template-columns: 1fr 1fr 1fr;
  }
  .sos-big-btn {
    width: 100px;
    height: 100px;
  }
  .sos-icon {
    font-size: 34px;
  }
  .sos-text {
    font-size: 15px;
  }
  .ai-chat-window {
    width: 280px;
  }
}

@media (max-width: 480px) {
  .header-right {
    gap: 6px;
  }
  .ai-assistant {
    width: 36px;
    height: 36px;
  }
  .avatar-mini {
    width: 36px;
    height: 36px;
  }
  .modal-card {
    width: 90%;
  }
  .vitals-modal, .service-modal, .profile-modal, .password-modal,
  .address-modal, .related-modal, .volunteer-modal, .butler-modal,
  .orders-modal, .member-modal {
    width: 90%;
  }
  .form-row {
    flex-direction: column;
    align-items: stretch;
  }
  .form-label {
    min-width: auto;
  }
  .stepper, .bp-input, .wide-input, .wide-select {
    width: 100%;
  }
  .member-stats {
    grid-template-columns: 1fr;
  }
}

.portal-list-overlay {
  z-index: 2050;
}

.portal-list-modal {
  width: 92%;
  max-width: 420px;
  max-height: 88vh;
  display: flex;
  flex-direction: column;
  padding: 20px 18px 16px;
  overflow: hidden;
}

.portal-list-modal .modal-top {
  margin-bottom: 8px;
}

.portal-list-modal .modal-title {
  font-size: 20px;
  font-weight: 800;
  color: #0d3b66;
}

.portal-list-modal-hint {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
  color: #546e7a;
  text-align: center;
}

.portal-list-modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 0 2px 8px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 120px;
  max-height: 55vh;
}

.portal-list-modal-foot {
  margin-top: 12px;
}

.portal-list-modal-foot .btn-confirm {
  width: 100%;
  padding: 14px;
  font-size: 17px;
  font-weight: 700;
}

.portal-card {
  display: flex;
  gap: 12px;
  padding: 10px;
  border: 1px solid #f0f2f5;
  border-radius: 12px;
  cursor: pointer;
  background: #fff;
  width: 100%;
  text-align: left;
  font-family: inherit;
}

.elder-a11y-portal-card {
  min-height: 80px;
  padding: 14px 12px;
  border: 2px solid #e3edf5;
  border-radius: 14px;
  background: #fafcff;
  transition: border-color 0.15s, background 0.15s;
}

.elder-a11y-portal-card:active {
  background: #e8f4fd;
  border-color: #90caf9;
}

.elder-a11y-portal-card:focus-visible {
  outline: 3px solid #1976d2;
  outline-offset: 2px;
}

.portal-card-thumb {
  width: 72px;
  height: 72px;
  border-radius: 12px;
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
  font-size: 32px;
}

.portal-card-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.elder-a11y-portal-title {
  font-size: 17px;
  font-weight: 800;
  color: #1a3347;
  line-height: 1.35;
}

.elder-a11y-portal-sub {
  font-size: 14px;
  font-weight: 600;
  color: #607d8b;
  margin-top: 6px;
}

.elder-a11y-portal-empty {
  font-size: 16px;
  font-weight: 600;
  padding: 24px 12px;
}
.bmi-readonly {
  width: 180px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  padding: 8px 12px;
  text-align: center;
  background: #f8f9fa;
  font-weight: 500;
}
.family-modal { width: 92%; max-width: 420px; max-height: 90vh; overflow: auto; }
.family-tabs { display: grid; grid-template-columns: 1fr 1fr 1fr; border-bottom: 1px solid #f0f2f5; }
.tab { border: 0; background: transparent; padding: 12px; font-weight: 600; color: #999; cursor: pointer; border-bottom: 2px solid transparent; }
.tab.active { color: #2196F3; border-bottom-color: #2196F3; }
.family-body { padding: 12px 16px 16px; }
.chat-wrap { display: flex; flex-direction: column; gap: 12px; min-height: 280px; }
.chat-list { flex: 1; overflow: auto; max-height: 240px; }
.chat-item { margin-bottom: 10px; display: flex; flex-direction: column; align-items: flex-start; }
.chat-item.mine { align-items: flex-end; }
.chat-bubble { max-width: 90%; background: #f5f7fa; border-radius: 12px; padding: 10px 12px; font-size: 13px; }
.chat-item.mine .chat-bubble { background: #E3F2FD; }
.chat-meta { font-size: 11px; color: #999; margin-top: 4px; }
.chat-input-row { display: grid; grid-template-columns: 1fr 72px; gap: 8px; }
.chat-input { border: 1px solid #e8ecf0; border-radius: 12px; padding: 10px; font-size: 14px; }
.chat-send { border: 0; border-radius: 12px; background: #2196F3; color: white; font-weight: 600; cursor: pointer; }
.album-wrap { padding: 4px 0; }
.album-tools { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; margin-bottom: 10px; }
.album-desc { font-size: 12px; color: #999; }
.album-empty { text-align: center; color: #999; padding: 16px; }
.album-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
.album-photo { width: 100%; height: 100px; object-fit: cover; border-radius: 8px 8px 0 0; cursor: pointer; }
.album-item { border: 1px solid #f0f2f5; border-radius: 10px; overflow: hidden; }
.album-item-name, .album-item-time { padding: 4px 8px; font-size: 11px; }
.album-del-btn { margin: 6px 8px 8px; width: calc(100% - 16px); border: 1px solid #FFCDD2; background: #FFEBEE; color: #F44336; border-radius: 8px; padding: 6px; cursor: pointer; font-size: 12px; }
.video-wrap { padding: 4px 0; }
.family-video-stage.visit-stage {
  position: relative;
  border-radius: 14px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: linear-gradient(120deg, #f4f7fb, #eef5ff);
  padding: 14px;
  min-height: 240px;
  overflow: hidden;
}
.family-video-stage.visit-stage.connected {
  border-color: rgba(67, 160, 71, 0.35);
}
.family-video-stage .video-main {
  position: relative;
  height: 170px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.family-video-stage.connected .video-main {
  height: 220px;
  min-height: 220px;
  padding: 0;
}
.family-video-stage .video-remote-stream {
  width: 100%;
  max-height: 240px;
  object-fit: cover;
  background: #111;
  border-radius: 8px;
}
.family-video-stage.connected .video-remote-stream {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  max-height: none;
  border-radius: 0;
  z-index: 1;
}
.family-video-stage .video-remote-stream.hidden {
  display: none;
}
.family-video-stage .video-local-stream {
  position: absolute;
  right: 10px;
  bottom: 10px;
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: 10px;
  border: 2px solid rgba(255, 255, 255, 0.85);
  background: #111;
  z-index: 2;
}
.family-video-stage .video-local-stream.off {
  opacity: 0.35;
  filter: grayscale(1);
}
.family-video-stage .video-title {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.5);
  font-weight: 800;
}
.family-video-stage.connected .video-title {
  position: absolute;
  top: 8px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 3;
  color: rgba(255, 255, 255, 0.92);
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.6);
}
.family-video-stage .video-avatar {
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
.family-video-stage .video-status {
  margin-top: 10px;
  color: rgba(0, 0, 0, 0.65);
  font-weight: 900;
  font-size: 12px;
}
.family-video-stage.connected .video-status {
  position: absolute;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 3;
  margin-top: 0;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.6);
}
.video-call-actions { display: flex; gap: 8px; margin-bottom: 10px; }
.video-action { border: 0; border-radius: 10px; background: #2196F3; color: #fff; padding: 8px 12px; font-weight: 600; cursor: pointer; font-size: 13px; }
.video-action.ghost { background: #f5f5f5; color: #666; border: 1px solid #e8ecf0; }
.video-action:disabled, .video-ctl:disabled { opacity: 0.5; cursor: not-allowed; }
.video-call-card { border-radius: 12px; border: 1px solid #f0f2f5; background: linear-gradient(120deg, #f4f7fb, #eef5ff); min-height: 160px; padding: 12px; position: relative; }
.video-call-card.connected { border-color: #81C784; }
.video-title { text-align: center; font-size: 12px; color: #999; }
.video-avatar { width: 56px; height: 56px; border-radius: 50%; background: #2196F3; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 24px; font-weight: 700; margin: 10px auto; }
.video-status { text-align: center; font-size: 13px; color: #666; margin-top: 8px; }
.video-local { position: absolute; right: 8px; bottom: 8px; width: 100px; height: 60px; border-radius: 8px; background: rgba(0,0,0,0.35); color: #fff; font-size: 11px; display: flex; align-items: center; justify-content: center; }
.video-local.off { background: rgba(0,0,0,0.55); }
.video-controls { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 8px; margin-top: 10px; }
.video-ctl { border: 1px solid #e8ecf0; border-radius: 10px; background: #fff; padding: 8px 4px; font-size: 12px; cursor: pointer; }
.album-preview-img { width: 100%; max-height: 60vh; object-fit: contain; border-radius: 10px; }
.modal-warning { display: flex; gap: 12px; align-items: flex-start; }
.modal-warning-badge { width: 28px; height: 28px; border-radius: 50%; background: #FFF3E0; color: #FF9800; display: flex; align-items: center; justify-content: center; font-weight: 700; }
.modal-warning-text { font-size: 14px; color: #666; line-height: 1.5; }
.modal-body { margin-bottom: 8px; }

.incoming-call-overlay {
  position: fixed;
  inset: 0;
  z-index: 120;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}
.incoming-call-card {
  width: min(360px, 100%);
  background: #fff;
  border-radius: 18px;
  padding: 24px 20px;
  text-align: center;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
}
.incoming-call-icon {
  font-size: 42px;
  margin-bottom: 8px;
}
.incoming-call-title {
  font-size: 18px;
  font-weight: 800;
  color: #1a3347;
}
.incoming-call-sub {
  margin-top: 6px;
  font-size: 13px;
  color: #888;
}
.incoming-call-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 18px;
}
.incoming-btn {
  border: none;
  border-radius: 12px;
  padding: 12px;
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;
}
.incoming-btn.decline {
  background: #f5f5f5;
  color: #666;
}
.incoming-btn.accept {
  background: linear-gradient(135deg, #1976d2, #42a5f5);
  color: #fff;
}

</style>
