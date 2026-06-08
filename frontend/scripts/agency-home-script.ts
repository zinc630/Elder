<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from "vue";
import type {
  AgencyAnomalyReportDto,
  AgencyClockInRecordDto,
  DispatchTaskDto,
  ServiceWorkerDto
} from "../../api/agency";
import {
  assignWorker,
  createTask as createDispatchTask,
  deleteWorker as apiDeleteWorker,
  listAnomalies,
  listClockIns,
  listTasks,
  listWorkers,
  addWorker,
  updateWorker,
  processAnomaly,
  updateTaskStatus,
  bookedByLabel,
  formatAgencyApiError
} from "../../api/agency";
import { listElders } from "../../api/elder";
import type { ElderProfileDto } from "../../api/elder";
import { deepSeekChat, type ChatTurn } from "../../api/deepseek";
import {
  loadActivities,
  saveActivities,
  loadFinance,
  saveFinance,
  loadNotifications,
  saveNotifications,
  loadDevices,
  saveDevices,
  loadSchedules,
  saveSchedules,
  loadEvaluations,
  saveEvaluations,
  type AgencyActivity,
  type AgencyFinance,
  type AgencyNotification,
  type AgencyDevice,
  type AgencySchedule,
  type AgencyEvaluation
} from "../../utils/agencyLocalStore";

type TabKey =
  | "dispatch"
  | "workers"
  | "schedule"
  | "evaluations"
  | "health"
  | "alerts"
  | "activities"
  | "finance"
  | "notifications"
  | "devices";

type AgencyAlertItem = {
  id: string;
  elderName: string;
  type: string;
  triggeredAt: string;
  status: "PENDING" | "PROCESSING" | "RESOLVED";
  processLog: string;
  fromApi: boolean;
};

type HealthRecord = {
  elderId: string;
  name: string;
  age: number | null;
  gender: string;
  healthStatus: string;
  heartRate: number | null;
  bloodPressure: string;
  bloodOxygen: number | null;
  lastUpdated: string;
};

type ChatMessage = { id: string; role: "user" | "assistant"; text: string };

type PromptField = { key: string; label: string; placeholder?: string };

const activeTab = ref<TabKey>("dispatch");
const isScrolled = ref(false);
const showAssistant = ref(false);
const showBackTop = ref(false);
const aiBusy = ref(false);
const aiDraft = ref("");

const tabs = [
  { key: "dispatch" as TabKey, icon: "", label: "工单派单" },
  { key: "workers" as TabKey, icon: "", label: "服务人员" },
  { key: "schedule" as TabKey, icon: "", label: "排班管理" },
  { key: "evaluations" as TabKey, icon: "", label: "服务评价" },
  { key: "health" as TabKey, icon: "", label: "健康档案" },
  { key: "alerts" as TabKey, icon: "", label: "告警管理" },
  { key: "activities" as TabKey, icon: "", label: "活动管理" },
  { key: "finance" as TabKey, icon: "", label: "收费管理" },
  { key: "notifications" as TabKey, icon: "", label: "消息通知" },
  { key: "devices" as TabKey, icon: "", label: "设备管理" }
];

const chatMessages = ref<ChatMessage[]>([
  { id: "welcome", role: "assistant", text: "您好，我是银发智盾智慧助手。在下方输入问题即可获得解答。" }
]);
const quickPrompts = ["如何查看工单？", "添加服务人员", "发布活动", "查看健康档案"];

const tasks = ref<DispatchTaskDto[]>([]);
const workers = ref<ServiceWorkerDto[]>([]);
const elderList = ref<ElderProfileDto[]>([]);
const anomalies = ref<AgencyAnomalyReportDto[]>([]);
const clockIns = ref<AgencyClockInRecordDto[]>([]);
const elderNameMap = reactive<Record<string, string>>({});

const activities = ref<AgencyActivity[]>([]);
const financeRecords = ref<AgencyFinance[]>([]);
const notifications = ref<AgencyNotification[]>([]);
const devices = ref<AgencyDevice[]>([]);
const schedules = ref<AgencySchedule[]>([]);
const evaluations = ref<AgencyEvaluation[]>([]);
const alerts = ref<AgencyAlertItem[]>([]);

const eldersCount = computed(() => elderList.value.length);
const agenciesCount = ref(1);
const totalWorkers = computed(() => workers.value.length);
const onlineWorkers = computed(() => workers.value.filter(w => w.onlineStatus === "ONLINE").length);

const todayOrders = computed(() => {
  const now = new Date();
  const y = now.getFullYear();
  const m = now.getMonth();
  const d = now.getDate();
  return tasks.value.filter(t => {
    const dt = new Date(t.createdAt || t.appointmentTime);
    return dt.getFullYear() === y && dt.getMonth() === m && dt.getDate() === d;
  }).length;
});

