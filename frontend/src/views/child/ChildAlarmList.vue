<template>
  <div class="page">
    <h2 class="section-title">子女端 - 告警列表</h2>

    <ElderInfoCard />

    <div v-if="activeSos" class="sos-strip" role="alert">
      <span class="sos-strip-icon">🆘</span>
      <div>
        <div class="sos-strip-title">SOS 紧急求助待响应</div>
        <div class="sos-strip-time">{{ formatAlarmTime(activeSos.triggeredAt) }}</div>
      </div>
    </div>

    <div v-if="loading" class="card">加载中...</div>
    <div v-else class="card">
      <div v-if="alarms.length === 0" class="muted">暂无告警</div>

      <div v-for="a in alarms" :key="a.id" class="alarm" :class="{ sos: isSosAlarm(a) }">
        <div class="alarm-top">
          <div class="type">{{ alarmTypeLabel(a.type) }}</div>
          <div class="status">{{ alarmStatusLabel(a.status) }}</div>
        </div>
        <div class="meta">触发时间：{{ formatAlarmTime(a.triggeredAt) }}</div>
        <div class="meta desc">{{ alarmDescription(a) }}</div>
        <div v-if="a.riskScore != null" class="meta">风险分：{{ a.riskScore }}</div>

        <div class="actions">
          <button class="ok" :class="{ sos: isSosAlarm(a) }" @click="confirm(a.id)">
            {{ isSosAlarm(a) ? "确认已联系老人" : "已确认（联系老人）" }}
          </button>
          <button class="link" @click="viewDetails(a.id)">详情</button>
        </div>
      </div>
    </div>

    <div v-if="selected" class="modal">
      <div class="modal-card">
        <h3>告警详情</h3>
        <div class="row">
          <div class="k">告警类型</div>
          <div class="v">{{ alarmTypeLabel(selected.type) }}</div>
        </div>
        <div class="row">
          <div class="k">状态</div>
          <div class="v">{{ alarmStatusLabel(selected.status) }}</div>
        </div>
        <div class="row">
          <div class="k">触发时间</div>
          <div class="v">{{ formatAlarmTime(selected.triggeredAt) }}</div>
        </div>
        <div class="row">
          <div class="k">说明</div>
          <div class="v">{{ alarmDescription(selected) }}</div>
        </div>

        <div class="row actions2">
          <button class="ok" @click="confirm(selected.id)">已确认（联系老人）</button>
          <button class="close" @click="selected = null">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from "vue";
import type { AlarmDto } from "../../api/alarm";
import { confirmAlarm, getAlarm, listAlarms } from "../../api/alarm";
import ElderInfoCard from "../../components/ElderInfoCard.vue";
import {
  ALARM_POLL_INTERVAL_MS,
  alarmDescription,
  alarmStatusLabel,
  alarmTypeLabel,
  formatAlarmTime,
  isPendingAlarm,
  isSosAlarm
} from "../../utils/alarmDisplay";

const elderId = ref(localStorage.getItem("elder.id") ?? "");

const loading = ref(true);
const alarms = ref<AlarmDto[]>([]);
const selected = ref<AlarmDto | null>(null);

const activeSos = computed(
  () => alarms.value.find(a => isSosAlarm(a) && isPendingAlarm(a)) ?? null
);

let pollTimer: number | undefined;

async function refresh() {
  if (!elderId.value) return;
  loading.value = true;
  try {
    alarms.value = await listAlarms(elderId.value, 20);
  } finally {
    loading.value = false;
  }
}

function startPolling() {
  stopPolling();
  pollTimer = window.setInterval(() => void refresh(), ALARM_POLL_INTERVAL_MS);
}

function stopPolling() {
  if (pollTimer !== undefined) {
    window.clearInterval(pollTimer);
    pollTimer = undefined;
  }
}

async function confirm(alarmId: string) {
  await confirmAlarm(alarmId, "child_app_confirmed");
  await refresh();
  selected.value = null;
}

async function viewDetails(alarmId: string) {
  selected.value = await getAlarm(alarmId);
}

onMounted(() => {
  if (!elderId.value) {
    alert("缺少 elderId，请先在登录页填写。");
    return;
  }
  void refresh();
  startPolling();
});

const elderSwitchedHandler = (ev: Event) => {
  const detail = (ev as CustomEvent).detail as { elderId?: string };
  const newElderId = detail?.elderId;
  if (!newElderId) return;
  elderId.value = newElderId;
  void refresh();
};

onUnmounted(() => {
  stopPolling();
  window.removeEventListener("elder-switched", elderSwitchedHandler);
});

window.addEventListener("elder-switched", elderSwitchedHandler);
</script>

<style scoped>
.card {
  background: white;
  border-radius: 14px;
  padding: 14px;
  border: 1px solid rgba(0, 0, 0, 0.08);
}
.sos-strip {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding: 12px 14px;
  border-radius: 12px;
  background: #fef2f2;
  border: 1px solid #fca5a5;
}
.sos-strip-icon { font-size: 24px; }
.sos-strip-title { font-weight: 900; color: #b91c1c; }
.sos-strip-time { font-size: 12px; color: #7f1d1d; margin-top: 4px; }
.alarm {
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  margin-bottom: 12px;
}
.alarm.sos {
  border-color: #fca5a5;
  background: #fef2f2;
}
.alarm-top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}
.type { font-weight: 900; font-size: 18px; }
.status { color: rgba(0, 0, 0, 0.55); font-weight: 800; }
.meta { margin-top: 6px; color: rgba(0, 0, 0, 0.65); font-weight: 700; }
.meta.desc { font-size: 13px; line-height: 1.4; }
.actions {
  display: grid;
  grid-template-columns: 1fr 120px;
  gap: 10px;
  margin-top: 10px;
}
.ok, .link {
  padding: 12px 10px;
  border-radius: 12px;
  border: 0;
  font-size: 15px;
  font-weight: 900;
  cursor: pointer;
}
.ok { background: #1976d2; color: white; }
.ok.sos { background: #dc2626; }
.link { background: #fff; border: 1px solid rgba(0, 0, 0, 0.15); color: #1976d2; }
.muted { color: rgba(0, 0, 0, 0.5); font-weight: 800; }

.modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
}
.modal-card {
  width: 720px;
  max-width: 100%;
  background: white;
  border-radius: 16px;
  padding: 16px;
}
.row { display: flex; justify-content: space-between; margin-bottom: 10px; gap: 12px; }
.k { color: rgba(0,0,0,0.6); font-weight: 800; flex-shrink: 0; }
.v { font-weight: 900; text-align: right; }
.actions2 { gap: 10px; }
.close {
  padding: 12px 10px;
  border-radius: 12px;
  border: 1px solid rgba(0,0,0,0.15);
  background: white;
  font-weight: 900;
  cursor: pointer;
}
</style>
