<template>
  <div class="portal-admin">
    <h1 class="page-title">首页内容管理</h1>
    <p class="sub">管理功能首页的文娱活动、学习赋能课程、新闻资讯（文字、封面图、课程视频）。已配置 MySQL 时自动写入 elder 库；未配置时仍使用本地 JSON 文件。</p>

    
    <div v-if="loadError" class="msg err">{{ loadError }}</div>
    <div v-if="msg" class="msg" :class="msgOk ? 'ok' : 'err'">{{ msg }}</div>
    <div v-if="loading" class="loading-hint">加载中…</div>

    <div class="tabs">
      <button
        v-for="t in tabs"
        :key="t.id"
        type="button"
        class="tab"
        :class="{ active: tab === t.id }"
        @click="switchTab(t.id)"
      >
        {{ t.label }}
      </button>
    </div>

    <div v-show="tab === 'activity'" class="panel">
      <div class="toolbar">
        <button type="button" class="btn primary" @click="newActivity">新建活动</button>
        <button type="button" class="btn" :disabled="busy" @click="loadAll">刷新</button>
        <button type="button" class="btn" :disabled="busy" @click="importDemo">导入示例数据</button>
      </div>
      <div class="split">
        <ul class="list">
          <li v-if="!activities.length" class="list-empty">暂无活动，可「导入示例数据」或「新建活动」</li>
          <li
            v-for="a in activities"
            :key="a.id"
            :class="{ active: activityForm.id === a.id && !activityIsNew }"
            @click="selectActivity(a)"
          >
            <strong>{{ a.title }}</strong>
            <span>{{ a.tag }} · {{ a.timeLabel }}</span>
          </li>
        </ul>
        <div v-if="!(activityForm.id || activityIsNew)" class="form card form-placeholder">
          <p>请从左侧选择一条活动，或点击「新建活动」开始编辑。</p>
          <p class="hint">支持修改文字、上传封面图片；保存后同步到功能首页。</p>
        </div>
        <div v-if="activityForm.id || activityIsNew" class="form card">
          <h2>{{ activityIsNew ? "新建活动" : "编辑活动" }}</h2>
          <label>图标（Emoji）<input v-model="activityForm.icon" type="text" /></label>
          <label>分类标签<input v-model="activityForm.tag" type="text" /></label>
          <label>标题<input v-model="activityForm.title" type="text" /></label>
          <label>时间<input v-model="activityForm.timeLabel" type="text" placeholder="如 2024.12.25 14:00" /></label>
          <label>地点<input v-model="activityForm.location" type="text" /></label>
          <label>简介<textarea v-model="activityForm.description" rows="3" /></label>
          <label>名额<input v-model.number="activityForm.capacity" type="number" min="1" /></label>
          <label class="row-check"><input v-model="activityForm.open" type="checkbox" />开放报名</label>
          <div class="upload-row">
            <label>封面图</label>
            
            <div class="upload-actions">
              <input ref="activityImgInput" type="file" accept="image/*" hidden @change="onActivityImage" />
              <button type="button" class="btn sm" @click="activityImgInput?.click()">上传图片</button>
              <button v-if="activityForm.imageUrl" type="button" class="btn sm ghost" @click="activityForm.imageUrl = null">清除</button>
            </div>
            <img v-if="activityForm.imageUrl" :src="resolveMediaUrl(activityForm.imageUrl)" class="preview" alt="" />
          </div>
          <div class="form-actions">
            <button type="button" class="btn primary" :disabled="busy" @click="saveActivity">保存</button>
            <button v-if="!activityIsNew" type="button" class="btn danger" :disabled="busy" @click="removeActivity">删除</button>
          </div>
        </div>
      </div>
    </div>

    <div v-show="tab === 'course'" class="panel">
      <div class="toolbar">
        <button type="button" class="btn primary" @click="newCourse">新建课程</button>
        <button type="button" class="btn" :disabled="busy" @click="loadAll">刷新</button>
        <button type="button" class="btn" :disabled="busy" @click="importDemo">导入示例数据</button>
      </div>
      <div class="split">
        <ul class="list">
          <li v-if="!courses.length" class="list-empty">暂无课程，可「导入示例数据」或「新建课程」</li>
          <li
            v-for="c in courses"
            :key="c.id"
            :class="{ active: courseForm.id === c.id && !courseIsNew }"
            @click="selectCourse(c)"
          >
            <strong>{{ c.title }}</strong>
            <span>{{ c.category }} · {{ c.duration }}</span>
          </li>
        </ul>
        <div v-if="!(courseForm.id || courseIsNew)" class="form card form-placeholder">
          <p>请从左侧选择课程，或点击「新建课程」。</p>
          <p class="hint">可上传封面图、各课时视频及文字说明。</p>
        </div>
        <div v-if="courseForm.id || courseIsNew" class="form card">
          <h2>{{ courseIsNew ? "新建课程" : "编辑课程" }}</h2>
          <label>图标<input v-model="courseForm.icon" type="text" /></label>
          <label>分类<input v-model="courseForm.category" type="text" /></label>
          <label>标题<input v-model="courseForm.title" type="text" /></label>
          <label>时长文案<input v-model="courseForm.duration" type="text" placeholder="如 30分钟" /></label>
          <label>评分<input v-model.number="courseForm.rating" type="number" min="0" max="5" step="0.1" /></label>
          <label>简介<textarea v-model="courseForm.description" rows="2" /></label>
          <div class="upload-row">
            <label>封面图</label>
            <div class="upload-actions">
              <input ref="courseImgInput" type="file" accept="image/*" hidden @change="onCourseImage" />
              <button type="button" class="btn sm" @click="courseImgInput?.click()">上传图片</button>
              <button v-if="courseForm.imageUrl" type="button" class="btn sm ghost" @click="courseForm.imageUrl = null">清除</button>
            </div>
            <img v-if="courseForm.imageUrl" :src="resolveMediaUrl(courseForm.imageUrl)" class="preview" alt="" />
          </div>
          <h3 class="sub-h">课时 / 视频</h3>
          
          <div v-for="(lesson, idx) in courseForm.lessons" :key="lesson.id || idx" class="lesson-block">
            <div class="lesson-head">
              <strong>第 {{ idx + 1 }} 节</strong>
              <button v-if="courseForm.lessons.length > 1" type="button" class="btn sm ghost" @click="removeLesson(idx)">删除</button>
            </div>
            <label>标题<input v-model="lesson.title" type="text" /></label>
            <label>说明<textarea v-model="lesson.content" rows="2" /></label>
            <label>时长（分钟）<input v-model.number="lesson.durationMinutes" type="number" min="1" /></label>
            <div class="upload-row">
              <label>视频</label>
              <div class="upload-actions">
                <input
                  :ref="(el) => setLessonVideoInput(idx, el as HTMLInputElement | null)"
                  type="file"
                  accept="video/mp4,video/webm,video/quicktime"
                  hidden
                  @change="(e) => onLessonVideo(idx, e)"
                />
                <button type="button" class="btn sm" @click="lessonVideoInput[idx]?.click()">上传视频</button>
                <button v-if="lesson.videoUrl" type="button" class="btn sm ghost" @click="lesson.videoUrl = null">清除</button>
              </div>
              <p v-if="lesson.videoUrl" class="url-hint">{{ lesson.videoUrl }}</p>
              <video
                v-if="lesson.videoUrl"
                class="preview preview-video"
                :src="resolveMediaUrl(lesson.videoUrl)"
                controls
                playsinline
                preload="metadata"
              />
              <p v-if="lesson.videoUrl" class="save-hint">上传后请点击下方「保存」，首页才能播放该视频</p>
            </div>
          </div>
          <button type="button" class="btn sm" @click="addLesson">+ 添加课时</button>
          <div class="form-actions">
            <button type="button" class="btn primary" :disabled="busy" @click="saveCourse">保存</button>
            <button v-if="!courseIsNew" type="button" class="btn danger" :disabled="busy" @click="removeCourse">删除</button>
          </div>
        </div>
      </div>
    </div>

    <div v-show="tab === 'news'" class="panel">
      <div class="toolbar">
        <button type="button" class="btn primary" @click="newNews">新建资讯</button>
        <button type="button" class="btn" :disabled="busy" @click="loadAll">刷新</button>
        <button type="button" class="btn" :disabled="busy" @click="importDemo">导入示例数据</button>
      </div>
      <div class="split">
        <ul class="list">
          <li v-if="!newsList.length" class="list-empty">暂无资讯，可「导入示例数据」或「新建资讯」</li>
          <li
            v-for="n in newsList"
            :key="n.id"
            :class="{ active: newsForm.id === n.id && !newsIsNew }"
            @click="selectNews(n)"
          >
            <strong>{{ n.title }}</strong>
            <span>{{ n.publishDate }} · {{ n.source }}</span>
          </li>
        </ul>
        <div v-if="!(newsForm.id || newsIsNew)" class="form card form-placeholder">
          <p>请从左侧选择资讯，或点击「新建资讯」。</p>
          <p class="hint">支持正文、摘要及封面图上传。</p>
        </div>
        <div v-if="newsForm.id || newsIsNew" class="form card">
          <h2>{{ newsIsNew ? "新建资讯" : "编辑资讯" }}</h2>
          <label>图标<input v-model="newsForm.icon" type="text" /></label>
          <label>标题<input v-model="newsForm.title" type="text" /></label>
          <label>摘要<textarea v-model="newsForm.summary" rows="2" /></label>
          <label>发布日期<input v-model="newsForm.publishDate" type="text" placeholder="YYYY-MM-DD" /></label>
          <label>来源<input v-model="newsForm.source" type="text" /></label>
          <label>正文<textarea v-model="newsForm.body" rows="8" /></label>
          <div class="upload-row">
            <label>封面图</label>
            <div class="upload-actions">
              <input ref="newsImgInput" type="file" accept="image/*" hidden @change="onNewsImage" />
              <button type="button" class="btn sm" @click="newsImgInput?.click()">上传图片</button>
              <button v-if="newsForm.imageUrl" type="button" class="btn sm ghost" @click="newsForm.imageUrl = null">清除</button>
            </div>
            <img v-if="newsForm.imageUrl" :src="resolveMediaUrl(newsForm.imageUrl)" class="preview" alt="" />
          </div>
          <div class="form-actions">
            <button type="button" class="btn primary" :disabled="busy" @click="saveNews">保存</button>
            <button v-if="!newsIsNew" type="button" class="btn danger" :disabled="busy" @click="removeNews">删除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import {
  deleteAdminActivity,
  deleteAdminCourse,
  deleteAdminNews,
  listAdminActivities,
  listAdminCourses,
  listAdminNews,
  saveAdminActivity,
  saveAdminCourse,
  saveAdminNews,
  seedDemoContent,
  uploadPortalMedia,
  type ActivityAdmin,
  type CourseAdmin,
  type LessonAdmin,
  type NewsAdmin
} from "../../api/adminHomeContent";
import { resolveMediaUrl } from "../../utils/mediaUrl";

