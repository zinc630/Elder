<template>
  <button class="big-btn" @click="$emit('click')">
    <slot />
  </button>
</template>

<script setup lang="ts">
defineEmits<{
  (e: "click"): void;
}>();
</script>

<style scoped>
.big-btn {
  width: 100%;
  padding: 18px 14px;
  font-size: 20px;
  font-weight: 700;
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: #1976d2;
  color: white;
}
</style>

