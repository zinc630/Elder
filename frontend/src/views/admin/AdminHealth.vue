<template>
  <div class="admin-health">
    <h1 class="page-title">健康监测 & 安全预警中心</h1>
    <p class="sub">实时健康监测（支持心率/血压/血氧/跌倒等预警演示）</p>

    <div v-if="err" class="err">{{ err }}</div>

    <div class="layout-split">
      <div class="card main-table">
        <table class="data-table">
          <thead>
            <tr>
              <th>姓名 / 年龄</th>
              <th>心率 bpm</th>
              <th>血压 mmHg</th>
              <th>血氧 %</th>
              <th>最新告警</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in elders" :key="row.elderId">
              <td>{{ row.name }} / {{ row.age ?? "—" }}</td>
              <td :class="{ crit: row.heartRateBpm > 100 }">{{ row.heartRateBpm }}</td>
              <td>{{ row.bloodPressureDisplay }}</td>
              <td :class="{ warn: row.bloodOxygenPercent < 95 }">{{ row.bloodOxygenPercent }}%</td>
              <td>
                <span :class="warnClass(row.warningLevel)">{{ row.latestWarning }}</span>
              </td>
              <td><button type="button" class="linkish" @click="detail(row)">详情</button></td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="side-col">
        <div class="card">
          <h2 class="card-h">跌倒 / 电子围栏实时告警</h2>
          <div v-for="c in critical" :key="c.id" class="crit-item">
            <div class="crit-title">{{ c.title }}</div>
            <div class="crit-time">{{ formatTime(c.occurredAt) }}</div>
            <button type="button" class="btn-handle">处理</button>
          </div>
        </div>
      </div>
    </div>

    <div class="card rules">
      <strong>体征异常规则引擎（示例）</strong>
      <p>
        心率 &gt;100 或 &lt;60、血氧 &lt;94%、设备离线 &gt;5 分钟、30 分钟无活动 等条件将触发分级预警；跌倒与 SOS 由体征与行为综合推断。
      </p>
    </div>

    <div v-if="simBarText" class="sim-bar">{{ simBarText }}</div>

    <div class="card map-block">
      <h2 class="card-h">电子地图 · 长者位置（演示）</h2>
      <div v-if="elders.length" class="map-toolbar">
        <label class="map-lab">选择长者</label>
        <select v-model="mapElderId" class="map-select">
          <option v-for="e in elders" :key="e.elderId" :value="e.elderId">
            {{ e.name }}（{{ e.elderId }}）
          </option>
        </select>
      </div>
      <ElderLocationMap v-if="mapElderId" :elder-id="mapElderId" height="360px" />
      <p v-else class="muted">暂无监测列表数据</p>
    </div>

    <div class="foot-actions">
      <RouterLink class="btn-ghost" to="/admin/pending-calls">待处理告警 · 外呼升级</RouterLink>
      <RouterLink class="btn-ghost" to="/admin/dashboard">返回仪表盘</RouterLink>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import type { AdminCriticalAlert, AdminElderVitalsRow } from "../../api/admin";
import { getCriticalAlerts, getMonitoringElders } from "../../api/admin";
import ElderLocationMap from "../../components/ElderLocationMap.vue";

const elders = ref<AdminElderVitalsRow[]>([]);
const critical = ref<AdminCriticalAlert[]>([]);
const err = ref("");
const mapElderId = ref("");

const simBarText = computed(() => {
  const crit = critical.value[0];
  if (crit) return `实时预警：${crit.title}（${formatTime(crit.occurredAt)}），请尽快处理。`;
  const high = elders.value.find((e) => e.heartRateBpm > 100 || e.bloodOxygenPercent < 94);
  if (high) {
    return `体征关注：${high.name} 心率 ${high.heartRateBpm} bpm，血氧 ${high.bloodOxygenPercent}%，${high.latestWarning}。`;
  }
  return elders.value.length ? "当前监测列表无紧急预警，体征均在关注范围内。" : "";
});