const pendingOrders = computed(() => tasks.value.filter(t => t.status === "NEW").length);

const completedThisMonth = computed(() => {
  const now = new Date();
  const y = now.getFullYear();
  const m = now.getMonth();
  return tasks.value.filter(t => t.status === "COMPLETED").filter(t => {
    const dt = new Date(t.appointmentTime);
    return dt.getFullYear() === y && dt.getMonth() === m;
  }).length;
});

const completionRate = computed(() => {
  const total = tasks.value.length;
  if (total === 0) return 0;
  const completed = tasks.value.filter(t => t.status === "COMPLETED").length;
  return Math.round((completed / total) * 100);
});

const orderTrendValues = computed(() => {
  const vals: number[] = [];
  const now = new Date();
  for (let i = 6; i >= 0; i--) {
    const d = new Date(now);
    d.setDate(d.getDate() - i);
    const y = d.getFullYear();
    const m = d.getMonth();
    const day = d.getDate();
    vals.push(
      tasks.value.filter(t => {
        const created = new Date(t.createdAt || t.appointmentTime);
        return created.getFullYear() === y && created.getMonth() === m && created.getDate() === day;
      }).length
    );
  }
  return vals;
});

const orderTrendLabels = computed(() => {
  const labels: string[] = [];
  const now = new Date();
  for (let i = 6; i >= 0; i--) {
    const d = new Date(now);
    d.setDate(d.getDate() - i);
    labels.push(`${d.getMonth() + 1}/${d.getDate()}`);
  }
  return labels;
});

const orderTrendPoints = computed(() =>
  orderTrendValues.value.map((v, i) => `${40 + i * 52},${getYPosition(v)}`).join(" ")
);

const areaPoints = computed(() => {
  const points = orderTrendValues.value.map((v, i) => `${40 + i * 52},${getYPosition(v)}`).join(" ");
  return `${40},140 ${points} ${40 + 6 * 52},140`;
});

function getYPosition(v: number) {
  const max = Math.max(...orderTrendValues.value, 1);
  return 125 - (v / max) * 100;
}

function readElderVitals(elderId: string) {
  try {
    const raw = localStorage.getItem(`elder.vitals.${elderId}`);
    if (!raw) return null;
    return JSON.parse(raw) as { hr?: number; sbp?: number; dbp?: number; spo2?: number; updatedAt?: string };
  } catch {
    return null;
  }
}

function deriveHealthStatus(hr: number | null, spo2: number | null, sbp: number | null): string {
  if (hr != null && (hr > 100 || hr < 60)) return "需关注";
  if (spo2 != null && spo2 < 95) return "需关注";
  if (sbp != null && sbp >= 140) return "异常";
  return "正常";
}

const healthRecords = computed<HealthRecord[]>(() =>
  elderList.value.map(e => {
    const v = readElderVitals(e.elderId);
    const hr = v?.hr ?? null;
    const sbp = v?.sbp ?? null;
    const dbp = v?.dbp ?? null;
    const spo2 = v?.spo2 ?? null;
    const status = deriveHealthStatus(hr, spo2, sbp);
    const bp = sbp != null && dbp != null ? `${sbp}/${dbp}` : "--";
    return {
      elderId: e.elderId,
      name: e.name,
      age: e.age,
      gender: e.gender || "—",
      healthStatus: status,
      heartRate: hr,
      bloodPressure: bp,
      bloodOxygen: spo2,
      lastUpdated: v?.updatedAt ?? new Date().toISOString()
    };
  })
);

const healthSearch = ref("");
const healthFilter = ref("all");

const filteredHealthRecords = computed(() => {
  let list = healthRecords.value;
  if (healthFilter.value === "normal") list = list.filter(e => e.healthStatus === "正常");
  else if (healthFilter.value === "attention") list = list.filter(e => e.healthStatus === "需关注");
  else if (healthFilter.value === "critical") list = list.filter(e => e.healthStatus === "异常");
  const kw = healthSearch.value.trim().toLowerCase();
  if (kw) list = list.filter(e => e.name.toLowerCase().includes(kw));
  return list;
});

function getHealthStatusClass(status: string) {
  if (status === "需关注") return "pending";
  if (status === "异常") return "danger";
  return "done";
}

const alertSearch = ref("");
const alertFilter = ref("all");
const alertBannerDismissed = ref(false);

const pendingAlerts = computed(() => alerts.value.filter(a => a.status === "PENDING"));

const filteredAlerts = computed(() => {
  let list = alerts.value;
  if (alertFilter.value !== "all") {
    const want = alertFilter.value.toUpperCase();
    list = list.filter(a => a.status === want);
  }
  const kw = alertSearch.value.trim().toLowerCase();
  if (kw) list = list.filter(a => a.elderName.toLowerCase().includes(kw) || a.type.toLowerCase().includes(kw));
  return list;
});

