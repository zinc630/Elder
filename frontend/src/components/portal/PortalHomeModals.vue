<template>
  <!-- 活动 -->
  <PortalOverlay
    :open="activityOpen"
    :title="activity?.title ?? ''"
    :subtitle="activity ? `${activity.timeLabel} · ${activity.location}` : ''"
    :emoji="activity?.icon"
    @close="closeActivity"
  >
    <template v-if="activity">
      <span class="portal-tag">{{ activity.tag }}</span>
      <p class="portal-desc">{{ activity.description }}</p>
      <p class="portal-meta">剩余名额 <strong>{{ activity.remaining }}</strong> / {{ activity.capacity }}</p>
      <div v-if="!activity.enrolled && activity.open" class="portal-form">
        <label>姓名<input v-model="form.contactName" type="text" placeholder="请输入姓名" /></label>
        <label>手机<input v-model="form.contactPhone" type="tel" placeholder="11 位手机号" /></label>
        <label>备注<textarea v-model="form.note" rows="2" placeholder="可选" /></label>
      </div>
      <p v-else-if="activity.enrolled" class="portal-success">您已报名该活动</p>
      <p v-else class="portal-warn">当前无法报名</p>
    </template>
    <template #footer>
      <button type="button" class="portal-btn ghost" @click="closeActivity">关闭</button>
      <button
        v-if="activity && !activity.enrolled && activity.open"
        type="button"
        class="portal-btn primary"
        :disabled="busy"
        @click="submitActivity"
      >
        {{ busy ? "提交中…" : "确认报名" }}
      </button>
    </template>
  </PortalOverlay>

  <!-- 生活服务 -->
  <PortalOverlay
    :open="lifeOpen"
    :title="life?.title ?? ''"
    :subtitle="life?.priceHint"
    :emoji="life?.icon"
    @close="closeLife"
  >
    <template v-if="life">
      <p class="portal-desc">{{ life.description }}</p>
      <div class="portal-chips">
        <span v-for="f in life.features" :key="f">{{ f }}</span>
      </div>
      <p class="portal-meta">预计 {{ life.slaHours }} 小时内响应</p>
      <div v-if="!life.booked" class="portal-form">
        <label>姓名<input v-model="form.contactName" type="text" /></label>
        <label>手机<input v-model="form.contactPhone" type="tel" /></label>
        <label>期望服务时间<input v-model="form.preferredTime" type="text" placeholder="如：明天上午" /></label>
        <label>需求说明<textarea v-model="form.note" rows="2" placeholder="可选" /></label>
      </div>
      <p v-else class="portal-success">您已提交预约，请等待工作人员联系</p>
    </template>
    <template #footer>
      <button type="button" class="portal-btn ghost" @click="closeLife">关闭</button>
      <button v-if="life && !life.booked" type="button" class="portal-btn primary" :disabled="busy" @click="submitLife">
        {{ busy ? "提交中…" : "确认预约" }}
      </button>
    </template>
  </PortalOverlay>

  <!-- 学习赋能 · 视频 -->
  <PortalOverlay
    wide
    :open="courseOpen"
    :title="course?.title ?? ''"
    :subtitle="course ? `${course.category} · ${course.duration}` : ''"
    :emoji="course?.icon"
    @close="closeCourse"
  >
    <template v-if="course">
      <div class="portal-video-wrap">
        <video
          v-if="currentVideoUrl"
          :key="currentVideoUrl"
          ref="videoRef"
          class="portal-video"
          :src="currentVideoUrl"
          controls
          playsinline
          preload="metadata"
          @error="onVideoError"
        />
        
        <div v-else-if="videoLoadError" class="portal-video-placeholder portal-video-error">{{ videoLoadError }}</div>
        <div v-else class="portal-video-placeholder">暂无视频，请在管理端上传并保存课程</div>
      </div>
      <p class="portal-desc">{{ course.description }}</p>
      <div class="portal-video-meta">
        <span>🕐 {{ course.viewsLabel }}</span>
        <span>⭐ {{ course.rating }}</span>
      </div>
      <ul v-if="course.lessons.length > 1" class="portal-chapters">
        <li
          v-for="(lesson, idx) in course.lessons"
          :key="lesson.id"
          :class="{ active: activeLessonId === lesson.id }"
          @click="selectLesson(lesson.id)"
        >
          <span class="chapter-num">{{ idx + 1 }}</span>
          <div>
            <strong>{{ lesson.title }}</strong>
            <span class="lesson-dur">{{ lesson.durationMinutes }} 分钟</span>
          </div>
        </li>
      </ul>
      <p v-if="activeLesson" class="portal-lesson-desc">{{ activeLesson.content }}</p>
    </template>
    <template #footer>
      <button type="button" class="portal-btn primary" @click="closeCourse">关闭</button>
    </template>
  </PortalOverlay>

  <!-- 新闻 -->
  <PortalOverlay
    :open="newsOpen"
    :title="news?.title ?? ''"
    :subtitle="news ? `${news.source} · ${news.publishDate}` : ''"
    :emoji="news?.icon"
    @close="closeNews"
  >
    <template v-if="news">
      <img v-if="news.imageUrl" :src="resolveMediaUrl(news.imageUrl)" class="portal-news-cover" alt="" />
      <p class="portal-meta">阅读 {{ news.viewsLabel }}</p>
      <p class="portal-desc lead">{{ news.summary }}</p>
      <article class="portal-article">{{ news.body }}</article>
    </template>
    <template #footer>
      <button type="button" class="portal-btn primary" @click="closeNews">关闭</button>
    </template>
  </PortalOverlay>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import PortalOverlay from "./PortalOverlay.vue";
