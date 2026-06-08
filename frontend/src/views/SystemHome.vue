<template>
  <div class="sys-home">
    <!-- 顶部导航栏 -->
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
              <span class="brand-sub">智慧养老 · 演示入口</span>
            </div>
          </div>
          <nav class="nav-desktop">
            <ul class="nav-list">
              <li><a href="#hero" class="nav-link">首页</a></li>
              <li class="nav-dropdown">
                <a href="#services" class="nav-link">产品简介 <span class="nav-arrow">▾</span></a>
                <div class="nav-dropdown-menu">
                  <div class="nav-dropdown-inner">
                    <div class="nav-dropdown-left">
                      <ul>
                        <li><a href="#services">养老机构智慧管理系统</a></li>
                        <li><a href="#services">养老公寓智慧管理系统</a></li>
                        <li><a href="#services">社区居家养老服务系统</a></li>
                        <li><a href="#services">旅居养老服务系统</a></li>
                        <li><a href="#services">CCRC智慧管理系统</a></li>
                        <li><a href="#services">智能硬件支持</a></li>
                        <li><a href="#services">智慧养老监管平台</a></li>
                        <li><a href="#learning">产教融合解决方案</a></li>
                      </ul>
                    </div>
                    <div class="nav-dropdown-right">
                      <div class="nav-dropdown-img">
                        <span style="font-size:60px">🏥</span>
                        <p>全场景康养管理平台</p>
                      </div>
                    </div>
                  </div>
                </div>
              </li>
              <li><a href="#numbers" class="nav-link">数字智盾</a></li>
              <li class="nav-dropdown">
                <a href="#activities" class="nav-link">养老服务 <span class="nav-arrow">▾</span></a>
                <ul class="nav-dropdown-simple">
                  <li><a href="#activities">文娱活动</a></li>
                  <li><a href="#life">生活服务</a></li>
                  <li><a href="#learning">学习赋能</a></li>
                </ul>
              </li>
              <li><a href="#news" class="nav-link">新闻资讯</a></li>
              <li class="nav-dropdown">
                <a href="#admin" class="nav-link">关于我们 <span class="nav-arrow">▾</span></a>
                <ul class="nav-dropdown-simple">
                  <li><a href="#admin">平台优势</a></li>
                  <li><a href="#clients">全端覆盖</a></li>
                  <li><a href="#footer">联系我们</a></li>
                </ul>
              </li>
            </ul>
          </nav>
          <div class="topbar-actions">
            <RouterLink class="topbar-btn primary" to="/login">登录</RouterLink>
            <button class="topbar-btn outline" @click="toggleAssistant">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <circle cx="8" cy="8" r="6.5" stroke="currentColor" stroke-width="1.2"/>
                <path d="M5.5 6.5C5.5 6.5 6.5 5 8 5C9.5 5 10.5 6.5 10.5 6.5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                <path d="M6 9H10" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
              </svg>
              人工助手
            </button>
          </div>
        </div>
      </div>
    </header>

    <div v-if="homeNotice" class="home-notice-bar">
      <div class="container">
        <button type="button" class="home-notice-inner" @click="toastDemo(homeNotice)">
          <span class="home-notice-tag">公告</span>
          <span class="home-notice-text">{{ homeNotice }}</span>
        </button>
      </div>
    </div>

    <!-- 智能助手弹窗 -->
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
                <span class="assistant-badge">{{ deepSeekReady ? "DeepSeek" : "演示模式" }}</span>
              </div>
            </div>
            <button class="assistant-close" @click="showAssistant = false">
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                <path d="M4 4L14 14M14 4L4 14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
          <div ref="messagesEl" class="assistant-messages">
            <div v-for="m in chatMessages" :key="m.id" class="assistant-msg" :class="m.role">
              <div class="assistant-bubble">{{ m.text }}</div>
            </div>
          </div>
          <div class="assistant-quick">
            <button v-for="p in quickPrompts" :key="p" class="assistant-chip" @click="applyQuickPrompt(p)">{{ p }}</button>
          </div>
          <div class="assistant-compose">
            <input ref="aiInputRef" v-model="aiDraft" type="search" placeholder="输入问题，回车搜索..." @keydown.enter.prevent="sendAiMessage" />
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

    <!-- 侧边栏客服 -->
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

    <!-- 1. Hero -->
    <section id="hero" class="section section-hero">
      <div class="container">
        <div class="hero-content">
          <div class="hero-left" data-animate="fadeInLeft">
            <h1 class="hero-title">
              <span class="hero-title-line">
                <span class="text-reveal" v-for="(char, i) in heroTitleChars" :key="i" :style="{ animationDelay: i * 0.05 + 's' }">{{ char }}</span>
              </span>
              <span class="hero-title-sub" data-animate="fadeInUp" data-delay="0.6">多角色统一入口，家属与机构协同守护</span>
            </h1>
            <p class="hero-desc" data-animate="fadeInUp" data-delay="0.8">银发智盾智慧养老管理系统，集成 AI 智能助手、多端协同、实时监测预警，为养老机构提供全场景数字化解决方案。</p>
            <div class="hero-actions" data-animate="fadeInUp" data-delay="1">
              <RouterLink class="hero-btn primary pulse-animation" to="/login">
                一键进入系统
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M6 4L10 8L6 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
              </RouterLink>
              <RouterLink class="hero-btn outline" to="/register">新用户注册</RouterLink>
            </div>
          </div>
        </div>
      </div>
      <div class="scroll-indicator">
        <div class="scroll-mouse"><div class="scroll-wheel"></div></div>
        <span>向下滚动</span>
      </div>
    </section>

    <!-- 2. 数字展示 -->
    <section id="numbers" class="section section-numbers">
      <div class="container">
        <div class="section-title" data-animate="fadeInUp"><h2>数字银发智盾</h2><p>{{ statsLabel }}</p></div>
        <div class="numbers-grid">
          <div class="number-item" v-for="(n, i) in numbers" :key="n.label" :data-animate="'fadeInUp'" :data-delay="i * 0.15">
            <div class="number-value">
              <span v-if="n.isCount" class="count-up" :data-target="n.countTarget">0</span>
              <span v-else>{{ n.value }}</span>
              <span class="number-unit">{{ n.unit }}</span>
            </div>
            <p class="number-label">{{ n.label }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 3. 服务宫格 -->
    <section id="services" class="section section-services">
      <div class="container">
        <div class="section-title" data-animate="fadeInUp"><h2>全角色覆盖</h2><p>支持长者、家属、服务机构、管理员多角色协同，一套系统统一管理</p></div>
        <div class="services-grid">
          <RouterLink v-for="(g, i) in gridPortals" :key="g.role" class="service-card hover-lift" :class="g.tone" :to="{ path: '/login', query: { role: g.role } }" :data-animate="'fadeInUp'" :data-delay="i * 0.15">
            <div class="service-icon icon-bounce">{{ g.icon }}</div>
            <div class="service-info"><h3>{{ g.title }}</h3><p>{{ g.sub }}</p></div>
            <span class="service-arrow">→</span>
          </RouterLink>
        </div>
      </div>
    </section>

    <!-- 4. 全客户端 -->
    <section id="clients" class="section section-clients">
      <div class="container">
        <div class="section-title" data-animate="fadeInUp"><h2>全客户端适配</h2><p>一套设计风格，统一操作体验；数据信息共享，业务高效运转</p></div>
        <div class="clients-row">
          <div class="client-item" v-for="(c, i) in clients" :key="c.name" :data-animate="'zoomIn'" :data-delay="i * 0.2">
            <div class="client-icon icon-rotate">{{ c.icon }}</div><h3>{{ c.name }}</h3>
          </div>
        </div>
      </div>
    </section>

    <!-- 5. 文娱活动 -->
    <section id="activities" class="section section-activities">
      <div class="container">
        <div class="section-title" data-animate="fadeInUp"><h2>文娱活动</h2><p>丰富多彩的文化娱乐活动，让晚年生活充满活力与欢乐</p></div>
        <div v-if="portalActivities.length" class="activities-grid">
          <div class="activity-card hover-lift" v-for="(a, i) in portalActivities" :key="a.id" :data-animate="'fadeInUp'" :data-delay="i * 0.1">
            <div class="activity-img"><span class="activity-emoji">{{ a.icon }}</span><span class="activity-tag">{{ a.tag }}</span></div>
            <div class="activity-info">
              <h3>{{ a.title }}</h3>
              <div class="activity-meta"><span>🕐 {{ a.timeLabel }}</span><span>📍 {{ a.location }}</span></div>
              <p>{{ a.description }}</p>
              <button type="button" class="activity-join" @click="openPortalActivity(a)">立即报名</button>
            </div>
          </div>
        </div>
        <p v-else class="section-empty">暂无活动数据，请确认后端已启动或在管理端发布门户内容。</p>
      </div>
    </section>

    <!-- 6. 生活服务 -->
    <section id="life" class="section section-life">
      <div class="container">
        <div class="section-title" data-animate="fadeInUp"><h2>生活服务</h2><p>贴心生活照料，让日常琐事变得简单便捷</p></div>
        <div v-if="portalLifeServices.length" class="life-grid">
          <div class="life-card hover-lift" v-for="(s, i) in portalLifeServices" :key="s.id" :data-animate="'fadeInUp'" :data-delay="i * 0.1">
            <div class="life-icon-wrap" :style="{ background: s.bgGradient }"><span class="life-icon">{{ s.icon }}</span></div>
            <h3>{{ s.title }}</h3>
            <p>{{ s.description }}</p>
            <div class="life-features"><span v-for="f in s.features" :key="f">{{ f }}</span></div>
            <button type="button" class="life-btn" @click="openPortalLife(s)">立即预约</button>
          </div>
        </div>
        <p v-else class="section-empty">暂无生活服务数据。</p>
      </div>
    </section>

    <!-- 7. 学习赋能 -->
    <section id="learning" class="section section-learning">
      <div class="container">
        <div class="section-title" data-animate="fadeInUp"><h2>学习赋能</h2><p>活到老学到老，知识技能持续提升，紧跟时代步伐</p></div>
        <div v-if="portalCourses.length" class="learning-grid">
          <div class="learning-card hover-lift" v-for="(l, i) in portalCourses" :key="l.id" :data-animate="'fadeInUp'" :data-delay="i * 0.1">
            <div class="learning-thumb"><span class="learning-emoji">{{ l.icon }}</span><div class="learning-duration">{{ l.duration }}</div></div>
            <div class="learning-info">
              <span class="learning-category">{{ l.category }}</span><h3>{{ l.title }}</h3><p>{{ l.description }}</p>
              <div class="learning-stats"><span>🕐 {{ l.viewsLabel }}</span><span>⭐ {{ l.rating }}</span></div>
              <button type="button" class="learning-btn" @click="openPortalCourse(l)">开始学习</button>
            </div>
          </div>
        </div>
        <p v-else class="section-empty">暂无课程数据。</p>
      </div>
    </section>

    <!-- 8. 新闻资讯 -->
    <section id="news" class="section section-news">
      <div class="container">
        <div class="section-title" data-animate="fadeInUp"><h2>新闻资讯</h2><p>关注养老行业动态，了解最新政策与资讯</p></div>
        <div v-if="portalNewsList.length" class="news-grid">
          <a v-for="(n, i) in portalNewsList" :key="n.id" class="news-card hover-lift" href="javascript:;" :data-animate="'fadeInUp'" :data-delay="i * 0.1" @click.prevent="openPortalNews(n)">
            <div class="news-img"><span class="news-emoji">{{ n.icon }}</span><span class="news-date">{{ n.publishDate }}</span></div>
            <div class="news-info"><h3>{{ n.title }}</h3><p>{{ n.summary }}</p><div class="news-meta"><span>{{ n.source }}</span><span class="news-arrow">查看详情 →</span></div></div>
          </a>
        </div>
        <p v-else class="section-empty">暂无新闻资讯。</p>
      </div>
    </section>

    <!-- 9. 管理端 -->
    <section id="admin" class="section section-admin">
      <div class="container">
        <div class="section-title" data-animate="fadeInUp"><h2>平台优势</h2><p>从营销到服务，从管理到运营，全面助力养老机构信息化建设</p></div>
        <div class="admin-grid">
          <div class="admin-card hover-slide" v-for="(m, i) in adminModules" :key="m.title" :data-animate="'fadeInUp'" :data-delay="i * 0.1">
            <span class="admin-num">{{ String(i + 1).padStart(2, '0') }}</span>
            <div class="admin-info"><h3>{{ m.title }}</h3><p>{{ m.desc }}</p></div>
          </div>
        </div>
        <div class="admin-bottom" data-animate="fadeInUp">
          <RouterLink class="hero-btn primary" :to="{ path: '/login', query: { role: 'ADMIN' } }">以管理员身份登录</RouterLink>
        </div>
      </div>
    </section>

    <!-- 10. 页脚 -->
    <footer id="footer" class="section section-footer">
      <div class="container">
        <div class="footer-grid">
          <div class="footer-col">
            <div class="footer-logo">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none"><path d="M12 2L4 7V17L12 22L20 17V7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/></svg>
              <div><h3>银发智盾</h3><p>智慧养老管理系统</p></div>
            </div>
            <p class="footer-tel">演示系统 · 已建档长者 {{ homeSummary?.elderCount ?? "—" }} 人 · 接入设备 {{ homeSummary?.deviceCount ?? "—" }} 台 · 智盾核心 v2.4</p>
          </div>
          <div class="footer-col"><h4>产品简介</h4><a href="#services">机构养老系统</a><a href="#services">公寓养老系统</a><a href="#services">社区居家系统</a></div>
          <div class="footer-col"><h4>养老服务</h4><a href="#activities">文娱活动</a><a href="#life">生活服务</a><a href="#learning">学习赋能</a></div>
          <div class="footer-col"><h4>关于我们</h4><span>数据为内存模拟</span><span>演示环境</span></div>
        </div>
        <div class="footer-bottom">
          <p>© 2024 银发智慧养老系统（演示）</p>
          <div class="footer-links"><RouterLink to="/">首页</RouterLink><span>·</span><RouterLink to="/login">登录</RouterLink></div>
        </div>
      </div>
    </footer>

    <PortalHomeModals ref="portalModals" @refresh="loadPortalContent" @toast="portalToast" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from "vue";
import { deepSeekChat, type ChatTurn } from "../api/deepseek";
import { fetchHomeSummary, type PublicHomeSummary } from "../api/home";
import {
  fetchActivities,
  fetchCourses,
  fetchLifeServices,
  fetchNews,
  type PortalActivity,
  type PortalCourse,
  type PortalLifeService,
  type PortalNews
} from "../api/portal";
import PortalHomeModals from "../components/portal/PortalHomeModals.vue";
import { resolveMediaUrl } from "../utils/mediaUrl";

const isScrolled = ref(false);
const showAssistant = ref(false);
const showBackTop = ref(false);
const aiBusy = ref(false);
const aiDraft = ref("");
const messagesEl = ref<HTMLElement | null>(null);
const aiInputRef = ref<HTMLInputElement | null>(null);
const homeSummary = ref<PublicHomeSummary | null>(null);
const homeNotice = ref("");
const deepSeekReady = ref(false);
const portalModals = ref<InstanceType<typeof PortalHomeModals> | null>(null);
const portalActivities = ref<PortalActivity[]>([]);
const portalLifeServices = ref<PortalLifeService[]>([]);
const portalCourses = ref<PortalCourse[]>([]);
const portalNewsList = ref<PortalNews[]>([]);

let msgSeq = 0;
let observer: IntersectionObserver | null = null;
let countObserver: IntersectionObserver | null = null;

const heroTitleChars = "监测 · 预警 · 服务 · 沟通".split("");

interface ChatMessage { id: string; role: "user" | "assistant"; text: string; }

const welcomeText = computed(() =>
  deepSeekReady.value
    ? "您好，我是银发智盾智慧助手（已接入 DeepSeek）。在下方输入问题即可获得解答，也可点击下方快捷问题。"
    : "您好，我是银发智盾智慧助手（当前为演示模式，未配置 DeepSeek 时将使用本地关键词指引）。在下方输入问题即可。"
);

const chatMessages = ref<ChatMessage[]>([]);

watch(
  welcomeText,
  (text) => {
    const w = chatMessages.value.find((m) => m.id === "welcome");
    if (w) w.text = text;
    else chatMessages.value.unshift({ id: "welcome", role: "assistant", text });
  },
  { immediate: true }
);

const quickPrompts = ["如何登录？", "家属端能做什么？", "服务机构工单入口", "管理端报表在哪"];

type NumberItem = {
  value: string;
  unit: string;
  label: string;
  isCount: boolean;
  countTarget: number;
};

const numbers = ref<NumberItem[]>([
  { value: "4", unit: "大角色", label: "长者·家属·机构·管理", isCount: false, countTarget: 0 },
  { value: "6", unit: "大模块", label: "管理端核心能力", isCount: false, countTarget: 0 },
  { value: "", unit: "%", label: "DeepSeek 智能助手", isCount: true, countTarget: 99.9 },
  { value: "", unit: "%", label: "手机·平板·电脑·大屏", isCount: true, countTarget: 100 }
]);

const statsLabel = computed(() => {
  const s = homeSummary.value;
  if (!s) return "立足养老行业，打造标准、易用、便捷的智慧康养综合管理平台";
  return `平台已接入 ${s.elderCount} 位长者档案、${s.deviceCount} 台设备；今日告警 ${s.todayAlarms} 条、本月工单 ${s.monthlyTasks} 单（演示汇总）`;
});

const gridPortals = [
  { role: "ELDER" as const, title: "长者端", sub: "体征监测 · 一键呼救", icon: "👴", tone: "tone-elder" },
  { role: "CHILD" as const, title: "家属端", sub: "告警接收 · 位置追踪", icon: "👨‍👩‍👧", tone: "tone-child" },
  { role: "AGENCY" as const, title: "服务机构", sub: "派单管理 · 工单流转", icon: "🏥", tone: "tone-agency" },
  { role: "ADMIN" as const, title: "管理端", sub: "数据大屏 · 报表分析", icon: "🛡️", tone: "tone-admin" }
];

const clients = [
  { icon: "💻", name: "电脑端" }, { icon: "📱", name: "手机端" },
  { icon: "📋", name: "平板端" }, { icon: "🖥️", name: "大屏端" }
];

const adminModules = [
  { title: "数据大屏 / 仪表盘", desc: "长者规模、今日告警、工单与设备运营总览" },
  { title: "健康监测 & 预警", desc: "多长者体征表、跌倒/围栏类告警、规则说明" },
  { title: "服务派单管理", desc: "预约建单、状态流转、一键派单建议" },
  { title: "亲情互动看板", desc: "相册、即时通讯、用药与生日关怀时间线" },
  { title: "数据管理 / 报表", desc: "健康趋势、满意度统计、用户与权限管理" },
  { title: "外呼升级", desc: "告警外呼链路中的待处理呼叫列表" }
];

function toggleAssistant() {
  showAssistant.value = !showAssistant.value;
}

function toastDemo(msg: string) { alert(msg); }
function portalToast(msg: string) { toastDemo(msg); }
function openPortalActivity(a: PortalActivity) { portalModals.value?.openActivity(a); }
function openPortalLife(s: PortalLifeService) { portalModals.value?.openLife(s); }
function openPortalCourse(c: PortalCourse) { portalModals.value?.openCourse(c); }
function openPortalNews(n: PortalNews) { portalModals.value?.openNews(n); }

function nextMsgId() { msgSeq += 1; return `m-${msgSeq}`; }

function messagesToTurns(msgs: ChatMessage[]): ChatTurn[] {
  return msgs.filter(m => m.id !== "welcome").map(m => ({ role: m.role, text: m.text }));
}

function smartReply(userText: string): string {
  const t = userText.trim();
  if (!t) return "请简单描述您的问题，例如「怎么登录」或「高血压怎么办」。";
  if (/帮助|你能|会什么|怎么用|功能/.test(t)) {
    return "我可以帮您说明各端入口、登录注册与体征/告警/派单等模块；也可点击下方快捷问题。";
  }
  if (/登录|账号|密码|进系统/.test(t)) {
    return "登录：首页点「一键进入系统」或顶部「登录」，使用注册时的姓名与密码（E/C/G/A 前缀演示账号）。";
  }
  if (/注册|新用户|开户/.test(t)) return "新用户：顶部或 Hero 区「新用户注册」，注册后会分配系统 ID 供家属或机构绑定。";
  if (/长者|老人|elder|sos|体征/.test(t)) return "长者端：宫格「长者端」登录，可查看体征、SOS 等。";
  if (/家属|子女|child|告警/.test(t)) return "家属端：宫格「家属端」登录，可查看健康动态、告警与远程探视等。";
  if (/机构|派单|工单|agency/.test(t)) return "服务机构：选择「服务机构」登录，可进行派单与工单管理。";
  if (/管理|admin|报表|大屏/.test(t)) return "管理端：选择「管理端」登录，可查看数据大屏与报表。";
  if (/健康|心率|血压|血氧|高血压/.test(t)) return "健康与预警：长者体征由监测模块展示；家属端可接收告警。具体以各端菜单为准。";
  if (/你好|您好|hi\b|hello/i.test(t)) return "您好！需要找哪个入口？可以说「登录」「家属端」「工单」等。";
  return `关于「${t}」：当前为演示环境。若 DeepSeek 未返回，请检查 backend/local-deepseek.yml 并重启 8081。`;
}

async function scrollChatToEnd() {
  await nextTick();
  const el = messagesEl.value;
  if (el) el.scrollTop = el.scrollHeight;
}

async function sendAiMessage() {
  const text = aiDraft.value.trim();
  if (!text || aiBusy.value) return;
  chatMessages.value.push({ id: nextMsgId(), role: "user", text });
  aiDraft.value = "";
  aiBusy.value = true;
  await scrollChatToEnd();

  let reply: string;
  try {
    reply = await deepSeekChat(messagesToTurns(chatMessages.value));
  } catch (e) {
    if (import.meta.env.DEV) console.warn("[DeepSeek]", e);
    const errText = e instanceof Error ? e.message.trim() : "";
    if (errText) {
      const hintNav = /未配置|密钥|DEEPSEEK|401|Unauthorized|503|余额不足|无法连接后端/i.test(errText);
      reply = hintNav ? `${errText}\n\n${smartReply(text)}` : errText;
    } else {
      reply = smartReply(text);
    }
  }

  chatMessages.value.push({ id: nextMsgId(), role: "assistant", text: reply });
  aiBusy.value = false;
  await scrollChatToEnd();
}

async function applyQuickPrompt(p: string) { aiDraft.value = p; await sendAiMessage(); }
function scrollToTop() { window.scrollTo({ top: 0, behavior: "smooth" }); }

function onKeydown(e: KeyboardEvent) {
  if (e.key === "Escape") showAssistant.value = false;
}

async function loadHomeSummary() {
  try {
    const data = await fetchHomeSummary();
    homeSummary.value = data;
    homeNotice.value = data.notice?.trim() || "";
    deepSeekReady.value = !!data.deepSeekConfigured;
    numbers.value = [
      { value: String(data.roleCount), unit: "大角色", label: "长者·家属·机构·管理", isCount: false, countTarget: 0 },
      { value: String(data.moduleCount), unit: "大模块", label: "管理端核心能力", isCount: false, countTarget: 0 },
      { value: "", unit: "%", label: "DeepSeek 智能助手", isCount: true, countTarget: 99.9 },
      { value: "", unit: "%", label: "手机·平板·电脑·大屏", isCount: true, countTarget: 100 }
    ];
    await nextTick();
    initCountUp();
  } catch (e) {
    if (import.meta.env.DEV) console.warn("[home/summary]", e);
  }
}

async function loadPortalContent() {
  try {
    const [acts, life, courses, news] = await Promise.all([
      fetchActivities(),
      fetchLifeServices(),
      fetchCourses(),
      fetchNews()
    ]);
    portalActivities.value = acts;
    portalLifeServices.value = life;
    portalCourses.value = courses;
    portalNewsList.value = news;
  } catch (e) {
    if (import.meta.env.DEV) console.warn("[portal]", e);
  }
}

watch(showAssistant, (open) => {
  document.body.style.overflow = open ? "hidden" : "";
});

// ========== 滚动动画系统 ==========
function initScrollAnimations() {
  const els = document.querySelectorAll(".sys-home [data-animate]");
  observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const el = entry.target as HTMLElement;
        const delay = el.dataset.delay || '0';
        el.style.animationDelay = `${delay}s`;
        el.classList.add('animated', el.dataset.animate || 'fadeInUp');
        observer?.unobserve(el);
      }
    });
  }, { threshold: 0.15, rootMargin: '0px 0px -50px 0px' });
  els.forEach(el => observer?.observe(el));
}