function warnClass(level: string) {
  if (level === "CRIT") return "tag crit";
  if (level === "WARN") return "tag warn";
  return "tag ok";
}

function formatTime(iso: string) {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  const p = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
}

function detail(row: AdminElderVitalsRow) {
  alert(`长者 ${row.name}（${row.elderId}）\n心率 ${row.heartRateBpm} 血压 ${row.bloodPressureDisplay} 血氧 ${row.bloodOxygenPercent}%`);
}

onMounted(async () => {
  try {
    const [e, c] = await Promise.all([getMonitoringElders(), getCriticalAlerts()]);
    elders.value = e;
    critical.value = c;
    if (e.length && !mapElderId.value) mapElderId.value = e[0].elderId;
  } catch (e: unknown) {
    err.value = (e as Error)?.message ?? "加载失败";
  }
});

watch(
  () => elders.value,
  (list) => {
    if (list.length && !list.some((x) => x.elderId === mapElderId.value)) {
      mapElderId.value = list[0].elderId;
    }
  },
  { deep: true }
);
</script>

<style scoped>
.page-title {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 1000;
}
.sub {
  margin: 0 0 18px;
  color: rgba(0, 0, 0, 0.5);
  font-weight: 700;
  font-size: 14px;
}
.err {
  padding: 12px;
  background: #fef0f0;
  color: #f56c6c;
  border-radius: 8px;
  margin-bottom: 14px;
  font-weight: 800;
}
.layout-split {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 16px;
  align-items: start;
}
@media (max-width: 1000px) {
  .layout-split {
    grid-template-columns: 1fr;
  }
}
.card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 18px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.main-table {
  overflow: auto;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
.data-table th {
  text-align: left;
  padding: 12px 10px;
  font-weight: 1000;
  color: rgba(0, 0, 0, 0.45);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  white-space: nowrap;
}
.data-table td {
  padding: 14px 10px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  font-weight: 800;
}
.crit {
  color: #f56c6c;
  font-weight: 1000;
}
.warn {
  color: #e6a23c;
  font-weight: 1000;
}
.tag {
  font-weight: 1000;
}
.tag.ok {
  color: #67c23a;
}
.tag.warn {
  color: #e6a23c;
}
.tag.crit {
  color: #f56c6c;
}
.linkish {
  border: 0;
  background: none;
  color: #409eff;
  font-weight: 900;
  cursor: pointer;
  padding: 0;
}
.card-h {
  margin: 0 0 14px;
  font-size: 15px;
  font-weight: 1000;
}
.crit-item {
  padding: 12px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}
.crit-item:last-child {
  border-bottom: 0;
}
.crit-title {
  font-weight: 900;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.78);
}
.crit-time {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
  margin: 6px 0 10px;
}
.btn-handle {
  border: 0;
  background: #fef0f0;
  color: #f56c6c;
  font-weight: 1000;
  padding: 6px 14px;
  border-radius: 8px;
  cursor: pointer;
}
.rules {
  margin-top: 16px;
}
.rules p {
  margin: 10px 0 0;
  line-height: 1.55;
  color: rgba(0, 0, 0, 0.6);
  font-size: 14px;
}
.sim-bar {
  margin-top: 14px;
  padding: 12px 16px;
  background: rgba(230, 162, 60, 0.15);
  border-radius: 10px;
  color: #b88230;
  font-weight: 800;
  font-size: 14px;
}
.map-block {
  margin-top: 16px;
}
.map-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.map-lab {
  font-weight: 900;
  font-size: 13px;
  color: rgba(0, 0, 0, 0.55);
}
.map-select {
  min-width: 220px;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  font-weight: 800;
}
.muted {
  color: rgba(0, 0, 0, 0.45);
  font-weight: 700;
  font-size: 14px;
}
.foot-actions {
  margin-top: 18px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.btn-ghost {
  display: inline-block;
  padding: 10px 16px;
  border-radius: 8px;
  border: 1px solid rgba(64, 158, 255, 0.5);
  color: #409eff;
  font-weight: 900;
  text-decoration: none;
  font-size: 14px;
}
</style>