type TabId = "activity" | "course" | "news";

const tabs = [
  { id: "activity" as TabId, label: "文娱活动" },
  { id: "course" as TabId, label: "学习赋能" },
  { id: "news" as TabId, label: "新闻资讯" }
];

const tab = ref<TabId>("activity");
const busy = ref(false);
const loading = ref(false);
const loadError = ref("");
const msg = ref("");
const msgOk = ref(true);

const activities = ref<ActivityAdmin[]>([]);
const courses = ref<CourseAdmin[]>([]);
const newsList = ref<NewsAdmin[]>([]);

const activityIsNew = ref(false);
const courseIsNew = ref(false);
const newsIsNew = ref(false);

const activityImgInput = ref<HTMLInputElement | null>(null);
const courseImgInput = ref<HTMLInputElement | null>(null);
const newsImgInput = ref<HTMLInputElement | null>(null);
const lessonVideoInput = ref<Record<number, HTMLInputElement | null>>({});

function setLessonVideoInput(idx: number, el: HTMLInputElement | null) {
  lessonVideoInput.value[idx] = el;
}

const emptyActivity = (): ActivityAdmin => ({
  id: "",
  icon: "\uD83C\uDFAD",
  tag: "活动",
  title: "",
  timeLabel: "",
  location: "",
  description: "",
  capacity: 50,
  enrolledCount: 0,
  open: true,
  imageUrl: null
});

