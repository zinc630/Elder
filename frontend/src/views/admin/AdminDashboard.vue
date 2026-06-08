<template>
  <div class="admin-dashboard">
    <header class="dash-head">
      <div>
        <h1 class="page-title">数据驾驶舱 | 实时运营看板</h1>
        <p class="page-sub">平台长者、工单、预警与体征一览 · 数据来自后端实时统计</p>
      </div>
      <button type="button" class="btn-refresh" :disabled="loading" @click="loadDashboard">
        {{ loading ? "刷新中…" : "刷新数据" }}
      </button>
    </header>

    <div v-if="loadError" class="err">{{ loadError }}</div>

    <div class="kpi-row">
      <div v-for="k in kpiCards" :key="k.label" class="kpi-card" :class="k.tone">
        <div class="kpi-num">{{ k.value }}</div>
        <div class="kpi-label">{{ k.label }}</div>
        <div v-if="k.sub" class="kpi-sub">{{ k.sub }}</div>
      </div>
    </div>

    <div class="grid-2">
      <div class="card chart-card">
        <h2 class="card-title">近一周健康异常趋势（预警事件）</h2>
        <TrendLineChart :points="alertTrend" color="#409eff" gradient-id="alertGrad" />
      </div>
      <div class="card chart-card">
        <h2 class="card-title">近一周服务工单趋势</h2>
        <TrendLineChart :points="serviceTrend" color="#67c23a" gradient-id="serviceGrad" />
      </div>
    </div>

    <section class="dash-charts-block" aria-label="统计图表">
      <div class="card card-chart-trend">
        <h2 class="card-title">近15天预警类型趋势</h2>
        <p v-if="!alarmTypeTrend.length" class="chart-empty">暂无数据</p>
        <div v-else class="column-chart-box">
          <ul class="multi-legend">
            <li v-for="s in alarmTypeSeries" :key="s.key">
              <i class="legend-bar" :style="{ background: s.color }" />
              <span>{{ s.name }}</span>
            </li>
          </ul>
          <div class="column-chart-plot column-chart-plot--grouped">
            <div v-for="(p, idx) in alarmTypeTrend" :key="idx" class="bar-group">
              <div class="bar-cluster">
                <div v-for="s in alarmTypeSeries" :key="s.key" class="bar-stick">
                  <span class="bar-top-val">{{ p[s.key] > 0 ? p[s.key] : "" }}</span>
                  <div
                    class="bar-fill"
                    :class="['tone-' + s.key, p[s.key] > 0 ? '' : 'bar-fill--empty']"
                    :style="{ height: barHeightPx(p[s.key], alarmTypeMax) + 'px' }"
                  />
                </div>
              </div>
              <span class="bar-x-label bar-x-label--day" :title="p.label">{{ p.label }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="grid-2 dash-charts-half">
      <div class="card card-chart-service">
        <h2 class="card-title">服务类型占比（有效工单）</h2>
        <p v-if="!serviceTypes.length" class="chart-empty">暂无数据</p>
        <div v-else class="column-chart-box">
          <div class="column-chart-plot column-chart-plot--service">
            <div v-for="(item, i) in serviceTypes" :key="item.label" class="bar-group bar-group--wide">
              <div class="bar-stick bar-stick--single">
                <span v-if="item.count > 0" class="bar-top-val">{{ item.count }}</span>
                <div
                  class="bar-fill bar-fill--service"
                  :class="'service-tone-' + (i % 3)"
                  :style="{ height: barHeightPx(item.count, serviceTypeMax) + 'px' }"
                />
              </div>
              <span class="bar-x-label bar-x-label--service">{{ item.label }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="card card-chart-pie">
        <h2 class="card-title">长者健康状态分布</h2>
        <div class="pie-wrap">
          <svg class="pie-svg" viewBox="0 0 200 200" role="img" aria-label="健康状态分布">
            <g v-for="(s, i) in healthPieSlices" :key="s.label">
              <path :d="s.path" :fill="healthColors[i % healthColors.length]" />
            </g>
            <circle cx="100" cy="100" r="42" fill="#fff" />
            <text x="100" y="96" text-anchor="middle" class="pie-center-num">{{ stats?.totalElders ?? 0 }}</text>
            <text x="100" y="114" text-anchor="middle" class="pie-center-label">长者</text>
          </svg>
          <ul class="pie-legend">
            <li v-for="(item, i) in healthDist" :key="item.label">
              <i :style="{ background: healthColors[i % healthColors.length] }" />
              <span>{{ item.label }}</span>
              <strong>{{ item.count }}</strong>
            </li>
          </ul>
        </div>
      </div>
      </div>
    </section>

    <div class="grid-2">
      <div class="card table-card">
        <h2 class="card-title">实时预警 / 高风险长者</h2>
        <table class="mini-table">
          <thead>
            <tr>
              <th>长者</th>
              <th>异常类型</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="highRisk.length === 0">
              <td colspan="3" class="empty-cell">暂无待处理预警</td>
            </tr>
            <tr v-for="r in highRisk" :key="r.alarmId">
              <td>{{ r.elderName }}</td>
              <td>{{ r.abnormalityType }}</td>
              <td><span class="pending">{{ r.statusLabel }}</span></td>
            </tr>
          </tbody>
        </table>
        <RouterLink class="more-link" to="/admin/health">查看全部预警 &gt;</RouterLink>
      </div>

      <div class="card table-card">
        <h2 class="card-title">长者体征监测（实时）</h2>
        <table class="mini-table vitals-table">
          <thead>
            <tr>
              <th>姓名</th>
              <th>心率</th>
              <th>血压</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="monitoring.length === 0">
              <td colspan="4" class="empty-cell">暂无长者档案</td>
            </tr>
            <tr v-for="e in monitoring" :key="e.elderId">
              <td>{{ e.name }}</td>
              <td>{{ e.heartRateBpm }}</td>
              <td>{{ e.bloodPressureDisplay }}</td>
              <td>
                <span class="level-tag" :class="'lv-' + e.warningLevel.toLowerCase()">{{ e.latestWarning }}</span>
              </td>
            </tr>
          </tbody>
        </table>
        <RouterLink class="more-link" to="/admin/health">健康监测详情 &gt;</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, onActivated, onBeforeUnmount, onMounted, ref, type PropType } from "vue";
import type {
  AdminAlertTrendPoint,
  AdminAlarmTypeTrendPoint,
  AdminDashboardStats,
  AdminElderVitalsRow,
  AdminHighRiskRow,
  AdminNamedCount
} from "../../api/admin";
import {
  formatAdminApiError,
  getAlertTrend,
  getAlarmTypeTrend,
  getDashboardStats,
  getHealthDistribution,
  getHighRiskAlarms,
  getMonitoringElders,
  getServiceTrend,
  getServiceTypeStats
} from "../../api/admin";

const stats = ref<AdminDashboardStats | null>(null);
const alertTrend = ref<AdminAlertTrendPoint[]>([]);
const serviceTrend = ref<AdminAlertTrendPoint[]>([]);
const alarmTypeTrend = ref<AdminAlarmTypeTrendPoint[]>([]);
const serviceTypes = ref<AdminNamedCount[]>([]);
const healthDist = ref<AdminNamedCount[]>([]);
const highRisk = ref<AdminHighRiskRow[]>([]);
const monitoring = ref<AdminElderVitalsRow[]>([]);
const loadError = ref("");
const loading = ref(false);

const healthColors = ["#67c23a", "#e6a23c", "#f56c6c"];

const kpiCards = computed(() => {
  const s = stats.value;
  return [
    { label: "长者总数", value: s?.totalElders ?? "—", sub: `绑定家属 ${s?.totalChildren ?? "—"} 人`, tone: "" },
    { label: "今日告警", value: s?.todayAlerts ?? "—", sub: `待处理 ${s?.pendingAlarms ?? "—"} 条`, tone: "warn" },
    { label: "本月工单", value: s?.monthlyServiceOrders ?? "—", sub: `已完成 ${s?.completedOrdersMonth ?? "—"} 单`, tone: "" },
    { label: "待处理工单", value: s?.pendingOrders ?? "—", sub: "含派单中", tone: "accent" },
    { label: "绑定设备", value: s?.boundDevices ?? "—", sub: "含定位终端", tone: "" },
    { label: "服务机构", value: s?.totalAgencies ?? "—", sub: "已入驻", tone: "" },
    { label: "家属账号", value: s?.totalChildren ?? "—", sub: "子女端", tone: "" },
    {
      label: "工单完成率",
      value: completionRate.value,
      sub: "本月创建工单",
      tone: "ok"
    }
  ];
});

const completionRate = computed(() => {
  const s = stats.value;
  if (!s || !s.monthlyServiceOrders) return "—";
  const pct = Math.round((s.completedOrdersMonth / s.monthlyServiceOrders) * 100);
  return `${Math.min(100, pct)}%`;
});

const healthPieSlices = computed(() => {
  const items = healthDist.value;
  const total = items.reduce((sum, i) => sum + i.count, 0) || 1;
  let angle = 0;
  const cx = 100;
  const cy = 100;
  const r = 78;
  return items.map((item) => {
    const sweep = (item.count / total) * 360;
    const path = sweep >= 359.9 ? fullCirclePath(cx, cy, r) : arcPath(cx, cy, r, angle, angle + sweep);
    angle += sweep;
    return { label: item.label, path };
  });
});

function polar(cx: number, cy: number, r: number, deg: number) {
  const rad = ((deg - 90) * Math.PI) / 180;
  return { x: cx + r * Math.cos(rad), y: cy + r * Math.sin(rad) };
}

function arcPath(cx: number, cy: number, r: number, start: number, end: number) {
  const s = polar(cx, cy, r, start);
  const e = polar(cx, cy, r, end);
  const large = end - start > 180 ? 1 : 0;
  return `M ${cx} ${cy} L ${s.x} ${s.y} A ${r} ${r} 0 ${large} 1 ${e.x} ${e.y} Z`;
}

function fullCirclePath(cx: number, cy: number, r: number) {
  return `M ${cx} ${cy - r} A ${r} ${r} 0 1 1 ${cx - 0.01} ${cy - r} Z`;
}

const TrendLineChart = defineComponent({
  name: "TrendLineChart",
  props: {
    points: { type: Array as PropType<AdminAlertTrendPoint[]>, default: () => [] },
    color: { type: String, default: "#409eff" },
    gradientId: { type: String, required: true }
  },
  setup(props) {
    const chartPad = 24;
    const chartW = 400 - chartPad * 2;
    const maxCount = computed(() => Math.max(7, ...props.points.map((t) => t.count), 1));
    const pointXs = computed(() => {
      const n = props.points.length || 1;
      return props.points.map((_, i) => chartPad + (i * chartW) / Math.max(n - 1, 1));
    });
    const pointYs = computed(() => {
      const max = maxCount.value;
      return props.points.map((t) => 130 - (t.count / max) * 110);
    });
    const linePoints = computed(() => pointXs.value.map((x, i) => `${x},${pointYs.value[i]}`).join(" "));
    const areaPoints = computed(() => {
      if (!pointXs.value.length) return "";
      const xs = pointXs.value;
      const ys = pointYs.value;
      const bottomY = 140;
      let d = `${xs[0]},${bottomY} `;
      for (let i = 0; i < xs.length; i++) d += `${xs[i]},${ys[i]} `;
      d += `${xs[xs.length - 1]},${bottomY}`;
      return d;
    });
    return () => {
      if (!props.points.length) {
        return h("p", { class: "chart-empty" }, "暂无数据");
      }
      const n = props.points.length;
      const viewBoxW = 400;
      const labelY = 152;
      return h(
        "div",
        {
          class: "trend-line-chart",
          style: { "--trend-cols": String(n) }
        },
        [
        h(
          "svg",
          { class: "chart-svg trend-line-svg", viewBox: `0 0 ${viewBoxW} 168`, preserveAspectRatio: "none" },
          [
            h("defs", null, [
              h("linearGradient", { id: props.gradientId, x1: "0", y1: "0", x2: "0", y2: "1" }, [
                h("stop", { offset: "0%", "stop-color": props.color, "stop-opacity": "0.35" }),
                h("stop", { offset: "100%", "stop-color": props.color, "stop-opacity": "0.02" })
              ])
            ]),
            h("polygon", { points: areaPoints.value, fill: `url(#${props.gradientId})` }),
            h("polyline", {
              points: linePoints.value,
              fill: "none",
              stroke: props.color,
              "stroke-width": "2.5"
            }),
            ...props.points.map((_, i) =>
              h("circle", {
                cx: pointXs.value[i],
                cy: pointYs.value[i],
                r: 4,
                fill: "#fff",
                stroke: props.color,
                "stroke-width": 2
              })
            ),
            ...props.points.map((p, i) =>
              h("text", {
                key: i,
                x: pointXs.value[i],
                y: labelY,
                "text-anchor": "middle",
                class: "svg-x-label"
              }, p.label)
            )
          ]
        )
      ]
      );
    };
  }
});

const alarmTypeSeries = [
  { key: "fallCount" as const, name: "跌倒检测", color: "#f56c6c" },
  { key: "sosCount" as const, name: "SOS 紧急", color: "#e6a23c" },
  { key: "vitalsCount" as const, name: "体征异常", color: "#409eff" }
];

const alarmTypeMax = computed(() => {
  let m = 1;
  for (const p of alarmTypeTrend.value) {
    m = Math.max(m, p.fallCount, p.sosCount, p.vitalsCount);
  }
  return m;
});

const serviceTypeMax = computed(() => Math.max(1, ...serviceTypes.value.map((i) => i.count), 1));

const BAR_AREA_PX = 112;

/** 柱高用 px：父级 flex 下 height:% 会失效为 0 */
function barHeightPx(count: number, max: number) {
  if (!max) return 0;
  if (!count) return 4;
  return Math.max(14, Math.round((count / max) * BAR_AREA_PX));
}

async function loadDashboard() {
  loadError.value = "";
  loading.value = true;
  try {
    const [s, tr, st, att, sv, hd, hr, elders] = await Promise.all([
      getDashboardStats(),
      getAlertTrend(),
      getServiceTrend(),
      getAlarmTypeTrend(),
      getServiceTypeStats(),
      getHealthDistribution(),
      getHighRiskAlarms(),
      getMonitoringElders()
    ]);
    stats.value = s;
    alertTrend.value = tr;
    serviceTrend.value = st;
    alarmTypeTrend.value = att;
    serviceTypes.value = sv;
    healthDist.value = hd;
    highRisk.value = hr;
    monitoring.value = elders.slice(0, 8);
  } catch (e: unknown) {
    loadError.value = formatAdminApiError(e);
  } finally {
    loading.value = false;
  }
}

function onPageVisible() {
  if (document.visibilityState === "visible") void loadDashboard();
}

onMounted(() => {
  document.addEventListener("visibilitychange", onPageVisible);
  void loadDashboard();
});

onActivated(() => {
  void loadDashboard();
});

onBeforeUnmount(() => {
  document.removeEventListener("visibilitychange", onPageVisible);
});
</script>

<style scoped>
.admin-dashboard {
  width: 100%;
  max-width: 100%;
  min-width: 0;
  box-sizing: border-box;
}
.dash-head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}
.page-title {
  margin: 0 0 4px;
  font-size: 22px;
  font-weight: 1000;
  color: rgba(0, 0, 0, 0.82);
}
.page-sub {
  margin: 0;
  font-size: 13px;
  font-weight: 700;
  color: rgba(0, 0, 0, 0.45);
}
.btn-refresh {
  padding: 10px 18px;
  border: 1px solid #409eff;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
}
.btn-refresh:hover:not(:disabled) {
  background: #d9ecff;
}
.btn-refresh:disabled {
  opacity: 0.65;
  cursor: wait;
}
.empty-cell,
.chart-empty {
  text-align: center;
  color: rgba(0, 0, 0, 0.45);
  font-weight: 800;
  padding: 24px 8px;
}
.chart-empty {
  margin: 0;
}
.err {
  padding: 12px;
  background: #fef0f0;
  color: #f56c6c;
  border-radius: 8px;
  margin-bottom: 16px;
  font-weight: 800;
}
.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 18px;
}
@media (max-width: 1100px) {
  .kpi-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
.kpi-card {
  background: #fff;
  border-radius: 12px;
  padding: 18px 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.kpi-card.warn .kpi-num {
  color: #e6a23c;
}
.kpi-card.accent .kpi-num {
  color: #409eff;
}
.kpi-card.ok .kpi-num {
  color: #67c23a;
}
.kpi-num {
  font-size: 26px;
  font-weight: 1000;
  color: rgba(0, 0, 0, 0.78);
  line-height: 1.1;
}
.kpi-label {
  margin-top: 8px;
  font-size: 13px;
  font-weight: 800;
  color: rgba(0, 0, 0, 0.5);
}
.kpi-sub {
  margin-top: 4px;
  font-size: 12px;
  font-weight: 700;
  color: rgba(0, 0, 0, 0.38);
}
.grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
  align-items: stretch;
  min-width: 0;
}
.dash-charts-block {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
  min-width: 0;
}
.dash-charts-half {
  margin-bottom: 0;
}
@media (max-width: 1000px) {
  .grid-2,
  .dash-charts-half {
    grid-template-columns: 1fr;
  }
}
.card {
  background: #fff;
  border-radius: 12px;
  padding: 18px 20px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  min-width: 0;
  overflow: hidden;
}
.card-chart-trend,
.card-chart-service,
.card-chart-pie {
  padding: 16px 18px;
}
.card-title {
  margin: 0 0 14px;
  font-size: 15px;
  font-weight: 1000;
  color: rgba(0, 0, 0, 0.72);
}
.chart-wrap {
  position: relative;
}
.trend-line-chart {
  width: 100%;
  min-width: 0;
}
.trend-line-svg,
.chart-svg {
  width: 100%;
  height: 180px;
  display: block;
}
.svg-x-label {
  font-size: 11px;
  font-weight: 800;
  fill: rgba(0, 0, 0, 0.55);
  text-anchor: middle;
}
.column-chart-box {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.multi-legend {
  list-style: none;
  margin: 0;
  padding: 0 4px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px 16px;
}
.multi-legend li {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 800;
  color: rgba(0, 0, 0, 0.58);
}
.multi-legend .legend-bar {
  width: 10px;
  height: 10px;
  border-radius: 3px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.12);
}
.column-chart-plot {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 2px;
  min-height: 168px;
  padding: 12px 8px 4px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 10px;
  background-color: #f8fafc;
  background-image: repeating-linear-gradient(
    to top,
    transparent,
    transparent 27px,
    rgba(0, 0, 0, 0.04) 27px,
    rgba(0, 0, 0, 0.04) 28px
  );
}
.column-chart-plot--grouped {
  overflow: hidden;
}
.column-chart-plot--grouped .bar-group {
  flex: 1 1 0;
  min-width: 0;
}
.column-chart-plot--service {
  padding-left: 16px;
  padding-right: 16px;
}
.column-chart-plot--service .bar-group--wide {
  flex: 1;
  min-width: 0;
}
.bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  height: 100%;
}
.bar-cluster {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 3px;
  flex: 1;
  width: 100%;
  height: 120px;
}
.column-chart-plot--grouped .bar-stick {
  max-width: 18px;
  min-width: 10px;
  width: 100%;
}
.card-chart-trend .column-chart-plot--grouped .bar-group {
  flex: 1 1 0;
  min-width: 0;
}
.bar-stick {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  height: 120px;
  flex-shrink: 0;
}
.bar-stick--single {
  max-width: 56px;
  width: 56%;
}
.bar-top-val {
  font-size: 10px;
  font-weight: 900;
  color: rgba(0, 0, 0, 0.55);
  line-height: 1;
  margin-bottom: 3px;
  min-height: 12px;
}
.bar-fill {
  width: 100%;
  flex-shrink: 0;
  border-radius: 4px 4px 2px 2px;
  min-height: 4px;
  transition: height 0.35s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}
