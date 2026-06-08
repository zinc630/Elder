<template>
  <Teleport to="body">
    <div v-if="open" class="portal-overlay" @click.self="emit('close')">
      <div class="portal-modal" :class="{ wide }" role="dialog" :aria-label="title">
        <header class="portal-modal-head">
          <div class="portal-modal-title">
            <span v-if="emoji" class="portal-emoji">{{ emoji }}</span>
            <div>
              <h3>{{ title }}</h3>
              <p v-if="subtitle" class="portal-sub">{{ subtitle }}</p>
            </div>
          </div>
          <button type="button" class="portal-close" aria-label="关闭" @click="emit('close')">×</button>
        </header>

        <div class="portal-modal-body">
          <slot />
        </div>

        <footer v-if="$slots.footer" class="portal-modal-foot">
          <slot name="footer" />
        </footer>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
defineProps<{
  open: boolean;
  title: string;
  subtitle?: string;
  emoji?: string;
  /** 视频等大内容弹窗 */
  wide?: boolean;
}>();

const emit = defineEmits<{ close: [] }>();
</script>

<style scoped>
.portal-overlay {
  position: fixed;
  inset: 0;
  z-index: 2100;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.portal-modal.wide {
  max-width: 720px;
}
.portal-modal {
  background: #fff;
  border-radius: 18px;
  width: 100%;
  max-width: 520px;
  max-height: 88vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.18);
  animation: fadeInUp 0.25s ease;
}
.portal-modal-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 20px;
  border-bottom: 1px solid #f1f5f9;
}
.portal-modal-title {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}
.portal-modal-title h3 {
  font-size: 17px;
  font-weight: 700;
  color: #1a3347;
  line-height: 1.35;
}
.portal-sub {
  font-size: 12px;
  color: #7b95a8;
  margin-top: 4px;
}
.portal-emoji {
  font-size: 32px;
  line-height: 1;
}
.portal-close {
  width: 34px;
  height: 34px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
  font-size: 22px;
  line-height: 1;
  color: #94a3b8;
  cursor: pointer;
}
.portal-close:hover {
  background: #f1f5f9;
  color: #1a3347;
}
.portal-modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  font-size: 14px;
  color: #475569;
  line-height: 1.65;
}
.portal-modal-foot {
  padding: 12px 20px 18px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