const emptyLesson = (): LessonAdmin => ({
  id: "",
  title: "第 1 节",
  content: "",
  durationMinutes: 10,
  videoUrl: null
});

const emptyCourse = (): CourseAdmin => ({
  id: "",
  icon: "\uD83D\uDCDA",
  category: "课程",
  title: "",
  description: "",
  duration: "30分钟",
  viewCount: 0,
  rating: 4.8,
  lessons: [emptyLesson()],
  imageUrl: null
});

const emptyNews = (): NewsAdmin => ({
  id: "",
  icon: "\uD83D\uDCF0",
  title: "",
  summary: "",
  publishDate: new Date().toISOString().slice(0, 10),
  source: "",
  body: "",
  viewCount: 0,
  imageUrl: null
});

const activityForm = reactive<ActivityAdmin>(emptyActivity());
const courseForm = reactive<CourseAdmin>(emptyCourse());
const newsForm = reactive<NewsAdmin>(emptyNews());

function flash(text: string, ok = true) {
  msg.value = text;
  msgOk.value = ok;
  setTimeout(() => { msg.value = ""; }, 3500);
}

function switchTab(id: TabId) { tab.value = id; }

async function loadActivities() { activities.value = await listAdminActivities(); }
async function loadCourses() { courses.value = await listAdminCourses(); }
async function loadNews() { newsList.value = await listAdminNews(); }