.bar-fill.tone-fallCount {
  background: linear-gradient(180deg, #f78989 0%, #f56c6c 100%);
}
.bar-fill.tone-sosCount {
  background: linear-gradient(180deg, #f0c78a 0%, #e6a23c 100%);
}
.bar-fill.tone-vitalsCount {
  background: linear-gradient(180deg, #79bbff 0%, #409eff 100%);
}
.bar-fill--empty {
  background: #e4e7ed !important;
  box-shadow: none;
  opacity: 0.85;
}
.bar-fill--service.service-tone-0 {
  background: linear-gradient(180deg, #79bbff 0%, #409eff 100%);
}
.bar-fill--service.service-tone-1 {
  background: linear-gradient(180deg, #95d475 0%, #67c23a 100%);
}
.bar-fill--service.service-tone-2 {
  background: linear-gradient(180deg, #b37feb 0%, #9c27b0 100%);
}
.bar-x-label {
  display: block;
  width: 100%;
  text-align: center;
  font-size: 11px;
  font-weight: 800;
  color: rgba(0, 0, 0, 0.52);
  line-height: 1.25;
  word-break: keep-all;
}
.bar-x-label--day {
  font-size: 10px;
  font-weight: 800;
  line-height: 1.2;
  padding-top: 2px;
}
.bar-x-label--service {
  font-size: 12px;
  padding: 0 4px;
  color: rgba(0, 0, 0, 0.62);
}
.pie-wrap {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  justify-content: center;
}
.card-chart-pie .pie-wrap {
  flex-direction: row;
  padding: 4px 0;
}
.card-chart-pie .pie-svg {
  width: 150px;
  height: 150px;
}
.pie-svg {
  width: 160px;
  height: 160px;
  flex-shrink: 0;
}
.card-chart-pie .pie-legend {
  flex: 1;
  min-width: 100px;
}
.pie-center-num {
  font-size: 22px;
  font-weight: 1000;
  fill: rgba(0, 0, 0, 0.78);
}
.pie-center-label {
  font-size: 11px;
  font-weight: 800;
  fill: rgba(0, 0, 0, 0.45);
}
.pie-legend {
  list-style: none;
  margin: 0;
  padding: 0;
  flex: 1;
  min-width: 120px;
}
.pie-legend li {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 800;
  color: rgba(0, 0, 0, 0.62);
}
.pie-legend i {
  width: 10px;
  height: 10px;
  border-radius: 3px;
  flex-shrink: 0;
}
.pie-legend strong {
  margin-left: auto;
  color: rgba(0, 0, 0, 0.78);
}
.mini-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
.mini-table th {
  text-align: left;
  padding: 10px 8px;
  color: rgba(0, 0, 0, 0.45);
  font-weight: 1000;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}
.mini-table td {
  padding: 11px 8px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  font-weight: 800;
  color: rgba(0, 0, 0, 0.72);
}
.pending {
  color: #f56c6c;
  font-weight: 1000;
}
.level-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 900;
}
.lv-normal {
  background: #f0f9eb;
  color: #67c23a;
}
.lv-warn {
  background: #fdf6ec;
  color: #e6a23c;
}
.lv-crit {
  background: #fef0f0;
  color: #f56c6c;
}
.more-link {
  display: inline-block;
  margin-top: 14px;
  color: #409eff;
  font-weight: 900;
  font-size: 14px;
  text-decoration: none;
}
.more-link:hover {
  text-decoration: underline;
}
</style>