import type {
  PortalActivity,
  PortalCourseDetail,
  PortalEnrollPayload,
  PortalLifeService,
  PortalNewsDetail
} from "../../api/portal";
import {
  bookLifeService,
  enrollActivity,
  fetchActivity,
  fetchCourseDetail,
  fetchNewsDetail
} from "../../api/portal";
import { getDefaultContactName } from "../../utils/portalActor";
import { resolveMediaUrl } from "../../utils/mediaUrl";

const emit = defineEmits<{
  refresh: [];
  toast: [message: string];
}>();

const activityOpen = ref(false);
const lifeOpen = ref(false);
const courseOpen = ref(false);
const newsOpen = ref(false);
const busy = ref(false);

const activity = ref<PortalActivity | null>(null);
const life = ref<PortalLifeService | null>(null);
const course = ref<PortalCourseDetail | null>(null);
const news = ref<PortalNewsDetail | null>(null);
const activeLessonId = ref<string | null>(null);
const videoRef = ref<HTMLVideoElement | null>(null);
const videoLoadError = ref("");

const activeLesson = computed(() =>
  course.value?.lessons.find((l) => l.id === activeLessonId.value) ?? null
);

const currentVideoUrl = computed(() => {
  const lesson = activeLesson.value;
  if (!lesson?.videoUrl?.trim()) return "";
  return resolveMediaUrl(lesson.videoUrl);
});

function onVideoError() {
  videoLoadError.value = "视频无法播放，请确认已在管理端保存课程，或更换 H.264 编码的 MP4 文件";
}

function selectLesson(lessonId: string) {
  videoLoadError.value = "";
  activeLessonId.value = lessonId;
}

const form = reactive<PortalEnrollPayload>({
  contactName: getDefaultContactName(),
  contactPhone: "",
  note: "",
  preferredTime: ""
});

function resetForm() {
  form.contactName = getDefaultContactName();
  form.contactPhone = "";
  form.note = "";
  form.preferredTime = "";
}

function validateContact(): boolean {
  if (!form.contactName.trim()) {
    emit("toast", "请填写姓名");
    return false;
  }
  if (!/^1\d{10}$/.test(form.contactPhone.trim())) {
    emit("toast", "请填写正确的 11 位手机号");
    return false;
  }
  return true;
}

async function openActivity(item: PortalActivity) {
  resetForm();
  activity.value = item;
  activityOpen.value = true;
  try {
    activity.value = await fetchActivity(item.id);
  } catch {
    /* 使用首页已加载的数据展示弹窗，提交报名仍走后端 */
  }
}

function closeActivity() {
  activityOpen.value = false;
}

async function submitActivity() {
  if (!activity.value || !validateContact()) return;
  busy.value = true;
  try {
    const res = await enrollActivity(activity.value.id, {
      contactName: form.contactName.trim(),
      contactPhone: form.contactPhone.trim(),
      note: form.note?.trim()
    });
    emit("toast", res.message);
    if (res.success) {
      activity.value = await fetchActivity(activity.value.id);
      emit("refresh");
    }
  } catch (e) {
    const msg = e instanceof Error ? e.message : "报名失败";
    emit("toast", msg);
  } finally {
    busy.value = false;
  }
}

function openLife(item: PortalLifeService) {
  resetForm();
  life.value = { ...item };
  lifeOpen.value = true;
}