async function loadAll() {
  loadError.value = "";
  loading.value = true;
  try {
    await Promise.all([loadActivities(), loadCourses(), loadNews()]);
  } catch (e) {
    const text = e instanceof Error ? e.message : "加载失败";
    loadError.value = text;
    flash(text, false);
  } finally {
    loading.value = false;
  }
}

async function importDemo() {
  if (!confirm("将导入示例文娱活动、课程与资讯（已有数据会被覆盖），是否继续？")) return;
  busy.value = true;
  loadError.value = "";
  try {
    await seedDemoContent();
    await loadAll();
    flash("示例数据已导入");
  } catch (e) {
    const text = e instanceof Error ? e.message : "导入失败";
    loadError.value = text;
    flash(text, false);
  } finally {
    busy.value = false;
  }
}

function selectActivity(a: ActivityAdmin) {
  activityIsNew.value = false;
  Object.assign(activityForm, { ...a });
}
function newActivity() {
  activityIsNew.value = true;
  Object.assign(activityForm, emptyActivity());
}
async function onActivityImage(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0];
  if (!file) return;
  busy.value = true;
  try {
    activityForm.imageUrl = (await uploadPortalMedia(file, "image")).url;
    flash("图片已上传");
  } catch (err) {
    flash(err instanceof Error ? err.message : "上传失败", false);
  } finally {
    busy.value = false;
    (e.target as HTMLInputElement).value = "";
  }
}
async function saveActivity() {
  busy.value = true;
  try {
    const saved = await saveAdminActivity(activityForm, activityIsNew.value);
    flash("活动已保存");
    activityIsNew.value = false;
    Object.assign(activityForm, saved);
    await loadActivities();
  } catch (err) {
    flash(err instanceof Error ? err.message : "保存失败", false);
  } finally {
    busy.value = false;
  }
}
async function removeActivity() {
  if (!activityForm.id || !confirm("确定删除该活动？")) return;
  busy.value = true;
  try {
    await deleteAdminActivity(activityForm.id);
    flash("已删除");
    Object.assign(activityForm, emptyActivity());
    activityForm.id = "";
    await loadActivities();
  } catch (err) {
    flash(err instanceof Error ? err.message : "保存失败", false);
  } finally {
    busy.value = false;
  }
}

