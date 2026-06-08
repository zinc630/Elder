<template>
  <div class="admin-reports">
    <h1 class="page-title">数据管理 / 统计分析 & 报表</h1>

    <div v-if="loadError" class="err">{{ loadError }}</div>

    <div class="grid-2">
      <div class="card">
        <h2>健康周报生成（近一周预警趋势）</h2>
        <svg v-if="trend.length" class="line-chart" viewBox="0 0 360 140">
          <polyline fill="none" stroke="#409eff" stroke-width="2.5" :points="hrLine" />
          <g v-for="(x, i) in trend" :key="i">
            <text :x="40 + i * 44" y="130" class="axis">{{ x.label }}</text>
          </g>
        </svg>
        <p v-else class="muted">暂无趋势数据</p>
        <button type="button" class="export" :disabled="exporting" @click="exportPdf">
          {{ exporting ? "生成中…" : "导出月度健康报告（PDF）" }}
        </button>
        <p class="note">汇总平台长者 {{ stats?.totalElders ?? "—" }} 人、今日告警 {{ stats?.todayAlerts ?? "—" }} 条。</p>
      </div>

      <div class="card">
        <h2>服务评价满意度 & 工单统计</h2>
        <svg class="pie" viewBox="0 0 200 200">
          <path :d="piePath(0, 0.72)" fill="#1f4e79" />
          <path :d="piePath(0.72, 0.92)" fill="#67c23a" />
          <path :d="piePath(0.92, 1)" fill="#e6a23c" />
        </svg>
        <div class="legend">
          <span><i class="c1" />非常满意</span>
          <span><i class="c2" />满意</span>
          <span><i class="c3" />一般</span>
        </div>
        <p class="note">本月工单 {{ stats?.monthlyServiceOrders ?? "—" }} 单 · 绑定设备 {{ stats?.boundDevices ?? "—" }} 台</p>
      </div>
    </div>

    <div class="card users">
      <h2>用户管理 / 权限设置</h2>
      <table class="tbl">
        <thead>
          <tr>
            <th>角色</th>
            <th>说明</th>
            <th>权限范围</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>系统管理员</td>
            <td>张院长</td>
            <td>全模块、外呼回执、报表导出</td>
          </tr>
          <tr>
            <td>护理主管</td>
            <td>—</td>
            <td>健康监测、派单、亲情看板</td>
          </tr>
          <tr>
            <td>数据员</td>
            <td>—</td>
            <td>报表只读</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import {
  getAlertTrend,
  getDashboardStats,
  type AdminAlertTrendPoint,
  type AdminDashboardStats
} from "../../api/admin";

const trend = ref<AdminAlertTrendPoint[]>([]);
const stats = ref<AdminDashboardStats | null>(null);
const loadError = ref("");
const exporting = ref(false);

const hrLine = computed(() => {
  const vals = trend.value.map((p) => p.count);
  if (!vals.length) return "";
  const max = Math.max(...vals, 1);
  const min = 0;
  return vals
    .map((v, i) => {
      const x = 30 + i * 48;
      const y = 110 - ((v - min) / (max - min || 1)) * 90;
      return `${x},${y}`;
    })
    .join(" ");
});

function polar(cx: number, cy: number, r: number, angle: number) {
  const a = (angle * Math.PI * 2) / 360 - Math.PI / 2;
  return [cx + r * Math.cos(a), cy + r * Math.sin(a)];
}

function piePath(startPct: number, endPct: number) {
  const cx = 100;
  const cy = 100;
  const r = 80;
  const a1 = startPct * 360;
  const a2 = endPct * 360;
  const [x1, y1] = polar(cx, cy, r, a1);
  const [x2, y2] = polar(cx, cy, r, a2);
  const large = a2 - a1 > 180 ? 1 : 0;
  return `M ${cx} ${cy} L ${x1} ${y1} A ${r} ${r} 0 ${large} 1 ${x2} ${y2} Z`;
}

async function loadReports() {
  loadError.value = "";
  try {
    const [t, s] = await Promise.all([getAlertTrend(), getDashboardStats()]);
    trend.value = t;
    stats.value = s;
  } catch (e) {
    loadError.value = e instanceof Error ? e.message : "加载失败";
  }
}

async function exportPdf() {
  exporting.value = true;
  try {
    await loadReports();
    const s = stats.value;
    alert(
      `已汇总平台数据：长者 ${s?.totalElders ?? "—"} 人，今日告警 ${s?.todayAlerts ?? "—"} 条，` +
        `本月工单 ${s?.monthlyServiceOrders ?? "—"} 单。正式环境可对接 PDF 生成服务下载。`
    );
  } finally {
    exporting.value = false;
  }
}

onMounted(() => {
  void loadReports();
});
</script>

<style scoped>
.page-title {
  margin: 0 0 20px;
  font-size: 20px;
  font-weight: 1000;
}
.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}
@media (max-width: 900px) {
  .grid-2 {
    grid-template-columns: 1fr;
  }
}
.card {
  background: #fff;
  border: 1px solid #e8eef5;
  border-radius: 12px;
  padding: 16px;
}
.card h2 {
  margin: 0 0 12px;
  font-size: 15px;
}
.line-chart {
  width: 100%;
  height: 140px;
}
.axis {
  font-size: 10px;
  fill: #94a3b8;
}
.export {
  margin-top: 12px;
  padding: 8px 16px;
  border-radius: 8px;
  border: none;
  background: #1f4e79;
  color: #fff;
  cursor: pointer;
}
.export:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.note,
.muted {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 8px;
}
.pie {
  width: 160px;
  height: 160px;
  display: block;
  margin: 0 auto;
}
.legend {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 8px;
  font-size: 12px;
}
.legend i {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 2px;
  margin-right: 4px;
}
.c1 {
  background: #1f4e79;
}
.c2 {
  background: #67c23a;
}
.c3 {
  background: #e6a23c;
}
.users {
  margin-top: 0;
}
.tbl {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.tbl th,
.tbl td {
  border-bottom: 1px solid #f1f5f9;
  padding: 10px 8px;
  text-align: left;
}
.err {
  color: #dc2626;
  margin-bottom: 12px;
  font-size: 13px;
}
</style>
