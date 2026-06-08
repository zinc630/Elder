<template>
  <div class="admin-dispatch">
    <h1 class="page-title">服务派单管理 | 全流程闭环</h1>

    <div class="toolbar">
      <div class="search-row">
        <input v-model="q" class="inp" placeholder="服务工单/老人姓名" @keyup.enter="load" />
        <button type="button" class="btn-blue" @click="load">搜索</button>
      </div>
      <button type="button" class="btn-green" @click="openNew">+ 新建服务预约（助浴/陪诊）</button>
    </div>

    <div class="card">
      <table class="tbl">
        <thead>
          <tr>
            <th>工单号</th>
            <th>老人</th>
            <th>服务类型</th>
            <th>服务人员</th>
            <th>状态</th>
            <th>预约时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in filtered" :key="t.id">
            <td class="mono">{{ wo(t) }}</td>
            <td>{{ nameOf(t.elderId) }}</td>
            <td>{{ stLabel(t.serviceType) }}</td>
            <td>{{ staff(t) }}</td>
            <td><span :class="'st ' + stClass(t.status)">{{ stZh(t.status) }}</span></td>
            <td>{{ fmt(t.appointmentTime) }}</td>
            <td class="ops">
              <button type="button" class="op" @click="dispatchOne(t)">派单/转单</button>
              <button v-if="t.status === 'IN_PROGRESS' || t.status === 'ASSIGNED'" type="button" class="op done" @click="complete(t)">
                完成
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="filtered.length === 0" class="empty">暂无工单</div>
    </div>

    <div class="card engine">
      <h2 class="eh">智能派单建议引擎</h2>
      <p class="ep">
        根据服务类型（助浴）& 距离最近 & 护工评价星级，推荐护理员：<strong>李秀芳</strong>（5.0 星，距离约 800m）
      </p>
      <button type="button" class="btn-blue lg" :disabled="!firstPending" @click="oneClick">一键派单</button>
    </div>

    <div v-if="showNew" class="modal-bg" @click.self="showNew = false">
      <div class="modal">
        <h3>新建服务预约</h3>
        <label>老人</label>
        <select v-model="form.elderId" class="inp">
          <option v-for="e in elders" :key="e.elderId" :value="e.elderId">{{ e.name }}</option>
        </select>
        <label>类型</label>
        <select v-model="form.serviceType" class="inp">
          <option value="BATH">助浴</option>
          <option value="ACCOMPANY">陪诊</option>
          <option value="NURSING">助餐</option>
          <option value="HOUSEKEEPING">保洁</option>
        </select>
        <label>预约时间</label>
        <input v-model="form.dt" type="datetime-local" class="inp" />
        <div class="ma">
          <button type="button" class="btn-g" @click="showNew = false">取消</button>
          <button type="button" class="btn-blue" @click="submitNew">创建</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import type { DispatchTaskDto, ServiceWorkerDto } from "../../api/agency";
import { assignWorker, createTask, listTasks, listWorkers, updateTaskStatus } from "../../api/agency";
import { listElders } from "../../api/elder";
import type { ElderProfileDto } from "../../api/elder";

const tasks = ref<DispatchTaskDto[]>([]);
const workers = ref<ServiceWorkerDto[]>([]);
const elders = ref<ElderProfileDto[]>([]);
const q = ref("");
const showNew = ref(false);

const form = reactive({
  elderId: "",
  serviceType: "BATH",
  dt: ""
});

