<template>
  <div class="page">
    <h2 class="section-title">告警中心</h2>

    <div v-if="loading" class="card">加载中...</div>
    <div v-else class="card">
      <div v-if="alarms.length === 0" class="muted">暂无告警</div>

      <div v-for="a in alarms" :key="a.id" class="alarm">
        <div class="alarm-top">
          <div class="type">{{ a.type }}</div>
          <div class="status">{{ a.status }}</div>
        </div>
        <div class="meta">触发时间：{{ a.triggeredAt }}</div>
        <div v-if="a.riskScore != null" class="meta">风险分：{{ a.riskScore }}</div>

        <div class="actions">
          <button class="ok" @click="confirm(a.id, 'elder_app_safe')">
            我已安全
          </button>
          <button class="help" @click="confirm(a.id, 'elder_app_need_help')">
            需要帮助
          </button>
        </div>
      </div>
    </div>

    <div class="hint">
      适老化建议：大字、少步骤、明确确认按钮。该页面为框架占位。
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import type { AlarmDto } from "../../api/alarm";
import { confirmAlarm, listAlarms } from "../../api/alarm";

const router = useRouter();
const elderId = localStorage.getItem("elder.id") ?? "";

const loading = ref(true);
const alarms = ref<AlarmDto[]>([]);

async function refresh() {
  loading.value = true;
  try {
    alarms.value = await listAlarms(elderId, 20);
  } finally {
    loading.value = false;
  }
}

async function confirm(alarmId: string, source: string) {
  await confirmAlarm(alarmId, source);
  await refresh();
}

onMounted(() => {
  if (!elderId) {
    alert("缺少 elderId，请先在登录页填写。");
    return;
  }
  refresh();
});
</script>

<style scoped>
.card {
  background: white;
  border-radius: 14px;
  padding: 14px;
  border: 1px solid rgba(0, 0, 0, 0.08);
}
.alarm {
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  margin-bottom: 12px;
}
.alarm-top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}
.type {
  font-weight: 900;
  font-size: 18px;
}
.status {
  color: rgba(0, 0, 0, 0.55);
  font-weight: 800;
}
.meta {
  margin-top: 6px;
  color: rgba(0, 0, 0, 0.65);
  font-weight: 700;
}
.actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 10px;
}
.ok, .help {
  padding: 12px 10px;
  border-radius: 12px;
  border: 0;
  font-size: 16px;
  font-weight: 900;
  cursor: pointer;
}
.ok {
  background: #2e7d32;
  color: white;
}
.help {
  background: #d32f2f;
  color: white;
}
.muted {
  color: rgba(0, 0, 0, 0.5);
  font-weight: 800;
}
.hint {
  margin-top: 12px;
  color: rgba(0, 0, 0, 0.55);
  font-size: 13px;
}
</style>

