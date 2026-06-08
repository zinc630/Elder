<template>
  <div class="admin-pending">
    <h2 class="section-title">待处理告警 - 外呼升级</h2>

    <div v-if="loading" class="card">加载中...</div>
    <div v-else class="card">
      <div v-if="alarms.length === 0" class="muted">暂无待处理告警</div>

      <div v-else class="list">
        <div v-for="a in alarms" :key="a.id" class="item">
          <div class="top">
            <div class="t1">{{ a.type }}</div>
            <div class="t2">{{ a.status }}</div>
          </div>
          <div class="meta">老人：{{ a.elderId }} | 时间：{{ a.triggeredAt }}</div>
          <div class="actions">
            <button class="small" @click="selectedAlarmId = a.id">查看呼叫尝试</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="selectedAlarmId" class="card2">
      <h3>呼叫尝试</h3>
      <div class="actions" style="margin-bottom: 10px;">
        <button class="small" @click="refreshCalls">刷新</button>
        <button class="small ghost" @click="selectedAlarmId = null">关闭</button>
      </div>

      <div v-if="callAttempts.length === 0" class="muted">暂无呼叫尝试</div>
      <div v-else class="attempts">
        <div v-for="c in callAttempts" :key="c.id" class="attempt">
          <div class="meta">
            阶段：{{ c.stage }} | 次数：{{ c.attemptNo }} | 结果：{{ c.result ?? "未反馈" }}
          </div>
          <div v-if="c.result == null" class="result-actions">
            <button class="small" @click="setResult(c.id, 'NO_ANSWER')">未接听</button>
            <button class="small" @click="setResult(c.id, 'BUSY')">忙线</button>
            <button class="small ok" @click="setResult(c.id, 'ANSWERED')">拨通成功(answered)</button>
            <button class="small" @click="setResult(c.id, 'FAIL')">失败</button>
          </div>
          <div v-if="c.resultDetail" class="detail">{{ c.resultDetail }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import type { AlarmEventAdminDto, CallAttemptDto } from "../../api/admin";
import { listCallAttempts, listPendingAlarms, setCallAttemptResult } from "../../api/admin";

const loading = ref(true);
const alarms = ref<AlarmEventAdminDto[]>([]);
const selectedAlarmId = ref<string | null>(null);
const callAttempts = ref<CallAttemptDto[]>([]);

async function refresh() {
  loading.value = true;
  try {
    alarms.value = await listPendingAlarms();
  } finally {
    loading.value = false;
  }
}

async function refreshCalls() {
  if (!selectedAlarmId.value) return;
  callAttempts.value = await listCallAttempts(selectedAlarmId.value);
}

async function setResult(callAttemptId: string, result: string) {
  await setCallAttemptResult(callAttemptId, result, "admin_simulated");
  await refreshCalls();
  await refresh();
}

onMounted(async () => {
  await refresh();
});
</script>

<style scoped>
.card {
  background: white;
  border-radius: 14px;
  padding: 14px;
  border: 1px solid rgba(0,0,0,0.08);
}
.card2 {
  margin-top: 12px;
  background: white;
  border-radius: 14px;
  padding: 14px;
  border: 1px solid rgba(0,0,0,0.08);
}
.muted { color: rgba(0,0,0,0.55); font-weight: 800; }
.list { display: grid; gap: 12px; }
.item {
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(0,0,0,0.06);
}
.top { display: flex; justify-content: space-between; gap: 10px; }
.t1 { font-weight: 900; font-size: 16px; }
.t2 { color: rgba(0,0,0,0.55); font-weight: 800; }
.meta { margin-top: 6px; color: rgba(0,0,0,0.7); font-weight: 700; font-size: 13px; }
.small {
  padding: 10px 10px;
  border-radius: 10px;
  border: 1px solid rgba(0,0,0,0.12);
  background: white;
  font-weight: 900;
  cursor: pointer;
}
.ghost { background: transparent; }
.attempts { display: grid; gap: 10px; margin-top: 10px; }
.attempt {
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(0,0,0,0.06);
}
.result-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 10px;
}
.ok { background: #2e7d32; border: 0; color: white; }
.detail {
  margin-top: 8px;
  color: rgba(0,0,0,0.55);
  font-weight: 700;
  font-size: 13px;
}
</style>