function initCountUp() {
  countObserver?.disconnect();
  countObserver = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          const el = entry.target as HTMLElement;
          const target = parseFloat(el.dataset.target || "0");
          const dur = 2000;
          const st = performance.now();
          const isDec = target % 1 !== 0;
          function upd(t: number) {
            const p = Math.min((t - st) / dur, 1);
            const eased = 1 - Math.pow(1 - p, 3);
            const c = target * eased;
            el.textContent = isDec ? c.toFixed(1) : Math.floor(c).toString();
            if (p < 1) requestAnimationFrame(upd);
            else el.textContent = isDec ? target.toFixed(1) : target.toString();
          }
          requestAnimationFrame(upd);
          countObserver?.unobserve(el);
        }
      });
    },
    { threshold: 0.5 }
  );
  document.querySelectorAll(".sys-home .count-up").forEach((el) => countObserver?.observe(el));
}

// ========== 视差滚动 ==========
function handleParallax() {
  const hero = document.querySelector('.section-hero') as HTMLElement;
  if (hero) hero.style.backgroundPosition = `center ${window.scrollY * 0.35}px`;
}

function handleScroll() {
  isScrolled.value = window.scrollY > 50;
  showBackTop.value = window.scrollY > 500;
  handleParallax();
}

onMounted(() => {
  loadHomeSummary();
  loadPortalContent();
  initScrollAnimations();
  initCountUp();
  window.addEventListener("scroll", handleScroll, { passive: true });
  window.addEventListener("keydown", onKeydown);
  handleScroll();
});

onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll);
  window.removeEventListener("keydown", onKeydown);
  observer?.disconnect();
  countObserver?.disconnect();
  document.body.style.overflow = "";
});
</script>

<style scoped>
* { margin: 0; padding: 0; box-sizing: border-box; }
.sys-home { font-family: 'Inter', 'Noto Sans SC', -apple-system, sans-serif; color: #1a3347; background: #fff; -webkit-font-smoothing: antialiased; overflow-x: hidden; }
.container { max-width: 1200px; margin: 0 auto; padding: 0 clamp(16px, 4vw, 40px); }
.section { padding: clamp(48px, 8vw, 100px) 0; }
.section-title { text-align: center; margin-bottom: clamp(32px, 5vw, 56px); }
.section-title h2 { font-size: clamp(24px, 4vw, 36px); font-weight: 700; margin-bottom: 12px; }
.section-title p { font-size: clamp(14px, 1.8vw, 17px); color: #7b95a8; max-width: 600px; margin: 0 auto; }

/* ========== 动画关键帧 ========== */
@keyframes fadeInUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }
@keyframes fadeInLeft { from { opacity: 0; transform: translateX(-40px); } to { opacity: 1; transform: translateX(0); } }
@keyframes fadeInRight { from { opacity: 0; transform: translateX(40px); } to { opacity: 1; transform: translateX(0); } }
@keyframes zoomIn { from { opacity: 0; transform: scale(0.8); } to { opacity: 1; transform: scale(1); } }
@keyframes textReveal { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
@keyframes float { 0%,100%{transform:translateY(0)}50%{transform:translateY(-12px)} }
@keyframes pulse { 0%,100%{box-shadow:0 0 0 0 rgba(31,106,165,.4)}50%{box-shadow:0 0 0 15px rgba(31,106,165,0)} }
@keyframes heroBtnPulse {
  0%, 100% {
    box-shadow: 0 8px 24px rgba(31,106,165,.3), 0 0 0 0 rgba(31,106,165,.45);
  }
  50% {
    box-shadow: 0 8px 24px rgba(31,106,165,.35), 0 0 0 14px rgba(31,106,165,0);
  }
}
@keyframes heroBtnGhost {
  0%, 100% { transform: translate(0, 0); opacity: 0; }
  50% { transform: translate(-4px, -3px); opacity: 0.45; }
}
@keyframes spin { to{transform:rotate(360deg)} }
@keyframes scrollWheel { 0%{opacity:1;transform:translateY(0)}100%{opacity:0;transform:translateY(8px)} }
@keyframes iconBounce { 0%,100%{transform:translateY(0)}50%{transform:translateY(-6px)} }
@keyframes iconRotate { 0%{transform:rotate(0deg)}100%{transform:rotate(360deg)} }
@keyframes shimmer { 0%{background-position:-200% 0}100%{background-position:200% 0} }

/* 动画应用类 */
.animated { animation-duration: 0.8s; animation-fill-mode: both; animation-timing-function: cubic-bezier(0.25, 0.46, 0.45, 0.94); }
.animated.fadeInUp { animation-name: fadeInUp; }
.animated.fadeInLeft { animation-name: fadeInLeft; }
.animated.fadeInRight { animation-name: fadeInRight; }
.animated.zoomIn { animation-name: zoomIn; }

.text-reveal { display: inline-block; opacity: 0; animation: textReveal 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards; }

.floating-card { animation: float 6s ease-in-out infinite; }
.pulse-animation { animation: pulse 2.5s infinite; }

/* 悬停动画类 */
.hover-lift { transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94); }
.hover-lift:hover { transform: translateY(-6px); box-shadow: 0 16px 40px rgba(0,0,0,0.08); }
.hover-slide { transition: all 0.3s; }
.hover-slide:hover { transform: translateX(4px); box-shadow: 0 8px 24px rgba(0,0,0,0.05); }
.icon-bounce:hover { animation: iconBounce 0.6s ease; }
.icon-rotate { display: inline-block; transition: transform 0.6s; }
.client-item:hover .icon-rotate { animation: iconRotate 0.6s ease; }

/* 导航栏 */
.app-topbar { position:fixed;top:0;left:0;right:0;z-index:1000;padding:14px 0;background:#fff;border-bottom:1px solid rgba(0,0,0,.04);transition:all .3s; }
.app-topbar.topbar-scrolled { background:rgba(255,255,255,.98);backdrop-filter:blur(20px);box-shadow:0 4px 20px rgba(0,0,0,.06);padding:10px 0; }
.topbar-inner { display:flex;align-items:center;justify-content:space-between; }
.topbar-brand { display:flex;align-items:center;gap:10px; }
.brand-mark { width:42px;height:42px;border-radius:12px;background:linear-gradient(135deg,#1f6aa5,#174d7a);display:flex;align-items:center;justify-content:center;color:#fff;transition:transform .3s; }
.brand-mark:hover { transform: scale(1.05); }
.brand-name { font-size:18px;font-weight:700; }
.brand-sub { font-size:10px;color:#94a3b8;letter-spacing:2px;display:none; }
.nav-desktop { margin:0 24px; }
.nav-list { display:flex;align-items:center;gap:2px;list-style:none; }
.nav-link { padding:8px 14px;border-radius:6px;font-size:13px;font-weight:500;color:#475569;text-decoration:none;transition:all .2s;white-space:nowrap;display:flex;align-items:center;gap:4px; }
.nav-link:hover { background:#f1f5f9;color:#1f6aa5; }
.nav-arrow { font-size:9px;opacity:.5; }
.nav-dropdown { position:relative; }
.nav-dropdown-menu { position:absolute;top:100%;left:50%;transform:translateX(-50%);padding-top:8px;opacity:0;visibility:hidden;transition:all .3s;pointer-events:none; }
.nav-dropdown:hover .nav-dropdown-menu { opacity:1;visibility:visible;pointer-events:auto; }
.nav-dropdown-inner { display:flex;background:#fff;border-radius:16px;box-shadow:0 20px 50px rgba(0,0,0,.12);overflow:hidden;min-width:520px; }
.nav-dropdown-left { flex:1;padding:12px; }
.nav-dropdown-left ul { list-style:none;display:grid;grid-template-columns:1fr 1fr;gap:2px; }
.nav-dropdown-left a { display:block;padding:10px 14px;border-radius:8px;font-size:13px;color:#475569;text-decoration:none;transition:all .2s; }
.nav-dropdown-left a:hover { background:#f1f5f9;color:#1f6aa5; }
.nav-dropdown-right { width:200px;background:linear-gradient(135deg,#f8fafc,#eff6ff);display:flex;align-items:center;justify-content:center;padding:20px;text-align:center; }
.nav-dropdown-img p { font-size:12px;color:#7b95a8;margin-top:8px; }
.nav-dropdown-simple { position:absolute;top:100%;left:0;padding-top:6px;opacity:0;visibility:hidden;transition:all .3s;list-style:none;min-width:160px;pointer-events:none; }
.nav-dropdown:hover .nav-dropdown-simple { opacity:1;visibility:visible;pointer-events:auto; }
.nav-dropdown-simple a { display:block;padding:10px 16px;font-size:13px;color:#475569;text-decoration:none;background:#fff;border-bottom:1px solid #f1f5f9;transition:all .2s; }
.nav-dropdown-simple a:hover { background:#f8fafc;color:#1f6aa5; }
.topbar-actions { display:flex;gap:10px;align-items:center; }
.topbar-btn { display:inline-flex;align-items:center;gap:6px;padding:9px 18px;border-radius:8px;font-size:13px;font-weight:500;cursor:pointer;border:none;font-family:inherit;transition:all .3s;white-space:nowrap;text-decoration:none; }
.topbar-btn.primary { background:#fff;color:#1a3347;border:1px solid #e2e8f0; }
.topbar-btn.primary:hover { transform:translateY(-2px);box-shadow:0 6px 20px rgba(0,0,0,.1); }
.topbar-btn.outline { background:#1f6aa5;color:#fff;border:1px solid #1f6aa5; }
.topbar-btn.outline:hover { background:#174d7a;transform:translateY(-2px);box-shadow:0 8px 24px rgba(31,106,165,.3); }

/* 侧边栏 */
.side-service { position:fixed;right:16px;bottom:50%;transform:translateY(50%);z-index:999; }
.side-service ul { list-style:none;display:flex;flex-direction:column;gap:2px; }
.side-service-item { width:44px;height:44px;display:flex;align-items:center;justify-content:center;border-radius:10px;background:#fff;border:1px solid #e2e8f0;color:#64748b;cursor:pointer;transition:all .3s;position:relative;text-decoration:none; }
.side-service-item:hover { background:#1f6aa5;color:#fff;border-color:#1f6aa5;box-shadow:0 8px 24px rgba(31,106,165,.25); }
.side-service-popup { position:absolute;right:56px;top:50%;transform:translateY(-50%);background:#1a2332;color:#fff;font-size:11px;padding:6px 12px;border-radius:6px;white-space:nowrap;opacity:0;pointer-events:none;transition:opacity .3s; }
.side-service-item:hover .side-service-popup { opacity:1; }
.side-service-item.backtop { opacity:0;visibility:hidden;transition:all .3s; }
.side-service-item.backtop.visible { opacity:1;visibility:visible; }

/* 助手弹窗 */
.assistant-overlay { position:fixed;inset:0;background:rgba(0,0,0,.4);backdrop-filter:blur(4px);z-index:2000;display:flex;align-items:center;justify-content:center;padding:20px; }
.assistant-modal { background:#fff;border-radius:20px;width:100%;max-width:460px;max-height:85vh;display:flex;flex-direction:column;box-shadow:0 30px 60px rgba(0,0,0,.2);animation:fadeInUp .3s ease; }
.assistant-modal-head { display:flex;align-items:center;justify-content:space-between;padding:18px 20px;border-bottom:1px solid #f1f5f9; }
.assistant-modal-title { display:flex;align-items:center;gap:10px; }
.assistant-modal-title h3 { font-size:16px;font-weight:700; }
.assistant-badge { font-size:9px;padding:2px 7px;border-radius:999px;background:#f5f3ff;color:#7c3aed;border:1px solid #ddd6fe; }
.ai-icon-pulse { width:36px;height:36px;border-radius:10px;background:linear-gradient(135deg,#eef2ff,#e0e7ff);display:flex;align-items:center;justify-content:center;color:#4f46e5;animation:iconBounce 2s ease-in-out infinite; }
.assistant-close { width:34px;height:34px;border-radius:8px;border:1px solid #e2e8f0;background:#fff;color:#94a3b8;cursor:pointer;display:flex;align-items:center;justify-content:center; }
.assistant-close:hover { background:#f1f5f9;color:#1a3347; }
.assistant-messages { flex:1;max-height:280px;overflow-y:auto;padding:12px 16px;display:flex;flex-direction:column;gap:8px; }
.assistant-msg { display:flex; } .assistant-msg.user { justify-content:flex-end; }
.assistant-bubble { max-width:85%;padding:8px 12px;border-radius:12px;font-size:13px;line-height:1.5;white-space:pre-wrap; }
.user .assistant-bubble { background:#1f6aa5;color:#fff;border-bottom-right-radius:4px; }
.assistant .assistant-bubble { background:#f8fafc;border:1px solid #e2e8f0;border-bottom-left-radius:4px; }
.assistant-quick { display:flex;flex-wrap:wrap;gap:6px;padding:0 16px 10px; }
.assistant-chip { font-size:11px;padding:5px 10px;border-radius:999px;border:1px solid #ddd6fe;background:#f5f3ff;color:#7c3aed;cursor:pointer;font-family:inherit;transition:all .2s; }
.assistant-chip:hover { background:#ede9fe;transform:translateY(-1px); }
.assistant-compose { display:flex;gap:8px;padding:10px 16px 16px; }
.assistant-compose input { flex:1;padding:10px 12px;border-radius:10px;border:1px solid #e2e8f0;font-size:13px;outline:none;font-family:inherit;transition:all .2s; }
.assistant-compose input:focus { border-color:#93c5fd;box-shadow:0 0 0 3px rgba(59,130,246,.06); }
.assistant-compose button { width:40px;height:40px;border-radius:10px;background:#7c3aed;border:none;color:#fff;cursor:pointer;display:flex;align-items:center;justify-content:center;flex-shrink:0;transition:all .2s; }
.assistant-compose button:hover:not(:disabled) { box-shadow:0 0 20px rgba(124,58,237,.4); }
.loading-dot { width:14px;height:14px;border-radius:50%;border:2px solid rgba(255,255,255,.3);border-top-color:#fff;animation:spin .6s linear infinite;display:block; }

/* Hero */
.section-hero {
  min-height:100vh;min-height:100dvh;display:flex;align-items:flex-start;justify-content:flex-start;position:relative;overflow:hidden;
  background-color:#0f172a;
  background-image:
    linear-gradient(120deg, rgba(15,23,42,0.72) 0%, rgba(15,23,42,0.35) 42%, transparent 68%),
    url('/images/hero-bg.png');
  background-size:cover;
  background-position:center center;
  background-repeat:no-repeat;
}
.section-hero > .container { display:flex;justify-content:flex-start;align-items:flex-start;width:100%; }
.hero-content {
  display:grid;grid-template-columns:minmax(0,1fr);gap:clamp(20px,3vw,32px);
  align-items:start;justify-items:start;text-align:left;
  padding-top:clamp(96px,14vh,128px);padding-bottom:48px;max-width:min(640px,92vw);margin:0;
}
.hero-left { background:none;border:none;box-shadow:none;padding:0; }
.hero-title-line {
  display:flex;flex-wrap:wrap;font-size:clamp(32px,5vw,52px);font-weight:800;margin-bottom:8px;
  color:#fff;text-shadow:0 2px 20px rgba(0,0,0,0.45);
}
.hero-title-sub {
  display:block;font-size:clamp(16px,2.5vw,22px);color:#bae6fd;font-weight:600;
  text-shadow:0 1px 12px rgba(0,0,0,0.4);
}
.hero-desc {
  font-size:clamp(14px,1.5vw,16px);color:rgba(255,255,255,0.92);line-height:1.75;margin:20px 0 28px;max-width:520px;
  text-shadow:0 1px 10px rgba(0,0,0,0.5);
}
.hero-actions { display:flex;gap:12px;flex-wrap:wrap; }
.hero-btn { display:inline-flex;align-items:center;gap:6px;padding:12px 24px;border-radius:10px;font-size:14px;font-weight:600;text-decoration:none;transition:all .3s;font-family:inherit;border:none;cursor:pointer; }
.hero-btn.primary { background:#1f6aa5;color:#fff;box-shadow:0 8px 24px rgba(31,106,165,.25); }
.hero-btn.primary.pulse-animation {
  position:relative;z-index:0;isolation:isolate;
  animation: heroBtnPulse 2.4s ease-in-out infinite;
}
.hero-btn.primary.pulse-animation::before {
  content:"";position:absolute;inset:0;border-radius:inherit;background:#174d7a;
  z-index:-1;pointer-events:none;
  animation: heroBtnGhost 2.4s ease-in-out infinite;
}
.hero-btn.primary.pulse-animation:hover {
  animation:none;transform:translateY(-2px);
  box-shadow:0 14px 32px rgba(31,106,165,.45);
}
.hero-btn.primary.pulse-animation:hover::before { opacity:0;animation:none; }
.hero-btn.primary:hover { background:#174d7a;transform:translateY(-2px);box-shadow:0 14px 32px rgba(31,106,165,.35); }
.hero-btn.outline { border:1px solid rgba(255,255,255,0.55);color:#fff;background:rgba(255,255,255,0.12);backdrop-filter:blur(6px); }
.hero-btn.outline:hover { border-color:#fff;background:rgba(255,255,255,0.22);color:#fff; }
.scroll-indicator {
  position:absolute;bottom:32px;left:50%;transform:translateX(-50%);
  display:flex;flex-direction:column;align-items:center;gap:6px;font-size:11px;
  color:rgba(255,255,255,0.85);text-shadow:0 1px 8px rgba(0,0,0,0.45);
}
.scroll-indicator .scroll-mouse { border-color:rgba(255,255,255,0.5); }
.scroll-indicator .scroll-wheel { background:rgba(255,255,255,0.7); }
.scroll-mouse { width:24px;height:38px;border-radius:12px;border:2px solid #cbd5e1;display:flex;justify-content:center;padding-top:6px; }
.scroll-wheel { width:4px;height:8px;border-radius:2px;background:#94a3b8;animation:scrollWheel 1.8s ease-in-out infinite; }

/* 数字 */
.section-numbers { background:#fff; }
.numbers-grid { display:grid;grid-template-columns:repeat(4,1fr);gap:24px; }
.number-item { text-align:center;padding:32px 20px;border-radius:14px;background:#f8fafc;border:1px solid #f1f5f9;transition:all .4s; }
.number-item:hover { transform:translateY(-4px);box-shadow:0 12px 28px rgba(0,0,0,.06); }
.number-value { font-size:clamp(32px,5vw,46px);font-weight:800;color:#1f6aa5;margin-bottom:6px; }
.number-unit { font-size:16px;font-weight:500;color:#7b95a8;margin-left:4px; }
.number-label { font-size:13px;color:#7b95a8; }

/* 服务宫格 */
.section-services { background:#f8fafc; }
.services-grid { display:grid;grid-template-columns:repeat(4,1fr);gap:16px; }
.service-card { position:relative;padding:28px 20px;border-radius:14px;background:#fff;border:1px solid #f1f5f9;text-decoration:none;color:inherit;transition:all .4s;display:flex;flex-direction:column;gap:14px;border-top-width:3px; }
.service-icon { font-size:40px; }
.service-info h3 { font-size:16px;font-weight:700;margin-bottom:4px; }
.service-info p { font-size:12px;color:#94a3b8; }
.service-arrow { position:absolute;top:16px;right:16px;color:#cbd5e1;font-size:14px;transition:all .3s; }
.service-card:hover .service-arrow { color:#3b82f6;right:12px; }
.tone-elder { border-top-color:#10b981; } .tone-child { border-top-color:#8b5cf6; }
.tone-agency { border-top-color:#f59e0b; } .tone-admin { border-top-color:#3b82f6; }

/* 客户端 */
.section-clients { background:#fff; }
.clients-row { display:flex;justify-content:center;gap:clamp(24px,5vw,48px);flex-wrap:wrap; }
.client-item { text-align:center;padding:32px 36px;border-radius:16px;background:#f8fafc;border:1px solid #f1f5f9;min-width:130px;transition:all .4s; }
.client-item:hover { transform:translateY(-4px) scale(1.03);box-shadow:0 12px 32px rgba(0,0,0,.08); }
.client-icon { font-size:42px;margin-bottom:12px; }
.client-item h3 { font-size:14px;font-weight:600; }

/* 文娱活动 */
.section-activities { background:#fafbfc; }
.activities-grid { display:grid;grid-template-columns:repeat(3,1fr);gap:20px; }
.activity-card { background:#fff;border-radius:16px;border:1px solid #f1f5f9;overflow:hidden;transition:all .4s; }
.activity-img { height:100px;display:flex;align-items:center;justify-content:center;position:relative;background:linear-gradient(135deg,#f8fafc,#eef2ff); }
.activity-emoji { font-size:48px; }
.activity-tag { position:absolute;top:10px;left:10px;font-size:10px;padding:3px 8px;border-radius:999px;background:#fff;color:#1f6aa5;border:1px solid #e2e8f0; }
.activity-info { padding:16px; }
.activity-info h3 { font-size:15px;font-weight:700;margin-bottom:8px; }
.activity-meta { display:flex;gap:12px;margin-bottom:8px;font-size:11px;color:#94a3b8; }
.activity-info p { font-size:12px;color:#64748b;margin-bottom:12px; }
.activity-join { width:100%;padding:8px;border-radius:8px;background:#eff6ff;color:#1f6aa5;border:1px solid #bfdbfe;font-size:12px;cursor:pointer;font-family:inherit;transition:all .2s; }
.activity-join:hover { background:#dbeafe; }

/* 生活服务 */
.section-life { background:#fff; }
.life-grid { display:grid;grid-template-columns:repeat(4,1fr);gap:20px; }
.life-card { background:#fff;border-radius:16px;border:1px solid #f1f5f9;padding:24px;text-align:center;transition:all .4s; }
.life-icon-wrap { width:64px;height:64px;border-radius:20px;display:flex;align-items:center;justify-content:center;margin:0 auto 14px; }
.life-icon { font-size:32px; }
.life-card h3 { font-size:16px;font-weight:700;margin-bottom:8px; }
.life-card > p { font-size:12px;color:#94a3b8;margin-bottom:12px;min-height:36px; }
.life-features { display:flex;flex-wrap:wrap;justify-content:center;gap:4px;margin-bottom:16px; }
.life-features span { font-size:10px;padding:3px 8px;border-radius:999px;background:#f8fafc;color:#64748b;border:1px solid #f1f5f9; }
.life-btn { width:100%;padding:8px;border-radius:8px;background:#1f6aa5;color:#fff;border:none;font-size:12px;cursor:pointer;font-family:inherit;transition:all .2s; }
.life-btn:hover { background:#174d7a; }

/* 学习赋能 */
.section-learning { background:#f8fafc; }
.learning-grid { display:grid;grid-template-columns:repeat(3,1fr);gap:20px; }
.learning-card { background:#fff;border-radius:16px;border:1px solid #f1f5f9;overflow:hidden;transition:all .4s; }
.learning-thumb { height:120px;display:flex;align-items:center;justify-content:center;position:relative;background:linear-gradient(135deg,#1e3a5f,#1f6aa5); }
.learning-emoji { font-size:48px; }
.learning-duration { position:absolute;bottom:8px;right:8px;font-size:10px;padding:2px 8px;border-radius:999px;background:rgba(0,0,0,.6);color:#fff; }
.learning-info { padding:16px; }
.learning-category { font-size:10px;padding:3px 8px;border-radius:999px;background:#eff6ff;color:#1f6aa5; }
.learning-info h3 { font-size:15px;font-weight:700;margin:8px 0 6px; }
.learning-info p { font-size:12px;color:#64748b;margin-bottom:10px; }
.learning-stats { display:flex;gap:12px;font-size:11px;color:#94a3b8; }

/* 新闻 */
.section-news { background:#fff; }
.news-grid { display:grid;grid-template-columns:repeat(3,1fr);gap:20px; }
.news-card { background:#fff;border-radius:14px;border:1px solid #f1f5f9;overflow:hidden;text-decoration:none;color:inherit;transition:all .4s;display:block; }
.news-img { height:100px;display:flex;align-items:center;justify-content:center;position:relative;background:linear-gradient(135deg,#f8fafc,#f1f5f9); }
.news-emoji { font-size:40px; }
.news-date { position:absolute;bottom:8px;right:8px;font-size:10px;padding:2px 8px;border-radius:999px;background:#fff;color:#94a3b8;border:1px solid #e2e8f0; }
.news-info { padding:16px; }
.news-info h3 { font-size:14px;font-weight:700;margin-bottom:8px;line-height:1.4;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden; }
.news-info p { font-size:12px;color:#94a3b8;margin-bottom:10px;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden; }
.news-meta { display:flex;justify-content:space-between;font-size:11px;color:#64748b; }
.news-arrow { color:#1f6aa5; }

/* 管理端 */
.section-admin { background:#f8fafc; }
.admin-grid { display:grid;grid-template-columns:repeat(3,1fr);gap:14px;margin-bottom:32px; }
.admin-card { display:flex;gap:14px;padding:20px;border-radius:12px;background:#fff;border:1px solid #f1f5f9;transition:all .3s; }
.admin-num { font-size:32px;font-weight:800;color:#e2e8f0;flex-shrink:0;transition:color .3s; }
.admin-card:hover .admin-num { color:#1f6aa5; }
.admin-info h3 { font-size:14px;font-weight:600;margin-bottom:6px; }
.admin-info p { font-size:12px;color:#94a3b8;line-height:1.5; }
.admin-bottom { text-align:center; }

/* 页脚 */
.section-footer { background:#1a2332;color:#fff;padding:56px 0 32px; }
.footer-grid { display:grid;grid-template-columns:2fr 1fr 1fr 1fr;gap:32px;margin-bottom:40px; }
.footer-logo { display:flex;align-items:center;gap:10px;color:#60a5fa;margin-bottom:12px; }
.footer-logo h3 { font-size:18px;color:#fff; }
.footer-logo p { font-size:11px;color:#94a3b8; }
.footer-tel { font-size:13px;color:#94a3b8; }
.footer-col h4 { font-size:14px;font-weight:600;margin-bottom:14px; }
.footer-col a, .footer-col span { display:block;font-size:12px;color:#94a3b8;text-decoration:none;margin-bottom:8px;transition:all .3s; }
.footer-col a:hover { color:#fff;transform:translateX(4px); }
.footer-bottom { display:flex;align-items:center;justify-content:space-between;padding-top:24px;border-top:1px solid rgba(255,255,255,.06);font-size:11px;color:#64748b;flex-wrap:wrap;gap:12px; }
.footer-links { display:flex;gap:8px; }
.footer-links a { color:#94a3b8;text-decoration:none; }
.footer-links a:hover { color:#fff; }

/* 响应式 */
@media (max-width:1199px) { .nav-desktop { display:none; } .side-service { right:8px; } }
@media (max-width:1023px) {
  .hero-content { max-width:min(560px,94vw);padding-top:88px; }
  .hero-desc { margin:16px 0 24px; }
  .numbers-grid { grid-template-columns:repeat(2,1fr); }
  .services-grid { grid-template-columns:repeat(2,1fr); }
  .activities-grid { grid-template-columns:repeat(2,1fr); }
  .life-grid { grid-template-columns:repeat(2,1fr); }
  .learning-grid { grid-template-columns:repeat(2,1fr); }
  .news-grid { grid-template-columns:repeat(2,1fr); }
  .admin-grid { grid-template-columns:repeat(2,1fr); }
  .footer-grid { grid-template-columns:repeat(2,1fr); }
}
@media (max-width:640px) {
  .numbers-grid { grid-template-columns:1fr; }
  .services-grid { grid-template-columns:1fr; }
  .activities-grid { grid-template-columns:1fr; }
  .life-grid { grid-template-columns:1fr; }
  .learning-grid { grid-template-columns:1fr; }
  .news-grid { grid-template-columns:1fr; }
  .admin-grid { grid-template-columns:1fr; }
  .footer-grid { grid-template-columns:1fr; }
  .clients-row { gap:12px; } .client-item { min-width:80px;padding:20px 24px; }
  .footer-bottom { flex-direction:column;text-align:center; }
  .scroll-indicator { display:none; }
}
.home-notice-bar { padding: 8px 0; background: #fffbeb; border-bottom: 1px solid #fde68a; }
.home-notice-inner { display: flex; align-items: center; gap: 8px; width: 100%; border: none; background: transparent; cursor: pointer; font-family: inherit; text-align: left; }
.home-notice-tag { font-size: 11px; padding: 2px 8px; border-radius: 4px; background: #f59e0b; color: #fff; }
.home-notice-text { font-size: 12px; color: #92400e; flex: 1; }
.section-empty { text-align: center; color: #94a3b8; font-size: 14px; padding: 24px 0; }
.activity-photo { width: 100%; height: 100%; object-fit: cover; position: absolute; inset: 0; }
.learning-btn { margin-top: 10px; padding: 6px 14px; border-radius: 8px; border: 1px solid #1f6aa5; background: #fff; color: #1f6aa5; font-size: 12px; cursor: pointer; }
.learning-btn:hover { background: #eff6ff; }
</style>