function selectCourse(c: CourseAdmin) {
  courseIsNew.value = false;
  Object.assign(courseForm, JSON.parse(JSON.stringify(c)));
}
function newCourse() {
  courseIsNew.value = true;
  Object.assign(courseForm, emptyCourse());
}
function addLesson() { courseForm.lessons.push(emptyLesson()); }
function removeLesson(idx: number) { courseForm.lessons.splice(idx, 1); }
async function onCourseImage(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0];
  if (!file) return;
  busy.value = true;
  try {
    courseForm.imageUrl = (await uploadPortalMedia(file, "image")).url;
    flash("封面已上传");
  } catch (err) {
    flash(err instanceof Error ? err.message : "上传失败", false);
  } finally {
    busy.value = false;
    (e.target as HTMLInputElement).value = "";
  }
}
async function onLessonVideo(idx: number, e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0];
  if (!file) return;
  busy.value = true;
  try {
    courseForm.lessons[idx].videoUrl = (await uploadPortalMedia(file, "video")).url;
    flash("视频已上传，请点击「保存」同步到首页");
  } catch (err) {
    flash(err instanceof Error ? err.message : "上传失败", false);
  } finally {
    busy.value = false;
    (e.target as HTMLInputElement).value = "";
  }
}
async function saveCourse() {
  busy.value = true;
  try {
    const saved = await saveAdminCourse(courseForm, courseIsNew.value);
    flash("课程已保存");
    courseIsNew.value = false;
    Object.assign(courseForm, JSON.parse(JSON.stringify(saved)));
    await loadCourses();
  } catch (err) {
    flash(err instanceof Error ? err.message : "保存失败", false);
  } finally {
    busy.value = false;
  }
}
async function removeCourse() {
  if (!courseForm.id || !confirm("确定删除该课程？")) return;
  busy.value = true;
  try {
    await deleteAdminCourse(courseForm.id);
    flash("已删除");
    Object.assign(courseForm, emptyCourse());
    courseForm.id = "";
    await loadCourses();
  } catch (err) {
    flash(err instanceof Error ? err.message : "保存失败", false);
  } finally {
    busy.value = false;
  }
}

function selectNews(n: NewsAdmin) {
  newsIsNew.value = false;
  Object.assign(newsForm, { ...n });
}
function newNews() {
  newsIsNew.value = true;
  Object.assign(newsForm, emptyNews());
}
async function onNewsImage(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0];
  if (!file) return;
  busy.value = true;
  try {
    newsForm.imageUrl = (await uploadPortalMedia(file, "image")).url;
    flash("图片已上传");
  } catch (err) {
    flash(err instanceof Error ? err.message : "上传失败", false);
  } finally {
    busy.value = false;
    (e.target as HTMLInputElement).value = "";
  }
}
async function saveNews() {
  busy.value = true;
  try {
    const saved = await saveAdminNews(newsForm, newsIsNew.value);
    flash("资讯已保存");
    newsIsNew.value = false;
    Object.assign(newsForm, saved);
    await loadNews();
  } catch (err) {
    flash(err instanceof Error ? err.message : "保存失败", false);
  } finally {
    busy.value = false;
  }
}
async function removeNews() {
  if (!newsForm.id || !confirm("确定删除该资讯？")) return;
  busy.value = true;
  try {
    await deleteAdminNews(newsForm.id);
    flash("已删除");
    Object.assign(newsForm, emptyNews());
    newsForm.id = "";
    await loadNews();
  } catch (err) {
    flash(err instanceof Error ? err.message : "保存失败", false);
  } finally {
    busy.value = false;
  }
}