function closeLife() {
  lifeOpen.value = false;
}

async function submitLife() {
  if (!life.value || !validateContact()) return;
  busy.value = true;
  try {
    const res = await bookLifeService(life.value.id, {
      contactName: form.contactName.trim(),
      contactPhone: form.contactPhone.trim(),
      note: form.note?.trim(),
      preferredTime: form.preferredTime?.trim()
    });
    emit("toast", res.message);
    if (res.success) {
      life.value = { ...life.value, booked: true };
      emit("refresh");
    }
  } catch (e) {
    emit("toast", e instanceof Error ? e.message : "预约失败");
  } finally {
    busy.value = false;
  }
}

async function openCourse(item: { id: string }) {
  courseOpen.value = true;
  activeLessonId.value = null;
  videoLoadError.value = "";
  try {
    course.value = await fetchCourseDetail(item.id);
    activeLessonId.value = course.value.lessons[0]?.id ?? null;
  } catch (e) {
    courseOpen.value = false;
    emit("toast", e instanceof Error ? e.message : "无法加载视频");
  }
}

function closeCourse() {
  courseOpen.value = false;
  videoLoadError.value = "";
  if (videoRef.value) videoRef.value.pause();
}

async function openNews(item: { id: string }) {
  news.value = await fetchNewsDetail(item.id);
  newsOpen.value = true;
}

function closeNews() {
  newsOpen.value = false;
}

defineExpose({ openActivity, openLife, openCourse, openNews });
</script>

<style scoped>
.portal-tag {
  display: inline-block;
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1f6aa5;
  margin-bottom: 10px;
}
.portal-desc {
  margin-bottom: 12px;
  white-space: pre-wrap;
}
.portal-desc.lead {
  color: #64748b;
  font-size: 13px;
}
.portal-news-cover {
  width: 100%;
  max-height: 200px;
  object-fit: cover;
  border-radius: 10px;
  margin-bottom: 12px;
}
.portal-meta {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 12px;
}
.portal-meta strong {
  color: #1f6aa5;
}
.portal-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 8px;
}
.portal-form.compact {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed #e2e8f0;
}
.portal-form label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #64748b;
}
.portal-form input,
.portal-form textarea {
  padding: 9px 11px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
}
.portal-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}
.portal-chips span {
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 999px;
  background: #f8fafc;
  border: 1px solid #f1f5f9;
}
.portal-success {
  color: #059669;
  font-size: 13px;
  padding: 10px;
  background: #ecfdf5;
  border-radius: 8px;
}
.portal-warn {
  color: #b45309;
  font-size: 13px;
}
.portal-hint {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 8px;
}
.portal-video-wrap {
  margin-bottom: 14px;
  border-radius: 12px;
  overflow: hidden;
  background: #0f172a;
  aspect-ratio: 16 / 9;
}
.portal-video {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: contain;
}
.portal-video-error {
  color: #b91c1c;
}
.portal-video-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 200px;
  color: #94a3b8;
  font-size: 14px;
}
.portal-video-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 12px;
}
.portal-chapters {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}
.portal-chapters li {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 10px 12px;
  border: 1px solid #f1f5f9;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s;
}
.portal-chapters li:hover {
  background: #f8fafc;
}
.portal-chapters li.active {
  background: #eff6ff;
  border-color: #bfdbfe;
}
.portal-chapters .chapter-num {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #e2e8f0;
  color: #475569;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.portal-chapters li.active .chapter-num {
  background: #1f6aa5;
  color: #fff;
}
.portal-chapters strong {
  display: block;
  font-size: 13px;
  color: #1a3347;
  margin-bottom: 2px;
}
.portal-lesson-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.65;
  padding-top: 8px;
  border-top: 1px solid #f1f5f9;
}
.lesson-dur {
  font-size: 11px;
  color: #94a3b8;
}
.portal-article {
  white-space: pre-wrap;
  font-size: 14px;
  line-height: 1.75;
  color: #334155;
}
.portal-btn {
  padding: 10px 18px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  font-family: inherit;
}
.portal-btn.primary {
  background: #1f6aa5;
  color: #fff;
}
.portal-btn.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.portal-btn.ghost {
  background: #fff;
  border: 1px solid #e2e8f0;
  color: #475569;
}
.portal-btn.sm {
  padding: 6px 12px;
  font-size: 11px;
  flex-shrink: 0;
  background: #eff6ff;
  color: #1f6aa5;
  border: 1px solid #bfdbfe;
}
.portal-btn.sm:disabled {
  opacity: 0.5;
}
</style>