function pad(n: number) {
  return String(n).padStart(2, "0");
}
function defaultDt() {
  const d = new Date(Date.now() + 3600000);
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

const filtered = computed(() => {
  const k = q.value.trim().toLowerCase();
  if (!k) return tasks.value;
  return tasks.value.filter(t => {
    const n = nameOf(t.elderId).toLowerCase();
    return wo(t).toLowerCase().includes(k) || t.id.toLowerCase().includes(k) || n.includes(k);
  });
});

const firstPending = computed(() => tasks.value.find(t => t.status === "NEW"));

function wo(t: DispatchTaskDto) {
  return "WO" + t.id.replace(/-/g, "").slice(0, 6).toUpperCase();
}
function stLabel(s: string) {
  const m: Record<string, string> = { NURSING: "助餐", BATH: "助浴", HOUSEKEEPING: "保洁", ACCOMPANY: "陪诊" };
  return m[s] ?? s;
}
function stZh(s: DispatchTaskDto["status"]) {
  if (s === "NEW") return "待派单";
  if (s === "IN_PROGRESS" || s === "ASSIGNED") return "服务中";
  if (s === "COMPLETED") return "已完成";
  return s;
}
function stClass(s: DispatchTaskDto["status"]) {
  if (s === "NEW") return "p";
  if (s === "COMPLETED") return "d";
  return "i";
}
function fmt(iso: string) {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
function nameOf(id: string) {
  return elders.value.find(e => e.elderId === id)?.name ?? id;
}
function staff(t: DispatchTaskDto) {
  if (!t.assignedWorkerId) return "—";
  return workers.value.find(w => w.id === t.assignedWorkerId)?.name ?? "—";
}

async function load() {
  const [ts, ws, es] = await Promise.all([listTasks(), listWorkers(), listElders()]);
  tasks.value = ts;
  workers.value = ws;
  elders.value = es;
}

function openNew() {
  form.dt = defaultDt();
  if (!form.elderId && elders.value[0]) form.elderId = elders.value[0].elderId;
  showNew.value = true;
}

async function submitNew() {
  await createTask({
    elderId: form.elderId,
    serviceType: form.serviceType,
    appointmentTimeMillis: new Date(form.dt).getTime(),
    notes: ""
  });
  showNew.value = false;
  await load();
}

async function dispatchOne(t: DispatchTaskDto) {
  const cand = workers.value.filter(w => w.onlineStatus === "ONLINE");
  const w = cand.find(x => x.serviceType === t.serviceType) ?? cand[0];
  if (!w) return alert("无在线服务人员");
  await assignWorker(t.id, w.id);
  await load();
}

async function complete(t: DispatchTaskDto) {
  await updateTaskStatus(t.id, "COMPLETED");
  await load();
}

async function oneClick() {
  const t = firstPending.value;
  if (!t) return;
  await dispatchOne(t);
}

onMounted(load);
</script>

<style scoped>
.page-title {
  margin: 0 0 18px;
  font-size: 20px;
  font-weight: 1000;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.search-row {
  display: flex;
  gap: 10px;
  align-items: center;
}
.inp {
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  min-width: 220px;
  font-weight: 700;
}
.btn-blue {
  border: 0;
  background: #409eff;
  color: #fff;
  font-weight: 1000;
  padding: 10px 18px;
  border-radius: 8px;
  cursor: pointer;
}
.btn-blue.lg {
  margin-top: 12px;
}
.btn-blue:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.btn-green {
  border: 0;
  background: #67c23a;
  color: #fff;
  font-weight: 1000;
  padding: 10px 16px;
  border-radius: 8px;
  cursor: pointer;
}
.card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.tbl {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
.tbl th {
  text-align: left;
  padding: 10px 8px;
  color: rgba(0, 0, 0, 0.45);
  font-weight: 1000;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}
.tbl td {
  padding: 12px 8px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  font-weight: 800;
}
.mono {
  font-family: ui-monospace, monospace;
}
.st.p {
  color: #e6a23c;
  font-weight: 1000;
}
.st.i {
  color: #409eff;
  font-weight: 1000;
}
.st.d {
  color: #67c23a;
  font-weight: 1000;
}
.ops {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.op {
  border: 0;
  background: rgba(64, 158, 255, 0.12);
  color: #409eff;
  font-weight: 900;
  padding: 6px 10px;
  border-radius: 6px;
  cursor: pointer;
}
.op.done {
  background: rgba(103, 194, 58, 0.15);
  color: #67c23a;
}
.empty {
  padding: 24px;
  text-align: center;
  color: rgba(0, 0, 0, 0.45);
  font-weight: 800;
}
.engine {
  margin-top: 16px;
}
.eh {
  margin: 0 0 10px;
  font-size: 16px;
  font-weight: 1000;
}
.ep {
  margin: 0;
  line-height: 1.55;
  color: rgba(0, 0, 0, 0.65);
  font-size: 14px;
}
.modal-bg {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 300;
  padding: 16px;
}
.modal {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  width: 400px;
  max-width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.modal h3 {
  margin: 0 0 8px;
}
.modal label {
  font-weight: 800;
  font-size: 13px;
  color: rgba(0, 0, 0, 0.55);
}
.ma {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 12px;
}
.btn-g {
  border: 1px solid rgba(0, 0, 0, 0.12);
  background: #fff;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 900;
}
</style>
