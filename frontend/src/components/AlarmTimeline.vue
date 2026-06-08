<template>
  <div class="card">
    <div class="row">
      <div class="label">告警类型</div>
      <div class="value">{{ alarm.type }}</div>
    </div>
    <div class="row">
      <div class="label">状态</div>
      <div class="value">{{ alarm.status }}</div>
    </div>
    <div class="row">
      <div class="label">触发时间</div>
      <div class="value">{{ alarm.triggeredAt }}</div>
    </div>
    <div v-if="alarm.riskScore != null" class="row">
      <div class="label">风险分</div>
      <div class="value">{{ alarm.riskScore }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { AlarmDto } from "../api/alarm";

defineProps<{
  alarm: AlarmDto;
}>();
</script>

<style scoped>
.card {
  padding: 14px;
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 14px;
}
.row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}
.label {
  color: rgba(0, 0, 0, 0.6);
  font-size: 13px;
}
.value {
  font-size: 14px;
  text-align: right;
}
</style>

