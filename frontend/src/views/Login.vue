<template>
  <div class="auth-shell">
    <div class="auth-decor" aria-hidden="true" />
    <div class="auth-panel">
      <nav class="auth-top" aria-label="页面导航">
        <button type="button" class="text-link" @click="router.push('/')">功能首页</button>
        <span class="sep">·</span>
        <button type="button" class="text-link" @click="router.push({ path: '/register', query: { role: role } })">
          注册账号
        </button>
      </nav>

      <header class="auth-header">
        <p class="auth-kicker">银发智盾</p>
        <h1 class="auth-title">登录</h1>
        <p class="auth-sub">银发智慧养老系统 · 多角色统一入口（演示）</p>
      </header>

      <div class="card">
        <div class="id-panel" v-if="lastLoggedUserId">
          <div class="id-panel-title">本机上次登录账号</div>
          <div class="id-panel-row">
            <span class="id-label">系统账号ID</span>
            <span class="id-value">{{ lastLoggedUserId }}</span>
          </div>
        </div>

        <div class="row">
          <label>身份角色</label>
          <select v-model="role">
            <option value="ELDER">老人</option>
            <option value="CHILD">子女</option>
            <option value="AGENCY">服务机构</option>
            <option value="ADMIN">管理员</option>
          </select>
        </div>

        <div class="field">
          <div class="field-label">用户姓名</div>
          <input
            v-model="userName"
            placeholder="请输入用户姓名"
            autocomplete="username"
            @blur="refreshPreviewUserId"
          />
        </div>

        <div v-if="previewUserId" class="id-hint">
          <span class="id-hint-label">该账号系统ID</span>
          <span class="id-hint-value">{{ previewUserId }}</span>
        </div>

        <div class="field">
          <div class="field-label">用户密码</div>
          <input
            v-model="password"
            placeholder="请输入用户密码"
            type="password"
            autocomplete="current-password"
          />
        </div>

        <div class="btn-row">
          <button class="btn-primary" type="button" @click="onLogin">登录系统</button>
          <button class="btn-secondary" type="button" @click="onRegister">注册</button>
        </div>
      </div>

      <p class="hint">
        注册时按身份分配独立账号空间：老人 E…、子女 C…、机构 G…、管理员 A…（如 E100001），全局不重复。请用对应角色的系统账号 ID
        完成亲属/机构绑定；登录仍使用姓名与密码。
      </p>
    </div>
  </div>

  <div v-if="showBindModal" class="modal-overlay" role="dialog" aria-modal="true" aria-label="绑定老人账号">
    <div class="modal-card">
      <div class="modal-top">
        <div class="modal-title">首次登录：绑定老人账号</div>
        <button class="modal-close" type="button" @click="showBindModal = false">×</button>
      </div>
      <div class="modal-body">
        <div class="field">
          <div class="field-label">老人系统账号ID</div>
          <input v-model="bindElderIdsDraft" placeholder="例如：E100001,E100002" />
          <div class="bind-hint">{{ bindHint }}</div>
        </div>
        <div v-if="bindError" class="bind-error">{{ bindError }}</div>
        <div class="btn-row">
          <button class="btn-secondary" type="button" @click="showBindModal = false">取消</button>
          <button class="btn-primary" type="button" :disabled="bindBusy" @click="confirmBind">
            {{ bindBusy ? "绑定中..." : "确认绑定并进入" }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import type { RegisterRole } from "../api/authServer";
import { bindChildEldersServer, getSystemUserIdIfExistsServer, loginUserServer } from "../api/authServer";
import { persistLoginSession } from "../utils/session";

const router = useRouter();
const route = useRoute();
const role = ref<RegisterRole>("ELDER");
const userName = ref("");
const password = ref("");
const previewUserId = ref("");
const showBindModal = ref(false);
const bindElderIdsDraft = ref("");
const bindBusy = ref(false);
const bindError = ref("");
const pendingLogin = ref<{ role: RegisterRole; userName: string; password: string; userId: string } | null>(null);

const lastLoggedUserId = ref(localStorage.getItem("elder.userId") ?? "");

async function refreshPreviewUserId() {
  const name = userName.value.trim();
  if (!name) {
    previewUserId.value = "";
    return;
  }
  previewUserId.value = (await getSystemUserIdIfExistsServer(role.value, name)) ?? "";
}

watch(role, () => {
  void refreshPreviewUserId();
});

onMounted(() => {
  const q = route.query.role;
  if (q === "ELDER" || q === "CHILD" || q === "AGENCY" || q === "ADMIN") {
    role.value = q;
    void refreshPreviewUserId();
  }
});

function onRegister() {
  router.push({ path: "/register", query: { role: role.value } });
}

function redirectHome() {
  const r = localStorage.getItem("elder.role");
  if (r === "ELDER") router.push("/elder/home");
  else if (r === "CHILD") router.push("/child/home");
  else if (r === "AGENCY") router.push("/agency/home");
  else if (r === "ADMIN") router.push("/admin/dashboard");
  else router.push("/login");
}

async function onLogin() {
  if (!userName.value.trim()) return alert("请输入用户姓名");
  if (!password.value.trim()) return alert("请输入用户密码");

  try {
    const res = await loginUserServer(role.value, userName.value.trim(), password.value);

    persistLoginSession(res, userName.value.trim());
    if (res.role === "CHILD" && (!res.linkedElderIds || res.linkedElderIds.length === 0)) {
      pendingLogin.value = { role: res.role, userName: userName.value.trim(), password: password.value, userId: res.userId };
      showBindModal.value = true;
      bindElderIdsDraft.value = "";
      bindError.value = "";
      return;
    }
    lastLoggedUserId.value = res.userId;
    if (res.role === "ADMIN" && res.address) localStorage.setItem("elder.adminOrg", res.address);
    redirectHome();
  } catch (e: any) {
    alert(e?.message ?? "登录失败");
  }
}

const bindHint = computed(() => "请输入要绑定的老人系统账号ID，多个用逗号或空格分隔（如 E100001,E100002）。");

function parseIds(raw: string): string[] {
  return Array.from(
    new Set(
      raw
        .split(/[,\uFF0C;\uFF1B\s]+/)
        .map((s) => s.trim().toUpperCase())
        .filter(Boolean)
    )
  );
}

async function confirmBind() {
  if (!pendingLogin.value) return;
  const ids = parseIds(bindElderIdsDraft.value);
  if (ids.length === 0) {
    bindError.value = "请至少输入一个老人系统账号ID";
    return;
  }
  bindBusy.value = true;
  bindError.value = "";
  try {
    await bindChildEldersServer(pendingLogin.value.userId, ids);
    // 绑定成功后重新登录一次拿到 linkedElderIds 并进入系统
    const res = await loginUserServer(pendingLogin.value.role, pendingLogin.value.userName, pendingLogin.value.password);
    persistLoginSession(res, pendingLogin.value.userName);
    showBindModal.value = false;
    pendingLogin.value = null;
    redirectHome();
  } catch (e: any) {
    bindError.value = e?.message ?? "绑定失败";
  } finally {
    bindBusy.value = false;
  }
}
</script>

<style scoped>
.auth-shell {
  min-height: 100%;
  position: relative;
  padding: 28px 16px 40px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.auth-decor {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background: radial-gradient(1200px 520px at 10% -10%, rgba(31, 106, 165, 0.14), transparent 55%),
    radial-gradient(900px 420px at 100% 0%, rgba(26, 35, 50, 0.08), transparent 50%),
    linear-gradient(180deg, #eef1f6 0%, #f5f6f8 100%);
}

.auth-panel {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 440px;
}

.auth-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 13px;
}

.text-link {
  border: 0;
  background: none;
  padding: 0;
  font-weight: 800;
  color: var(--elder-brand, #1f6aa5);
  cursor: pointer;
  letter-spacing: 0.02em;
}
.text-link:hover {
  text-decoration: underline;
  text-underline-offset: 3px;
}

.sep {
  color: rgba(26, 35, 50, 0.28);
  user-select: none;
}

.auth-header {
  margin-bottom: 18px;
}

.auth-kicker {
  margin: 0 0 6px;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.12em;
  color: rgba(26, 35, 50, 0.45);
  text-transform: uppercase;
}

.auth-title {
  margin: 0 0 8px;
  font-size: 1.75rem;
  font-weight: 1000;
  letter-spacing: -0.03em;
  color: #1a2332;
}

.auth-sub {
  margin: 0;
  font-size: 14px;
  font-weight: 650;
  color: rgba(26, 35, 50, 0.5);
  line-height: 1.45;
}

.card {
  background: #fff;
  padding: 22px 22px 20px;
  border-radius: var(--elder-radius-lg, 18px);
  border: 1px solid rgba(26, 35, 50, 0.08);
  box-shadow: var(--elder-shadow, 0 4px 28px rgba(26, 35, 50, 0.07));
}

.row {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 14px;
}

label {
  width: 100px;
  flex-shrink: 0;
  color: rgba(26, 35, 50, 0.72);
  font-weight: 800;
  font-size: 14px;
}

input,
select {
  flex: 1;
  min-width: 0;
  padding: 12px 14px;
  border-radius: var(--elder-radius-md, 12px);
  border: 1px solid rgba(26, 35, 50, 0.12);
  background: #fafbfc;
  font-size: 15px;
  font-weight: 650;
  color: #1a2332;
  transition: border-color 0.15s ease, box-shadow 0.15s ease, background 0.15s ease;
}

input::placeholder {
  color: rgba(26, 35, 50, 0.35);
}

input:hover,
select:hover {
  border-color: rgba(31, 106, 165, 0.25);
}

input:focus,
select:focus {
  outline: none;
  border-color: rgba(31, 106, 165, 0.55);
  box-shadow: 0 0 0 3px rgba(31, 106, 165, 0.12);
  background: #fff;
}

.field {
  margin-bottom: 14px;
}

.field-label {
  font-weight: 900;
  color: rgba(26, 35, 50, 0.72);
  margin-bottom: 8px;
  font-size: 13px;
}

.btn-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 6px;
}

.btn-primary {
  padding: 14px 14px;
  border-radius: 14px;
  border: 0;
  background: linear-gradient(180deg, #2478b8 0%, #1f6aa5 100%);
  color: white;
  font-weight: 900;
  font-size: 16px;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(31, 106, 165, 0.35);
  transition: transform 0.15s ease, box-shadow 0.15s ease, filter 0.15s ease;
}

.btn-primary:hover {
  filter: brightness(1.04);
  transform: translateY(-1px);
  box-shadow: 0 6px 22px rgba(31, 106, 165, 0.4);
}

.btn-primary:active {
  transform: translateY(0);
}

.btn-secondary {
  padding: 14px 14px;
  border-radius: 14px;
  border: 1px solid rgba(26, 35, 50, 0.12);
  background: #fff;
  color: #1a2332;
  font-weight: 900;
  font-size: 16px;
  cursor: pointer;
  transition: border-color 0.15s ease, background 0.15s ease;
}

.btn-secondary:hover {
  border-color: rgba(31, 106, 165, 0.35);
  background: rgba(31, 106, 165, 0.04);
}

.hint {
  margin-top: 18px;
  padding: 0 4px;
  color: rgba(26, 35, 50, 0.48);
  font-size: 12.5px;
  font-weight: 650;
  line-height: 1.55;
}

/* bind modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: grid;
  place-items: center;
  padding: 12px;
  z-index: 120;
}

.modal-card {
  width: 520px;
  max-width: 100%;
  background: white;
  border-radius: 18px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.modal-top {
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.modal-title {
  font-weight: 1000;
  font-size: 16px;
}

.modal-close {
  border: 0;
  background: transparent;
  font-size: 20px;
  cursor: pointer;
  color: rgba(0, 0, 0, 0.45);
}

.modal-body {
  padding: 14px 16px 16px;
}

.bind-hint {
  margin-top: 8px;
  font-size: 12px;
  font-weight: 650;
  color: rgba(26, 35, 50, 0.48);
  line-height: 1.45;
}

.bind-error {
  margin-top: 8px;
  font-size: 12px;
  font-weight: 800;
  color: #b42318;
}

.id-panel {
  margin-bottom: 18px;
  padding: 14px 14px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(31, 106, 165, 0.09), rgba(31, 106, 165, 0.04));
  border: 1px solid rgba(31, 106, 165, 0.18);
}

.id-panel-title {
  font-weight: 800;
  font-size: 12px;
  color: rgba(26, 35, 50, 0.48);
  margin-bottom: 8px;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.id-panel-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.id-label {
  font-weight: 800;
  color: rgba(26, 35, 50, 0.55);
  font-size: 13px;
}

.id-value {
  font-weight: 900;
  font-size: 17px;
  color: #1f6aa5;
  letter-spacing: 0.02em;
}

.id-hint {
  margin: -4px 0 14px;
  padding: 11px 14px;
  border-radius: 12px;
  background: rgba(26, 35, 50, 0.04);
  border: 1px solid rgba(26, 35, 50, 0.06);
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.id-hint-label {
  font-weight: 800;
  font-size: 12px;
  color: rgba(26, 35, 50, 0.48);
}

.id-hint-value {
  font-weight: 900;
  font-size: 15px;
  color: rgba(26, 35, 50, 0.82);
}
</style>