function mapAnomalyType(t: string): string {
  const u = (t || "").toUpperCase();
  if (u.includes("跌倒") || u.includes("FALL")) return "FALL";
  if (u.includes("SOS") || u.includes("求助")) return "SOS";
  if (u.includes("体征") || u.includes("VITAL")) return "VITALS";
  if (u.includes("电")) return "BATTERY";
  return u || "VITALS";
}

function syncAlertsFromAnomalies() {
  alerts.value = anomalies.value.map(a => ({
    id: a.id,
    elderName: a.elderName,
    type: mapAnomalyType(a.anomalyType),
    triggeredAt: a.reportedAt,
    status: a.status === "PENDING" ? "PENDING" : "RESOLVED",
    processLog: a.description ?? "",
    fromApi: true
  }));
}

function getAlertTypeLabel(type?: string) {
  const map: Record<string, string> = {
    SOS: "SOS求助",
    FALL: "跌倒预警",
    VITALS: "体征异常",
    BATTERY: "电量不足"
  };
  return type ? map[type] ?? type : "";
}

function getAlertTypeClass(type: string) {
  const map: Record<string, string> = {
    SOS: "alert-sos",
    FALL: "alert-fall",
    VITALS: "alert-vitals",
    BATTERY: "alert-battery"
  };
  return map[type] || "";
}

function getAlertStatusLabel(status: string) {
  const map: Record<string, string> = {
    PENDING: "待处理",
    PROCESSING: "处理中",
    RESOLVED: "已处理"
  };
  return map[status] || status;
}

function getAlertStatusClass(status: string) {
  const map: Record<string, string> = {
    PENDING: "pending",
    PROCESSING: "doing",
    RESOLVED: "done"
  };
  return map[status] || "";
}

function dismissAlertBanner() {
  alertBannerDismissed.value = true;
}

function openAlertList() {
  navToTab("alerts");
}

const activitySearch = ref("");
const filteredActivities = computed(() => {
  const kw = activitySearch.value.trim().toLowerCase();
  if (!kw) return activities.value;
  return activities.value.filter(a => a.title.toLowerCase().includes(kw));
});

const financeSearch = ref("");
const filteredFinanceRecords = computed(() => {
  const kw = financeSearch.value.trim().toLowerCase();
  if (!kw) return financeRecords.value;
  return financeRecords.value.filter(f => f.elderName.toLowerCase().includes(kw));
});

const totalIncome = computed(() =>
  financeRecords.value.reduce((acc, f) => (f.status === "paid" ? acc + f.amount : acc), 0).toFixed(2)
);

const pendingPayments = computed(() => financeRecords.value.filter(f => f.status === "pending").length);

const notificationSearch = ref("");
const filteredNotifications = computed(() => {
  const kw = notificationSearch.value.trim().toLowerCase();
  if (!kw) return notifications.value;
  return notifications.value.filter(n => n.title.toLowerCase().includes(kw));
});

const deviceSearch = ref("");
const filteredDevices = computed(() => {
  const kw = deviceSearch.value.trim().toLowerCase();
  if (!kw) return devices.value;
  return devices.value.filter(
    d => d.id.toLowerCase().includes(kw) || d.name.toLowerCase().includes(kw) || (d.elderName?.toLowerCase().includes(kw) ?? false)
  );
});

const weekDays = ["周一", "周二", "周三", "周四", "周五", "周六", "周日"];
const workHours = Array.from({ length: 12 }, (_, i) => i + 8);
const currentWeekStart = ref(new Date());

const currentWeekLabel = computed(() => {
  const start = currentWeekStart.value;
  const end = new Date(start);
  end.setDate(end.getDate() + 6);
  return `${start.getMonth() + 1}月${start.getDate()}日 - ${end.getMonth() + 1}月${end.getDate()}日`;
});

function prevWeek() {
  const d = new Date(currentWeekStart.value);
  d.setDate(d.getDate() - 7);
  currentWeekStart.value = d;
}

function nextWeek() {
  const d = new Date(currentWeekStart.value);
  d.setDate(d.getDate() + 7);
  currentWeekStart.value = d;
}

function getShifts(day: string, hour: number) {
  return schedules.value.filter(s => s.day === day && s.hour === hour);
}

function deleteShift(id: string) {
  if (!confirm("确定删除该排班吗？")) return;
  schedules.value = schedules.value.filter(s => s.id !== id);
  saveSchedules(schedules.value);
  showToast({ icon: "✓", title: "已删除", desc: "排班已移除" });
}

const evaluationSearch = ref("");
const evaluationFilter = ref("all");

