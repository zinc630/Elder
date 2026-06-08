<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="brand">银发智盾 · 管理端</div>
      <nav class="nav">
        <RouterLink
          v-for="item in menu"
          :key="item.to"
          :to="item.to"
          class="nav-item"
          active-class="active"
        >
          {{ item.label }}
        </RouterLink>
      </nav>
    </aside>
    <div class="main-col">
      <header class="top-bar">
        <div class="welcome">欢迎，管理员 {{ displayName }}</div>
        <div class="org">
          <span class="status-dot" aria-hidden="true" />
          {{ orgLine }}
        </div>
      </header>
      <main class="content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

const menu = [
  { to: "/admin/dashboard", label: "数据大屏 / 仪表盘" },
  { to: "/admin/health", label: "健康监测 & 预警" },
  { to: "/admin/dispatch", label: "服务派单管理" },
  { to: "/admin/family", label: "亲情互动看板" },
  { to: "/admin/reports", label: "数据管理 / 报表" },
  { to: "/admin/pending-calls", label: "外呼升级（待处理）" },
  { to: "/admin/portal-content", label: "首页内容管理" }
];

const displayName = computed(() => localStorage.getItem("elder.userName") ?? "管理员");
const orgLine = computed(() => localStorage.getItem("elder.adminOrg") ?? "养老中心 · 总院");

onMounted(() => {
  if (localStorage.getItem("elder.role") !== "ADMIN") {
    router.replace("/login");
  }
});
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: flex;
  background: #f0f2f5;
}

.sidebar {
  width: 232px;
  flex-shrink: 0;
  background: linear-gradient(180deg, #1a2332 0%, #243447 100%);
  color: #fff;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
}

.brand {
  padding: 0 20px 24px;
  font-weight: 1000;
  font-size: 15px;
  line-height: 1.4;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  margin-bottom: 12px;
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 0 10px;
}

.nav-item {
  display: block;
  padding: 12px 14px;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.78);
  text-decoration: none;
  font-weight: 800;
  font-size: 14px;
  transition: background 0.15s, color 0.15s;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}

.nav-item.active {
  background: rgba(64, 158, 255, 0.22);
  color: #79bbff;
}

.main-col {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.top-bar {
  height: 56px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.welcome {
  font-weight: 900;
  font-size: 15px;
  color: rgba(0, 0, 0, 0.78);
}

.org {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 800;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.55);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f56c6c;
  flex-shrink: 0;
}

.content {
  flex: 1;
  padding: 20px 24px 32px;
  overflow: auto;
}
</style>