onMounted(() => { void loadAll(); });
</script>

<style scoped>
.portal-admin { max-width: 1200px; }
.page-title { font-size: 22px; font-weight: 900; margin-bottom: 6px; }
.sub { color: rgba(0,0,0,.55); font-size: 14px; margin-bottom: 16px; }
.loading-hint { font-size: 14px; color: #64748b; margin-bottom: 12px; }
.msg { padding: 10px 14px; border-radius: 8px; margin-bottom: 12px; font-size: 14px; }
.msg.ok { background: #ecfdf5; color: #047857; }
.msg.err { background: #fef2f2; color: #b91c1c; }
.tabs { display: flex; gap: 8px; margin-bottom: 16px; }
.tab { padding: 8px 16px; border-radius: 8px; border: 1px solid #e2e8f0; background: #fff; cursor: pointer; font-weight: 700; }
.tab.active { background: #1f6aa5; color: #fff; border-color: #1f6aa5; }
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.split { display: grid; grid-template-columns: 280px 1fr; gap: 16px; align-items: start; }
.list { list-style: none; margin: 0; padding: 0; background: #fff; border-radius: 10px; border: 1px solid #e2e8f0; max-height: 520px; overflow-y: auto; }
.list li { padding: 12px 14px; border-bottom: 1px solid #f1f5f9; cursor: pointer; }
.list li.list-empty { cursor: default; color: #94a3b8; font-size: 13px; line-height: 1.5; }
.list li:hover:not(.list-empty) { background: #f8fafc; }
.list li.active { background: #eff6ff; }
.list strong { display: block; font-size: 14px; margin-bottom: 4px; }
.list span { font-size: 12px; color: #94a3b8; }
.card.form { background: #fff; border-radius: 10px; border: 1px solid #e2e8f0; padding: 16px 18px; }
.form-placeholder { color: #64748b; font-size: 14px; line-height: 1.6; }
.form-placeholder .hint { margin-top: 8px; font-size: 13px; color: #94a3b8; }
.card h2 { font-size: 16px; margin-bottom: 14px; }
.card label { display: block; font-size: 12px; color: #64748b; margin-bottom: 10px; }
.card input, .card textarea { width: 100%; margin-top: 4px; padding: 8px 10px; border: 1px solid #e2e8f0; border-radius: 6px; font-size: 14px; font-family: inherit; box-sizing: border-box; }
.row-check { display: flex !important; align-items: center; gap: 8px; flex-direction: row; }
.row-check input { width: auto; margin: 0; }
.upload-row { margin-bottom: 12px; }
.upload-actions { display: flex; gap: 8px; margin: 6px 0; }
.preview { max-width: 200px; max-height: 120px; border-radius: 8px; object-fit: cover; margin-top: 8px; }
.preview-video { max-width: 100%; max-height: 200px; width: 100%; object-fit: contain; background: #0f172a; margin-top: 8px; border-radius: 8px; }
.save-hint { font-size: 12px; color: #b45309; margin-top: 6px; }
.lesson-block { border: 1px dashed #e2e8f0; border-radius: 8px; padding: 12px; margin-bottom: 10px; }
.lesson-head { display: flex; justify-content: space-between; margin-bottom: 8px; }
.sub-h { font-size: 14px; margin: 14px 0 8px; }
.url-hint { font-size: 11px; color: #94a3b8; word-break: break-all; }
.form-actions { display: flex; gap: 10px; margin-top: 16px; }
.btn { padding: 8px 16px; border-radius: 8px; border: 1px solid #e2e8f0; background: #fff; cursor: pointer; font-weight: 700; font-size: 13px; }
.btn.primary { background: #1f6aa5; color: #fff; border-color: #1f6aa5; }
.btn.danger { background: #fef2f2; color: #b91c1c; border-color: #fecaca; }
.btn.sm { padding: 5px 10px; font-size: 12px; }
.btn.ghost { background: transparent; }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }
@media (max-width: 900px) { .split { grid-template-columns: 1fr; } }
</style>