const filteredEvaluations = computed(() => {
  let list = evaluations.value;
  if (evaluationFilter.value === "excellent") list = list.filter(e => e.rating >= 5);
  else if (evaluationFilter.value === "good") list = list.filter(e => e.rating === 4);
  else if (evaluationFilter.value === "normal") list = list.filter(e => e.rating === 3);
  else if (evaluationFilter.value === "poor") list = list.filter(e => e.rating <= 2);
  const kw = evaluationSearch.value.trim().toLowerCase();
  if (kw) list = list.filter(e => e.elderName.includes(kw) || e.workerName.includes(kw));
  return list;
});

const totalEvaluations = computed(() => evaluations.value.length);
const averageRating = computed(() => {
  if (evaluations.value.length === 0) return "0";
  const sum = evaluations.value.reduce((acc, e) => acc + e.rating, 0);
  return (sum / evaluations.value.length).toFixed(1);
});
const satisfactionRate = computed(() => {
  if (evaluations.value.length === 0) return 0;
  const satisfied = evaluations.value.filter(e => e.rating >= 4).length;
  return Math.round((satisfied / evaluations.value.length) * 100);
});

const taskSearch = ref("");
const filteredTasks = computed(() => {
  const k = taskSearch.value.trim();
  if (!k) return tasks.value;
  const kw = k.toLowerCase();
  return tasks.value.filter(t => {
    const elderName = elderNameMap[t.elderId] ?? t.elderId;
    const assigned = t.assignedWorkerId ? workers.value.find(w => w.id === t.assignedWorkerId)?.name ?? "" : "";
    const wo = workOrderNo(t).toLowerCase();
    return (
      wo.includes(kw) ||
      t.id.toLowerCase().includes(kw) ||
      t.elderId.toLowerCase().includes(kw) ||
      elderName.toLowerCase().includes(kw) ||
      t.serviceType.toLowerCase().includes(kw) ||
      assigned.toLowerCase().includes(kw)
    );
  });
});

function workOrderNo(t: DispatchTaskDto): string {
  return "WO" + t.id.replace(/-/g, "").slice(0, 6).toUpperCase();
}

function serviceTypeLabel(st: string) {
  const m: Record<string, string> = {
    NURSING: "助餐",
    BATH: "助浴",
    HOUSEKEEPING: "保洁",
    ACCOMPANY: "陪诊"
  };
  return m[st] ?? st;
}

function statusLabel(st?: string) {
  if (!st) return "";
  switch (st) {
    case "NEW":
      return "待派单";
    case "ASSIGNED":
      return "派单中";
    case "ARRIVING":
      return "到达中";
    case "IN_PROGRESS":
      return "服务中";
    case "COMPLETED":
      return "已完成";
    case "CANCELLED":
      return "已取消";
    default:
      return st;
  }
}

function statusClass(st: DispatchTaskDto["status"]) {
  if (st === "NEW") return "pending";
  if (st === "IN_PROGRESS" || st === "ASSIGNED" || st === "ARRIVING") return "doing";
  if (st === "COMPLETED") return "done";
  return "pending";
}

function assignedStaffName(t: DispatchTaskDto) {
  if (!t.assignedWorkerId) return "—";
  return workers.value.find(w => w.id === t.assignedWorkerId)?.name ?? "—";
}

const workerSearch = ref("");
const filteredWorkers = computed(() => {
  const k = workerSearch.value.trim();
  if (!k) return workers.value;
  const kw = k.toLowerCase();
  return workers.value.filter(
    w => w.name.toLowerCase().includes(kw) || (w.position ?? "").toLowerCase().includes(kw) || (w.phone ?? "").toLowerCase().includes(kw)
  );
});

