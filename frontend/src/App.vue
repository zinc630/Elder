<template>
  <div
    v-if="showBoot"
    class="boot-screen"
    :class="{ 'light-mode': lightMode }"
    role="status"
    aria-live="polite"
    aria-label="系统加载中"
  >
    <div class="startup-card">
      <div class="top-accent-bar"></div>

      <div class="shield-emblem">
        <div class="shield-icon">
          <div class="shield-inner"></div>
        </div>
        <h1 class="brand-name">银发智盾</h1>
        <div class="subtitle">SILVER GUARDIAN</div>
      </div>

      <div class="status-text">{{ statusText }}
        <template v-if="!finished">
          <span class="dot-pulse"></span>
          <span class="dot-pulse"></span>
          <span class="dot-pulse"></span>
        </template>
      </div>

      <div class="progress-wrapper">
        <div class="progress-bar-bg">
          <div class="progress-fill" :style="{ width: `${progress}%` }"></div>
        </div>
        <div class="progress-value">
          <span>{{ progress }}</span><span class="percent-sign">%</span>
        </div>
      </div>

      <div class="footer-note">
        <span class="lock-icon">🔒</span>
        <span>端到端加密</span>
        <span class="brand-dot"></span>
        <span>智盾核心 v2.4</span>
      </div>
    </div>
  </div>
  <router-view v-else />
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();
const showBoot = ref(route.path === "/");
const progress = ref(0);
const statusText = ref("系统启动中，请稍候");
const lightMode = ref(false);
const finished = ref(false);

let timer: ReturnType<typeof setTimeout> | null = null;

function scheduleNext() {
  if (progress.value >= 100) return;
  let delay = 35;
  if (progress.value < 10) delay = 88;
  else if (progress.value < 30) delay = 63;
  else if (progress.value < 70) delay = 28;
  else if (progress.value < 90) delay = 49;
  else delay = 105;

  timer = setTimeout(() => {
    progress.value = Math.min(progress.value + 1, 100);
    if (progress.value >= 100) {
      finished.value = true;
      statusText.value = "系统启动完成";
      setTimeout(() => {
        lightMode.value = true;
      }, 600);
      setTimeout(() => {
        showBoot.value = false;
      }, 1400);
      return;
    }
    scheduleNext();
  }, delay);
}

onMounted(() => {
  if (!showBoot.value) return;
  timer = setTimeout(() => {
    scheduleNext();
  }, 300);
});

onBeforeUnmount(() => {
  if (timer) clearTimeout(timer);
});
</script>

<style scoped>
* { box-sizing: border-box; }
.boot-screen {
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.8rem;
  position: relative;
  overflow-x: hidden;
  background: radial-gradient(circle at 30% 30%, #1a1f2b, #0b0d12);
  transition: background 0.8s ease;
}
.boot-screen::before {
  content: "";
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 50% 30%, rgba(90, 140, 210, 0.12) 0%, transparent 60%);
}
.boot-screen::after {
  content: "";
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 40% 35%, rgba(31, 106, 165, 0.1) 0%, rgba(23, 77, 122, 0.04) 30%, transparent 60%);
  opacity: 0;
  transition: opacity 0.8s ease;
}
.boot-screen.light-mode {
  background: linear-gradient(160deg, #f3f6f9 0%, #f8fafc 35%, #ffffff 65%, #f2f4f8 100%);
}
.boot-screen.light-mode::before { opacity: 0; }
.boot-screen.light-mode::after { opacity: 1; }

.startup-card {
  position: relative;
  width: 100%;
  max-width: 500px;
  border-radius: 2.5rem;
  padding: 2.8rem 2.4rem;
  z-index: 10;
  text-align: center;
  background: rgba(18, 22, 30, 0.75);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow: 0 30px 50px rgba(0, 0, 0, 0.7);
}
.boot-screen.light-mode .startup-card {
  background: #fff;
  backdrop-filter: none;
  box-shadow: 0 30px 60px -15px rgba(26, 51, 71, 0.12);
}
.top-accent-bar {
  position: absolute; top: 0; left: 50%; transform: translateX(-50%);
  width: 80px; height: 4px; border-radius: 0 0 6px 6px;
  background: linear-gradient(90deg, transparent, rgba(180, 200, 220, 0.6) 30%, rgba(210, 225, 240, 0.8) 50%, rgba(180, 200, 220, 0.6) 70%, transparent);
}
.shield-icon { width: 72px; height: 72px; margin: .6rem auto 1.2rem; border-radius: 20px; display:flex; align-items:center; justify-content:center; background: linear-gradient(135deg, #2f3e5c, #141a24);}
.shield-inner { width: 32px; height: 36px; border: 2.5px solid #c0d4f0; border-radius: 6px 6px 10px 10px; position: relative; }
.shield-inner::after { content:""; position:absolute; left:50%; bottom:4px; transform:translateX(-50%); width:14px; height:14px; border-radius:2px; background:#b7ceff; }
.brand-name { font-size: 2.4rem; font-weight: 600; letter-spacing: 2px; color: #eef5ff; }
.subtitle { font-size: .85rem; letter-spacing: 3px; color: rgba(190, 205, 230, 0.7); margin-top:.4rem; }
.status-text { margin: 2rem 0 1.5rem; color:#b4c8e6; display:flex; justify-content:center; align-items:center; gap:.4rem; }
.dot-pulse { width:6px; height:6px; border-radius:50%; background:#7aa2f7; animation: softPulse 1.4s infinite; }
.dot-pulse:nth-child(2){animation-delay:.2s}.dot-pulse:nth-child(3){animation-delay:.4s}
@keyframes softPulse { 0%{opacity:.4;transform:scale(.9)} 50%{opacity:1;transform:scale(1.3)} 100%{opacity:.4;transform:scale(.9)} }
.progress-bar-bg { border-radius: 60px; height: 14px; padding: 3px; background: rgba(20, 25, 35, 0.8); }
.progress-fill { height: 100%; border-radius: 60px; transition: width .3s ease; background: linear-gradient(90deg, #6b8fc7, #b7d0ff, #cfe2ff); }
.progress-value { margin-top: .7rem; text-align: right; font-size: 1.5rem; color:#eef3ff; }
.percent-sign { font-size: 1rem; margin-left: 3px; color: #b7cefd; }
.footer-note { margin-top:2rem; padding-top:1.4rem; border-top:1px solid rgba(255,255,255,.08); color: rgba(160, 180, 210, 0.5); font-size:.8rem; display:flex; justify-content:center; gap:.5rem; }
.brand-dot{width:4px;height:4px;border-radius:50%;background:#c9a96e;display:inline-block}
.boot-screen.light-mode .brand-name,
.boot-screen.light-mode .progress-value { color:#1a3347; }
.boot-screen.light-mode .status-text,
.boot-screen.light-mode .subtitle { color:#3d5a70; }
</style>

