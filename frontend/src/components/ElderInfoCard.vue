<template>
  <div v-if="profile" class="card">
    <div class="top">
      <div class="name">{{ profile.name }}</div>
      <div class="id">ID: {{ profile.elderId }}</div>
    </div>
    <div class="row">
      <div class="k">年龄</div>
      <div class="v">{{ profile.age ?? "-" }}</div>
    </div>
    <div class="row">
      <div class="k">性别</div>
      <div class="v">{{ profile.gender }}</div>
    </div>
    <div class="row">
      <div class="k">地址</div>
      <div class="v">{{ profile.address }}</div>
    </div>
    <div class="row" v-if="profile.keyHealthNotes">
      <div class="k">关键健康</div>
      <div class="v">{{ profile.keyHealthNotes }}</div>
    </div>
  </div>
  <div v-else class="card muted">加载老人信息...</div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from "vue";
import type { ElderProfileDto } from "../api/elder";
import { getElderProfile } from "../api/elder";

const elderId = ref(localStorage.getItem("elder.id") ?? "");
const profile = ref<ElderProfileDto | null>(null);

async function load() {
  if (!elderId.value) return;
  profile.value = await getElderProfile(elderId.value);
}

onMounted(() => {
  void load();
});

const handler = (ev: Event) => {
  const detail = (ev as CustomEvent).detail as any;
  const nextId = detail?.elderId;
  if (!nextId) return;
  elderId.value = nextId;
  void load();
};

onUnmounted(() => {
  window.removeEventListener("elder-switched", handler);
});

window.addEventListener("elder-switched", handler);
</script>

<style scoped>
.card {
  background: white;
  border-radius: 14px;
  padding: 14px;
  border: 1px solid rgba(0, 0, 0, 0.08);
}
.muted {
  color: rgba(0, 0, 0, 0.5);
  font-weight: 800;
}
.top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}
.name {
  font-weight: 1000;
  font-size: 18px;
}
.id {
  color: rgba(0, 0, 0, 0.55);
  font-weight: 800;
  font-size: 12px;
  text-align: right;
}
.row {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin-top: 6px;
}
.k {
  color: rgba(0, 0, 0, 0.6);
  font-weight: 800;
  font-size: 13px;
  white-space: nowrap;
}
.v {
  font-weight: 900;
  font-size: 13px;
  text-align: right;
  flex: 1;
}
</style>