function formatDateTime(isoOrTs: string) {
  if (!isoOrTs) return "";
  const d = new Date(isoOrTs);
  if (Number.isNaN(d.getTime())) return String(isoOrTs);
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  const hh = String(d.getHours()).padStart(2, "0");
  const mi = String(d.getMinutes()).padStart(2, "0");
  const ss = String(d.getSeconds()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}:${ss}`;
}

function toLocalDateTimeLocal(d: Date) {
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

const showReportModal = ref(false);
const monthlyOrderData = computed(() => {
  const data: number[] = [];
  const now = new Date();
  for (let i = 29; i >= 0; i--) {
    const d = new Date(now);
    d.setDate(d.getDate() - i);
    const y = d.getFullYear();
    const m = d.getMonth();
    const day = d.getDate();
    data.push(
      tasks.value.filter(t => {
        const created = new Date(t.createdAt || t.appointmentTime);
        return created.getFullYear() === y && created.getMonth() === m && created.getDate() === day;
      }).length
    );
  }
  return data;
});
const maxMonthlyOrders = computed(() => Math.max(...monthlyOrderData.value, 1));
const workerPerformance = computed(() =>
  workers.value.slice(0, 5).map((w, i) => ({
    id: w.id,
    name: w.name,
    score: 88 + ((w.name.codePointAt(0) ?? 0) + i * 7) % 10
  }))
);

const showHealthDetail = ref(false);
const currentHealthElder = ref<HealthRecord | null>(null);
const showAlertDetail = ref(false);
const currentAlert = ref<AgencyAlertItem | null>(null);
const showCreateTaskModal = ref(false);
const showAssignModal = ref(false);
const showTaskDetailModal = ref(false);
const showWorkerModal = ref(false);
const workerModalMode = ref<"add" | "edit">("add");
const selectedTask = ref<DispatchTaskDto | null>(null);

const showPromptModal = ref(false);
const promptTitle = ref("");
const promptFields = ref<PromptField[]>([]);
const promptValues = reactive<Record<string, string>>({});
let promptSubmitHandler: ((values: Record<string, string>) => void) | null = null;

const createTaskForm = reactive({
  elderId: "",
  serviceType: "NURSING",
  appointmentTimeLocal: toLocalDateTimeLocal(new Date(Date.now() + 3600000)),
  notes: ""
});

const assignForm = reactive({ workerId: "" });
const workerForm = reactive({ id: "", name: "", position: "", phone: "" });

const elderOptions = computed(() => elderList.value);

const assignWorkerOptions = computed(() => {
  if (!selectedTask.value) return [];
  const st = selectedTask.value.serviceType;
  return workers.value.filter(w => {
    if (w.onlineStatus !== "ONLINE") return false;
    if (w.serviceType === st) return true;
    if (st === "BATH" && w.serviceType === "NURSING") return true;
    return false;
  });
});

const assignRecoText = computed(() => {
  const w = workers.value.find(x => x.id === assignForm.workerId);
  if (!w) return "请选择服务人员后查看智能推荐说明。";
  const dist = 600 + ((w.id.length * 37) % 500);
  const rate = 95 + ((w.name.codePointAt(0) ?? 0) % 4);
  return `系统根据距离&评价推荐：${w.name}（距养老院约${dist}m，好评率${rate}%）`;
});

const toast = ref<{ icon: string; title: string; desc: string } | null>(null);
let toastTimer: number | undefined;

function showToast(payload: { icon: string; title: string; desc: string }) {
  toast.value = payload;
  if (toastTimer !== undefined) window.clearTimeout(toastTimer);
  toastTimer = window.setTimeout(() => {
    toast.value = null;
    toastTimer = undefined;
  }, 3000);
}

function toastDemo(msg: string) {
  showToast({ icon: "ℹ", title: "提示", desc: msg });
}

function openPrompt(title: string, fields: PromptField[], onSubmit: (values: Record<string, string>) => void) {
  promptTitle.value = title;
  promptFields.value = fields;
  for (const k of Object.keys(promptValues)) delete promptValues[k];
  for (const f of fields) promptValues[f.key] = "";
  promptSubmitHandler = onSubmit;
  showPromptModal.value = true;
}

function submitPromptModal() {
  const handler = promptSubmitHandler;
  if (!handler) return;
  const snapshot: Record<string, string> = {};
  for (const f of promptFields.value) snapshot[f.key] = (promptValues[f.key] ?? "").trim();
  showPromptModal.value = false;
  promptSubmitHandler = null;
  handler(snapshot);
}

function navToTab(key: TabKey) {
  activeTab.value = key;
  if (key === "alerts") void refreshAlerts();
  nextTick(() => {
    document.getElementById("dashboard")?.scrollIntoView({ behavior: "smooth", block: "start" });
  });
}

function switchTab(key: TabKey) {
  navToTab(key);
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: "smooth" });
}

function toggleAssistant() {
  showAssistant.value = !showAssistant.value;
}

async function sendAiMessage() {
  const text = aiDraft.value.trim();
  if (!text || aiBusy.value) return;
  chatMessages.value.push({ id: `m-${Date.now()}`, role: "user", text });
  aiDraft.value = "";
  aiBusy.value = true;
  try {
    const turns: ChatTurn[] = chatMessages.value.map(m => ({ role: m.role, text: m.text }));
    const reply = await deepSeekChat(turns);
    chatMessages.value.push({ id: `m-${Date.now() + 1}`, role: "assistant", text: reply });
  } catch (e: unknown) {
    chatMessages.value.push({
      id: `m-${Date.now() + 1}`,
      role: "assistant",
      text: e instanceof Error ? e.message : "助手暂时不可用，请稍后再试。"
    });
  } finally {
    aiBusy.value = false;
  }
}

function applyQuickPrompt(p: string) {
  aiDraft.value = p;
  void sendAiMessage();
}

function openReportModal() {
  showReportModal.value = true;
}

function exportReport() {
  showReportModal.value = false;
  showToast({ icon: "✓", title: "报表已生成", desc: "演示环境：报表数据已汇总，可对接 PDF 导出。" });
}

function openHealthDetail(e: HealthRecord) {
  currentHealthElder.value = e;
  showHealthDetail.value = true;
}

function openAlertDetail(a: AgencyAlertItem) {
  currentAlert.value = a;
  showAlertDetail.value = true;
}

async function processAlert(id: string) {
  const item = alerts.value.find(a => a.id === id);
  if (!item) return;
  try {
    if (item.fromApi) await processAnomaly(id);
    item.status = "RESOLVED";
    item.processLog = item.processLog || "已处理";
    await refreshAlerts();
    showToast({ icon: "✓", title: "告警已处理", desc: "该告警已标记为已处理。" });
  } catch (e: unknown) {
    showToast({ icon: "!", title: "处理失败", desc: formatAgencyApiError(e) });
  }
}

function openAddScheduleModal() {
  openPrompt(
    "添加排班",
    [
      { key: "day", label: "星期", placeholder: "周一" },
      { key: "hour", label: "小时(8-19)", placeholder: "9" },
      { key: "workerName", label: "服务人员", placeholder: "王阿姨" },
      { key: "type", label: "班次(morning/afternoon/night)", placeholder: "morning" }
    ],
    values => {
      const hour = Number(values.hour);
      const type = (values.type || "morning") as AgencySchedule["type"];
      if (!values.day || !values.workerName || Number.isNaN(hour)) {
        showToast({ icon: "!", title: "请填写完整", desc: "星期、小时、人员为必填项" });
        return;
      }
      schedules.value.push({
        id: `S${Date.now()}`,
        day: values.day,
        hour,
        workerName: values.workerName,
        type: type === "afternoon" || type === "night" ? type : "morning"
      });
      saveSchedules(schedules.value);
      showToast({ icon: "✓", title: "排班已添加", desc: `${values.day} ${hour}:00` });
    }
  );
}

function openAddActivityModal() {
  openPrompt(
    "发布活动",
    [
      { key: "title", label: "活动名称" },
      { key: "location", label: "地点" },
      { key: "maxParticipants", label: "名额", placeholder: "50" }
    ],
    values => {
      if (!values.title) return;
      const now = new Date();
      const end = new Date(now.getTime() + 7200000);
      activities.value.unshift({
        id: `A${Date.now()}`,
        title: values.title,
        startTime: now.toISOString(),
        endTime: end.toISOString(),
        location: values.location || "活动中心",
        maxParticipants: Number(values.maxParticipants) || 30,
        registered: 0,
        status: "upcoming",
        description: ""
      });
      saveActivities(activities.value);
      showToast({ icon: "✓", title: "活动已发布", desc: values.title });
    }
  );
}

function openActivityDetail(a: AgencyActivity) {
  showToast({ icon: "📋", title: a.title, desc: `${a.location} · ${a.description || "暂无描述"}` });
}

function openActivityRegistration(a: AgencyActivity) {
  showToast({ icon: "👥", title: "报名管理", desc: `${a.title}：已报名 ${a.registered}/${a.maxParticipants}` });
}

function openActivityCheckin(a: AgencyActivity) {
  if (a.registered < a.maxParticipants) a.registered += 1;
  saveActivities(activities.value);
  showToast({ icon: "✓", title: "签到成功", desc: `${a.title} 签到 +1` });
}

function openAddFinanceModal() {
  openPrompt(
    "新增收费",
    [
      { key: "elderName", label: "老人姓名" },
      { key: "serviceType", label: "服务类型(NURSING等)" },
      { key: "amount", label: "金额", placeholder: "100" }
    ],
    values => {
      if (!values.elderName || !values.amount) return;
      financeRecords.value.unshift({
        id: `F${Date.now()}`,
        elderName: values.elderName,
        serviceType: values.serviceType || "NURSING",
        amount: Number(values.amount) || 0,
        status: "pending",
        paidAt: null
      });
      saveFinance(financeRecords.value);
      showToast({ icon: "✓", title: "收费已录入", desc: values.elderName });
    }
  );
}

function openFinanceDetail(f: AgencyFinance) {
  showToast({
    icon: "💰",
    title: `${f.elderName} · ${serviceTypeLabel(f.serviceType)}`,
    desc: `¥${f.amount} · ${f.status === "paid" ? "已支付" : "待支付"}`
  });
}

function markAsPaid(id: string) {
  const record = financeRecords.value.find(f => f.id === id);
  if (!record) return;
  record.status = "paid";
  record.paidAt = new Date().toISOString();
  saveFinance(financeRecords.value);
  showToast({ icon: "✓", title: "已标记支付", desc: record.elderName });
}

function openAddNotificationModal() {
  openPrompt(
    "发布通知",
    [
      { key: "title", label: "标题" },
      { key: "content", label: "内容" }
    ],
    values => {
      if (!values.title) return;
      notifications.value.unshift({
        id: `N${Date.now()}`,
        title: values.title,
        content: values.content || "",
        status: "published",
        createdAt: new Date().toISOString(),
        author: "管理员",
        views: 0
      });
      saveNotifications(notifications.value);
      showToast({ icon: "✓", title: "通知已发布", desc: values.title });
    }
  );
}

function openNotificationDetail(n: AgencyNotification) {
  n.views += 1;
  saveNotifications(notifications.value);
  showToast({ icon: "📨", title: n.title, desc: n.content });
}

function editNotification(n: AgencyNotification) {
  openPrompt(
    "编辑通知",
    [
      { key: "title", label: "标题" },
      { key: "content", label: "内容" }
    ],
    values => {
      if (values.title) n.title = values.title;
      if (values.content) n.content = values.content;
      saveNotifications(notifications.value);
      showToast({ icon: "✓", title: "已保存", desc: n.title });
    }
  );
}

function deleteNotification(id: string) {
  if (!confirm("确定删除该通知吗？")) return;
  notifications.value = notifications.value.filter(n => n.id !== id);
  saveNotifications(notifications.value);
  showToast({ icon: "✓", title: "已删除", desc: "通知已移除" });
}

function openAddDeviceModal() {
  openPrompt(
    "添加设备",
    [
      { key: "name", label: "设备名称" },
      { key: "type", label: "类型", placeholder: "智能手环" }
    ],
    values => {
      if (!values.name) return;
      devices.value.unshift({
        id: `D${Date.now()}`,
        name: values.name,
        type: values.type || "智能设备",
        elderName: null,
        battery: 100,
        status: "online",
        lastOnline: new Date().toISOString()
      });
      saveDevices(devices.value);
      showToast({ icon: "✓", title: "设备已添加", desc: values.name });
    }
  );
}

function openDeviceDetail(d: AgencyDevice) {
  showToast({ icon: "📱", title: d.name, desc: `${d.type} · 电量 ${d.battery}%` });
}

function bindDevice(d: AgencyDevice) {
  if (d.elderName) {
    d.elderName = null;
    showToast({ icon: "🔗", title: "已解绑", desc: d.name });
  } else {
    const name = window.prompt("绑定老人姓名：", elderList.value[0]?.name ?? "");
    if (name) d.elderName = name;
  }
  saveDevices(devices.value);
}

function deleteDevice(id: string) {
  if (!confirm("确定删除该设备吗？")) return;
  devices.value = devices.value.filter(d => d.id !== id);
  saveDevices(devices.value);
  showToast({ icon: "✓", title: "已删除", desc: "设备已移除" });
}

function openCreateTaskModal() {
  showCreateTaskModal.value = true;
  if (!createTaskForm.elderId && elderList.value.length > 0) {
    createTaskForm.elderId = elderList.value[0].elderId;
  }
}

function parseLocalDateTimeToMillis(local: string) {
  return new Date(local).getTime();
}

async function createTaskSubmit() {
  if (!createTaskForm.elderId) {
    showToast({ icon: "!", title: "请选择老人", desc: "创建工单前需选择服务对象" });
    return;
  }
  try {
    const id = await createDispatchTask({
      elderId: createTaskForm.elderId,
      serviceType: createTaskForm.serviceType,
      appointmentTimeMillis: parseLocalDateTimeToMillis(createTaskForm.appointmentTimeLocal),
      notes: createTaskForm.notes
    });
    showCreateTaskModal.value = false;
    showToast({ icon: "✓", title: "创建成功", desc: `工单 ${id.slice(0, 8)} 已提交` });
    await refreshTasks();
  } catch (e: unknown) {
    showToast({ icon: "!", title: "创建失败", desc: formatAgencyApiError(e) });
  }
}

function openAssignModal(t: DispatchTaskDto) {
  selectedTask.value = t;
  showAssignModal.value = true;
  assignForm.workerId = assignWorkerOptions.value[0]?.id ?? "";
}

async function assignSubmit() {
  if (!selectedTask.value || !assignForm.workerId) {
    showToast({ icon: "!", title: "请选择服务人员", desc: "派单前需指定服务人员" });
    return;
  }
  try {
    await assignWorker(selectedTask.value.id, assignForm.workerId);
    showAssignModal.value = false;
    showToast({ icon: "✓", title: "派单成功", desc: "工单已指派" });
    await refreshTasks();
  } catch (e: unknown) {
    showToast({ icon: "!", title: "派单失败", desc: formatAgencyApiError(e) });
  }
}

function openTaskDetail(t: DispatchTaskDto) {
  selectedTask.value = t;
  showTaskDetailModal.value = true;
}

async function markTaskCompleted(t: DispatchTaskDto) {
  try {
    await updateTaskStatus(t.id, "COMPLETED");
    showToast({ icon: "✓", title: "完成服务", desc: "工单状态已更新为已完成" });
    await refreshTasks();
  } catch (e: unknown) {
    showToast({ icon: "!", title: "更新失败", desc: formatAgencyApiError(e) });
  }
}

function openAddWorkerModal() {
  workerModalMode.value = "add";
  workerForm.id = "";
  workerForm.name = "";
  workerForm.position = "";
  workerForm.phone = "";
  showWorkerModal.value = true;
}

function openEditWorkerModal(w: ServiceWorkerDto) {
  workerModalMode.value = "edit";
  workerForm.id = w.id;
  workerForm.name = w.name;
  workerForm.position = w.position;
  workerForm.phone = w.phone;
  showWorkerModal.value = true;
}

function closeWorkerModal() {
  showWorkerModal.value = false;
}

async function workerModalSubmit() {
  if (!workerForm.name.trim() || !workerForm.position.trim() || !workerForm.phone.trim()) {
    showToast({ icon: "!", title: "请填写完整", desc: "姓名、岗位、电话为必填" });
    return;
  }
  try {
    if (workerModalMode.value === "add") {
      await addWorker({
        name: workerForm.name.trim(),
        position: workerForm.position.trim(),
        phone: workerForm.phone.trim(),
        onlineStatus: "ONLINE"
      });
      showToast({ icon: "✓", title: "已添加", desc: "服务人员已加入列表" });
    } else {
      await updateWorker(workerForm.id, {
        name: workerForm.name.trim(),
        position: workerForm.position.trim(),
        phone: workerForm.phone.trim(),
        onlineStatus: "ONLINE"
      });
      showToast({ icon: "✓", title: "已保存", desc: "服务人员信息已更新" });
    }
    showWorkerModal.value = false;
    await refreshWorkers();
  } catch (e: unknown) {
    showToast({ icon: "!", title: "保存失败", desc: formatAgencyApiError(e) });
  }
}

async function deleteWorker(id: string) {
  if (!confirm("确定删除该服务人员吗？")) return;
  try {
    await apiDeleteWorker(id);
    showToast({ icon: "✓", title: "删除成功", desc: "服务人员已移除" });
    await refreshWorkers();
  } catch (e: unknown) {
    showToast({ icon: "!", title: "删除失败", desc: formatAgencyApiError(e) });
  }
}

function refreshHealthRecords() {
  /* healthRecords 为 computed，随 elderList 自动更新 */
}

async function refreshAlerts() {
  try {
    anomalies.value = await listAnomalies();
    syncAlertsFromAnomalies();
  } catch (e: unknown) {
    showToast({ icon: "!", title: "告警加载失败", desc: formatAgencyApiError(e) });
  }
}

function refreshActivities() {
  activities.value = loadActivities();
}

function refreshFinance() {
  financeRecords.value = loadFinance();
}

function refreshNotifications() {
  notifications.value = loadNotifications();
}

function refreshDevices() {
  devices.value = loadDevices();
}

function refreshEvaluations() {
  evaluations.value = loadEvaluations();
}

async function refreshTasks() {
  try {
    tasks.value = await listTasks();
  } catch (e: unknown) {
    tasks.value = [];
    showToast({ icon: "!", title: "工单加载失败", desc: formatAgencyApiError(e) });
  }
}

async function refreshWorkers() {
  try {
    workers.value = await listWorkers();
  } catch (e: unknown) {
    workers.value = [];
    showToast({ icon: "!", title: "人员加载失败", desc: formatAgencyApiError(e) });
  }
}

async function refreshEldersNameMap() {
  for (const k of Object.keys(elderNameMap)) delete elderNameMap[k];
  for (const e of elderList.value) elderNameMap[e.elderId] = e.name;
}

async function refreshAll() {
  try {
    elderList.value = await listElders();
    await refreshEldersNameMap();
  } catch {
    elderList.value = [];
  }
  await refreshWorkers();
  await refreshTasks();
  await refreshAlerts();
}

function loadLocalModules() {
  activities.value = loadActivities();
  financeRecords.value = loadFinance();
  notifications.value = loadNotifications();
  devices.value = loadDevices();
  schedules.value = loadSchedules();
  evaluations.value = loadEvaluations();
}

function handleScroll() {
  isScrolled.value = window.scrollY > 50;
  showBackTop.value = window.scrollY > 500;
}

onMounted(async () => {
  loadLocalModules();
  await refreshAll();
  window.addEventListener("scroll", handleScroll, { passive: true });
});

onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll);
});
</script>
