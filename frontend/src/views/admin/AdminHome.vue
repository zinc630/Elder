<template>
  <div class="page">
    <h2 class="section-title">管理员端（Web 管理后台示例）</h2>

    <div class="grid">
      <button class="primary" @click="router.push('/admin/pending')">
        待处理告警（呼叫升级）
      </button>
    </div>

    <div class="card">
      <div class="muted">说明：电话外呼能力在框架中以“管理员模拟回执”方式演示。</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
const router = useRouter();
</script>

<style scoped>
.grid { display: grid; gap: 12px; }
.primary {
  padding: 16px;
  border-radius: 16px;
  border: 0;
  background: #1976d2;
  color: white;
  font-weight: 900;
  font-size: 18px;
  cursor: pointer;
}
.card {
  margin-top: 12px;
  padding: 14px;
  border-radius: 14px;
  background: white;
  border: 1px solid rgba(0,0,0,0.08);
}
.muted { color: rgba(0,0,0,0.6); font-weight: 800; }
</style>

