# -*- coding: utf-8 -*-
"""Restore AdminPortalContent.vue with correct UTF-8 Chinese labels."""
from pathlib import Path

OUT = Path(__file__).resolve().parents[1] / "src/views/admin/AdminPortalContent.vue"

T = {
    "page_title": "\u9996\u9875\u5185\u5bb9\u7ba1\u7406",
    "sub": "\u7ba1\u7406\u529f\u80fd\u9996\u9875\u7684\u6587\u5a31\u6d3b\u52a8\u3001\u5b66\u4e60\u8d4b\u80fd\u8bfe\u7a0b\u3001\u65b0\u95fb\u8d44\u8baf\uff08\u6587\u5b57\u3001\u5c01\u9762\u56fe\u3001\u8bfe\u7a0b\u89c6\u9891\uff09\u3002\u5df2\u914d\u7f6e MySQL \u65f6\u81ea\u52a8\u5199\u5165 elder \u5e93\uff1b\u672a\u914d\u7f6e\u65f6\u4ecd\u4f7f\u7528\u672c\u5730 JSON \u6587\u4ef6\u3002",
    "loading": "\u52a0\u8f7d\u4e2d\u2026",
    "tab_activity": "\u6587\u5a31\u6d3b\u52a8",
    "tab_course": "\u5b66\u4e60\u8d4b\u80fd",
    "tab_news": "\u65b0\u95fb\u8d44\u8baf",
    "new_activity": "\u65b0\u5efa\u6d3b\u52a8",
    "new_course": "\u65b0\u5efa\u8bfe\u7a0b",
    "new_news": "\u65b0\u5efa\u8d44\u8baf",
    "refresh": "\u5237\u65b0",
    "import_demo": "\u5bfc\u5165\u793a\u4f8b\u6570\u636e",
    "empty_activity": "\u6682\u65e0\u6d3b\u52a8\uff0c\u53ef\u300c\u5bfc\u5165\u793a\u4f8b\u6570\u636e\u300d\u6216\u300c\u65b0\u5efa\u6d3b\u52a8\u300d",
    "empty_course": "\u6682\u65e0\u8bfe\u7a0b\uff0c\u53ef\u300c\u5bfc\u5165\u793a\u4f8b\u6570\u636e\u300d\u6216\u300c\u65b0\u5efa\u8bfe\u7a0b\u300d",
    "empty_news": "\u6682\u65e0\u8d44\u8baf\uff0c\u53ef\u300c\u5bfc\u5165\u793a\u4f8b\u6570\u636e\u300d\u6216\u300c\u65b0\u5efa\u8d44\u8baf\u300d",
    "ph_activity": "\u8bf7\u4ece\u5de6\u4fa7\u9009\u62e9\u4e00\u6761\u6d3b\u52a8\uff0c\u6216\u70b9\u51fb\u300c\u65b0\u5efa\u6d3b\u52a8\u300d\u5f00\u59cb\u7f16\u8f91\u3002",
    "ph_activity_hint": "\u652f\u6301\u4fee\u6539\u6587\u5b57\u3001\u4e0a\u4f20\u5c01\u9762\u56fe\u7247\uff1b\u4fdd\u5b58\u540e\u540c\u6b65\u5230\u529f\u80fd\u9996\u9875\u3002",
    "ph_course": "\u8bf7\u4ece\u5de6\u4fa7\u9009\u62e9\u8bfe\u7a0b\uff0c\u6216\u70b9\u51fb\u300c\u65b0\u5efa\u8bfe\u7a0b\u300d\u3002",
    "ph_course_hint": "\u53ef\u4e0a\u4f20\u5c01\u9762\u56fe\u3001\u5404\u8bfe\u65f6\u89c6\u9891\u53ca\u6587\u5b57\u8bf4\u660e\u3002",
    "ph_news": "\u8bf7\u4ece\u5de6\u4fa7\u9009\u62e9\u8d44\u8baf\uff0c\u6216\u70b9\u51fb\u300c\u65b0\u5efa\u8d44\u8baf\u300d\u3002",
    "ph_news_hint": "\u652f\u6301\u6b63\u6587\u3001\u6458\u8981\u53ca\u5c01\u9762\u56fe\u4e0a\u4f20\u3002",
    "edit_activity": "\u7f16\u8f91\u6d3b\u52a8",
    "edit_course": "\u7f16\u8f91\u8bfe\u7a0b",
    "edit_news": "\u7f16\u8f91\u8d44\u8baf",
    "save": "\u4fdd\u5b58",
    "delete": "\u5220\u9664",
    "upload_img": "\u4e0a\u4f20\u56fe\u7247",
    "upload_video": "\u4e0a\u4f20\u89c6\u9891",
    "clear": "\u6e05\u9664",
    "cover": "\u5c01\u9762\u56fe",
    "lesson_video": "\u8bfe\u65f6 / \u89c6\u9891",
    "add_lesson": "+ \u6dfb\u52a0\u8bfe\u65f6",
    "confirm_import": "\u5c06\u5bfc\u5165\u793a\u4f8b\u6587\u5a31\u6d3b\u52a8\u3001\u8bfe\u7a0b\u4e0e\u8d44\u8baf\uff08\u5df2\u6709\u6570\u636e\u4f1a\u88ab\u8986\u76d6\uff09\uff0c\u662f\u5426\u7ee7\u7eed\uff1f",
    "import_ok": "\u793a\u4f8b\u6570\u636e\u5df2\u5bfc\u5165",
    "import_fail": "\u5bfc\u5165\u5931\u8d25",
    "load_fail": "\u52a0\u8f7d\u5931\u8d25",
    "upload_fail": "\u4e0a\u4f20\u5931\u8d25",
    "save_fail": "\u4fdd\u5b58\u5931\u8d25",
    "img_ok": "\u56fe\u7247\u5df2\u4e0a\u4f20",
    "cover_ok": "\u5c01\u9762\u5df2\u4e0a\u4f20",
    "video_ok": "\u89c6\u9891\u5df2\u4e0a\u4f20",
    "activity_ok": "\u6d3b\u52a8\u5df2\u4fdd\u5b58",
    "course_ok": "\u8bfe\u7a0b\u5df2\u4fdd\u5b58",
    "news_ok": "\u8d44\u8baf\u5df2\u4fdd\u5b58",
    "deleted": "\u5df2\u5220\u9664",
    "confirm_del_activity": "\u786e\u5b9a\u5220\u9664\u8be5\u6d3b\u52a8\uff1f",
    "confirm_del_course": "\u786e\u5b9a\u5220\u9664\u8be5\u8bfe\u7a0b\uff1f",
    "confirm_del_news": "\u786e\u5b9a\u5220\u9664\u8be5\u8d44\u8baf\uff1f",
    "tag_activity": "\u6d3b\u52a8",
    "tag_course": "\u8bfe\u7a0b",
    "lesson1": "\u7b2c 1 \u8282",
    "duration30": "30\u5206\u949f",
    "open_enroll": "\u5f00\u653e\u62a5\u540d",
    "icon_emoji": "\u56fe\u6807\uff08Emoji\uff09",
    "tag_label": "\u5206\u7c7b\u6807\u7b7e",
    "title": "\u6807\u9898",
    "time": "\u65f6\u95f4",
    "location": "\u5730\u70b9",
    "summary": "\u7b80\u4ecb",
    "capacity": "\u540d\u989d",
    "icon": "\u56fe\u6807",
    "category": "\u5206\u7c7b",
    "duration_label": "\u65f6\u957f\u6587\u6848",
    "rating": "\u8bc4\u5206",
    "desc": "\u8bf4\u660e",
    "video": "\u89c6\u9891",
    "publish_date": "\u53d1\u5e03\u65e5\u671f",
    "source": "\u6765\u6e90",
    "body": "\u6b63\u6587",
    "abstract": "\u6458\u8981",
    "lesson_n": "\u7b2c",
    "lesson_unit": "\u8282",
    "duration_min": "\u65f6\u957f\uff08\u5206\u9499\uff09",
}

# Read current file for script/style sections (ASCII-safe), patch template
current = OUT.read_text(encoding="utf-8", errors="replace") if OUT.exists() else ""

SCRIPT_START = "<script setup lang=\"ts\">"
STYLE_START = "<style scoped>"

def extract_section(text: str, start: str, end: str) -> str:
    i = text.find(start)
    j = text.find(end, i)
    if i < 0 or j < 0:
        return ""
    return text[i : j + len(end)]

# Use script/style from current if valid; else minimal fallback
script = extract_section(current, SCRIPT_START, "</script>")
style = extract_section(current, STYLE_START, "</style>")

if "loadAll" not in script:
    script = open(Path(__file__).parent / "_admin_portal_script.ts.txt", encoding="utf-8").read() if False else ""

# Build template
vue = f'''<template>
  <div class="portal-admin">
    <h1 class="page-title">{T["page_title"]}</h1>
    <p class="sub">{T["sub"]}</p>

    <motion>
    <div v-if="loadError" class="msg err">{{{{ loadError }}}}</div>
    <div v-if="msg" class="msg" :class="msgOk ? 'ok' : 'err'">{{{{ msg }}}}</div>
    <div v-if="loading" class="loading-hint">{T["loading"]}</motion>

    <div class="tabs">
      <button
        v-for="t in tabs"
        :key="t.id"
        type="button"
        class="tab"
        :class="{{ active: tab === t.id }}"
        @click="switchTab(t.id)"
      >
        {{{{ t.label }}}}
      </button>
    </div>

    <div v-show="tab === 'activity'" class="panel">
      <div class="toolbar">
        <button type="button" class="btn primary" @click="newActivity">{T["new_activity"]}</button>
        <button type="button" class="btn" :disabled="busy" @click="loadAll">{T["refresh"]}</button>
        <button type="button" class="btn" :disabled="busy" @click="importDemo">{T["import_demo"]}</button>
      </div>
      <div class="split">
        <ul class="list">
          <li v-if="!activities.length" class="list-empty">{T["empty_activity"]}</li>
          <li
            v-for="a in activities"
            :key="a.id"
            :class="{{ active: activityForm.id === a.id && !activityIsNew }}"
            @click="selectActivity(a)"
          >
            <strong>{{{{ a.title }}}}</strong>
            <span>{{{{ a.tag }}}} \u00b7 {{{{ a.timeLabel }}}}</span>
          </li>
        </ul>
        <div v-if="!(activityForm.id || activityIsNew)" class="form card form-placeholder">
          <p>{T["ph_activity"]}</p>
          <p class="hint">{T["ph_activity_hint"]}</p>
        </div>
        <div v-if="activityForm.id || activityIsNew" class="form card">
          <h2>{{{{ activityIsNew ? "{T["new_activity"]}" : "{T["edit_activity"]}" }}}}</h2>
          <label>{T["icon_emoji"]}<input v-model="activityForm.icon" type="text" /></label>
          <label>{T["tag_label"]}<input v-model="activityForm.tag" type="text" /></label>
          <label>{T["title"]}<input v-model="activityForm.title" type="text" /></label>
          <label>{T["time"]}<input v-model="activityForm.timeLabel" type="text" placeholder="\u5982 2024.12.25 14:00" /></label>
          <label>{T["location"]}<input v-model="activityForm.location" type="text" /></label>
          <label>{T["summary"]}<textarea v-model="activityForm.description" rows="3" /></label>
          <label>{T["capacity"]}<input v-model.number="activityForm.capacity" type="number" min="1" /></label>
          <label class="row-check"><input v-model="activityForm.open" type="checkbox" />{T["open_enroll"]}</label>
          <div class="upload-row">
            <label>{T["cover"]}</label>
            <motion>
            <div class="upload-actions">
              <input ref="activityImgInput" type="file" accept="image/*" hidden @change="onActivityImage" />
              <button type="button" class="btn sm" @click="activityImgInput?.click()">{T["upload_img"]}</button>
              <button v-if="activityForm.imageUrl" type="button" class="btn sm ghost" @click="activityForm.imageUrl = null">{T["clear"]}</button>
            </div>
            <img v-if="activityForm.imageUrl" :src="resolveMediaUrl(activityForm.imageUrl)" class="preview" alt="" />
          </div>
          <div class="form-actions">
            <button type="button" class="btn primary" :disabled="busy" @click="saveActivity">{T["save"]}</button>
            <button v-if="!activityIsNew" type="button" class="btn danger" :disabled="busy" @click="removeActivity">{T["delete"]}</button>
          </motion>
        </div>
      </div>
    </div>

    <div v-show="tab === 'course'" class="panel">
      <div class="toolbar">
        <button type="button" class="btn primary" @click="newCourse">{T["new_course"]}</button>
        <button type="button" class="btn" :disabled="busy" @click="loadAll">{T["refresh"]}</button>
        <button type="button" class="btn" :disabled="busy" @click="importDemo">{T["import_demo"]}</button>
      </div>
      <div class="split">
        <ul class="list">
          <li v-if="!courses.length" class="list-empty">{T["empty_course"]}</li>
          <li
            v-for="c in courses"
            :key="c.id"
            :class="{{ active: courseForm.id === c.id && !courseIsNew }}"
            @click="selectCourse(c)"
          >
            <strong>{{{{ c.title }}}}</strong>
            <span>{{{{ c.category }}}} \u00b7 {{{{ c.duration }}}}</span>
          </li>
        </ul>
        <div v-if="!(courseForm.id || courseIsNew)" class="form card form-placeholder">
          <p>{T["ph_course"]}</p>
          <p class="hint">{T["ph_course_hint"]}</p>
        </div>
        <div v-if="courseForm.id || courseIsNew" class="form card">
          <h2>{{{{ courseIsNew ? "{T["new_course"]}" : "{T["edit_course"]}" }}}}</h2>
          <label>{T["icon"]}<input v-model="courseForm.icon" type="text" /></label>
          <label>{T["category"]}<input v-model="courseForm.category" type="text" /></label>
          <label>{T["title"]}<input v-model="courseForm.title" type="text" /></label>
          <label>{T["duration_label"]}<input v-model="courseForm.duration" type="text" placeholder="\u5982 30\u5206\u949f" /></label>
          <label>{T["rating"]}<input v-model.number="courseForm.rating" type="number" min="0" max="5" step="0.1" /></label>
          <label>{T["summary"]}<textarea v-model="courseForm.description" rows="2" /></label>
          <div class="upload-row">
            <label>{T["cover"]}</label>
            <div class="upload-actions">
              <input ref="courseImgInput" type="file" accept="image/*" hidden @change="onCourseImage" />
              <button type="button" class="btn sm" @click="courseImgInput?.click()">{T["upload_img"]}</button>
              <button v-if="courseForm.imageUrl" type="button" class="btn sm ghost" @click="courseForm.imageUrl = null">{T["clear"]}</button>
            </div>
            <img v-if="courseForm.imageUrl" :src="resolveMediaUrl(courseForm.imageUrl)" class="preview" alt="" />
          </div>
          <h3 class="sub-h">{T["lesson_video"]}</h3>
          <motion>
          <div v-for="(lesson, idx) in courseForm.lessons" :key="lesson.id || idx" class="lesson-block">
            <div class="lesson-head">
              <strong>{T["lesson_n"]} {{{{ idx + 1 }}}} {T["lesson_unit"]}</strong>
              <button v-if="courseForm.lessons.length > 1" type="button" class="btn sm ghost" @click="removeLesson(idx)">{T["delete"]}</button>
            </div>
            <label>{T["title"]}<input v-model="lesson.title" type="text" /></label>
            <label>{T["desc"]}<textarea v-model="lesson.content" rows="2" /></label>
            <label>{T["duration_min"]}<input v-model.number="lesson.durationMinutes" type="number" min="1" /></label>
            <div class="upload-row">
              <label>{T["video"]}</label>
              <div class="upload-actions">
                <input
                  :ref="(el) => setLessonVideoInput(idx, el as HTMLInputElement | null)"
                  type="file"
                  accept="video/mp4,video/webm,video/quicktime"
                  hidden
                  @change="(e) => onLessonVideo(idx, e)"
                />
                <button type="button" class="btn sm" @click="lessonVideoInput[idx]?.click()">{T["upload_video"]}</button>
                <button v-if="lesson.videoUrl" type="button" class="btn sm ghost" @click="lesson.videoUrl = null">{T["clear"]}</button>
              </div>
              <p v-if="lesson.videoUrl" class="url-hint">{{{{ lesson.videoUrl }}}}</p>
            </div>
          </div>
          <button type="button" class="btn sm" @click="addLesson">{T["add_lesson"]}</button>
          <div class="form-actions">
            <button type="button" class="btn primary" :disabled="busy" @click="saveCourse">{T["save"]}</button>
            <button v-if="!courseIsNew" type="button" class="btn danger" :disabled="busy" @click="removeCourse">{T["delete"]}</button>
          </div>
        </div>
      </div>
    </div>

    <div v-show="tab === 'news'" class="panel">
      <div class="toolbar">
        <button type="button" class="btn primary" @click="newNews">{T["new_news"]}</button>
        <button type="button" class="btn" :disabled="busy" @click="loadAll">{T["refresh"]}</button>
        <button type="button" class="btn" :disabled="busy" @click="importDemo">{T["import_demo"]}</button>
      </div>
      <div class="split">
        <ul class="list">
          <li v-if="!newsList.length" class="list-empty">{T["empty_news"]}</li>
          <li
            v-for="n in newsList"
            :key="n.id"
            :class="{{ active: newsForm.id === n.id && !newsIsNew }}"
            @click="selectNews(n)"
          >
            <strong>{{{{ n.title }}}}</strong>
            <span>{{{{ n.publishDate }}}} \u00b7 {{{{ n.source }}}}</span>
          </li>
        </ul>
        <div v-if="!(newsForm.id || newsIsNew)" class="form card form-placeholder">
          <p>{T["ph_news"]}</p>
          <p class="hint">{T["ph_news_hint"]}</p>
        </div>
        <div v-if="newsForm.id || newsIsNew" class="form card">
          <h2>{{{{ newsIsNew ? "{T["new_news"]}" : "{T["edit_news"]}" }}}}</h2>
          <label>{T["icon"]}<input v-model="newsForm.icon" type="text" /></label>
          <label>{T["title"]}<input v-model="newsForm.title" type="text" /></label>
          <label>{T["abstract"]}<textarea v-model="newsForm.summary" rows="2" /></label>
          <label>{T["publish_date"]}<input v-model="newsForm.publishDate" type="text" placeholder="YYYY-MM-DD" /></label>
          <label>{T["source"]}<input v-model="newsForm.source" type="text" /></label>
          <label>{T["body"]}<textarea v-model="newsForm.body" rows="8" /></label>
          <div class="upload-row">
            <label>{T["cover"]}</label>
            <div class="upload-actions">
              <input ref="newsImgInput" type="file" accept="image/*" hidden @change="onNewsImage" />
              <button type="button" class="btn sm" @click="newsImgInput?.click()">{T["upload_img"]}</button>
              <button v-if="newsForm.imageUrl" type="button" class="btn sm ghost" @click="newsForm.imageUrl = null">{T["clear"]}</button>
            </div>
            <img v-if="newsForm.imageUrl" :src="resolveMediaUrl(newsForm.imageUrl)" class="preview" alt="" />
          </div>
          <div class="form-actions">
            <button type="button" class="btn primary" :disabled="busy" @click="saveNews">{T["save"]}</button>
            <button v-if="!newsIsNew" type="button" class="btn danger" :disabled="busy" @click="removeNews">{T["delete"]}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
'''

# Fix accidental motion tags in generator
vue = vue.replace("<motion>", "").replace("</motion>", "</div>")

# Script block - embed directly with unicode in strings via T
SCRIPT = '''
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
  { id: "activity" as TabId, label: "''' + T["tab_activity"] + '''" },
  { id: "course" as TabId, label: "''' + T["tab_course"] + '''" },
  { id: "news" as TabId, label: "''' + T["tab_news"] + '''" }
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
  icon: "\\uD83C\\uDFAD",
  tag: "''' + T["tag_activity"] + '''",
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
  title: "''' + T["lesson1"] + '''",
  content: "",
  durationMinutes: 10,
  videoUrl: null
});

const emptyCourse = (): CourseAdmin => ({
  id: "",
  icon: "\\uD83D\\uDCDA",
  category: "''' + T["tag_course"] + '''",
  title: "",
  description: "",
  duration: "''' + T["duration30"] + '''",
  viewCount: 0,
  rating: 4.8,
  lessons: [emptyLesson()],
  imageUrl: null
});

const emptyNews = (): NewsAdmin => ({
  id: "",
  icon: "\\uD83D\\uDCF0",
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
    const text = e instanceof Error ? e.message : "''' + T["load_fail"] + '''";
    loadError.value = text;
    flash(text, false);
  } finally {
    loading.value = false;
  }
}

async function importDemo() {
  if (!confirm("''' + T["confirm_import"] + '''")) return;
  busy.value = true;
  loadError.value = "";
  try {
    await seedDemoContent();
    await loadAll();
    flash("''' + T["import_ok"] + '''");
  } catch (e) {
    const text = e instanceof Error ? e.message : "''' + T["import_fail"] + '''";
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
    flash("''' + T["img_ok"] + '''");
  } catch (err) {
    flash(err instanceof Error ? err.message : "''' + T["upload_fail"] + '''", false);
  } finally {
    busy.value = false;
    (e.target as HTMLInputElement).value = "";
  }
}
async function saveActivity() {
  busy.value = true;
  try {
    const saved = await saveAdminActivity(activityForm, activityIsNew.value);
    flash("''' + T["activity_ok"] + '''");
    activityIsNew.value = false;
    Object.assign(activityForm, saved);
    await loadActivities();
  } catch (err) {
    flash(err instanceof Error ? err.message : "''' + T["save_fail"] + '''", false);
  } finally {
    busy.value = false;
  }
}
async function removeActivity() {
  if (!activityForm.id || !confirm("''' + T["confirm_del_activity"] + '''")) return;
  busy.value = true;
  try {
    await deleteAdminActivity(activityForm.id);
    flash("''' + T["deleted"] + '''");
    Object.assign(activityForm, emptyActivity());
    activityForm.id = "";
    await loadActivities();
  } catch (err) {
    flash(err instanceof Error ? err.message : "''' + T["save_fail"] + '''", false);
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
    flash("''' + T["cover_ok"] + '''");
  } catch (err) {
    flash(err instanceof Error ? err.message : "''' + T["upload_fail"] + '''", false);
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
    flash("''' + T["video_ok"] + '''");
  } catch (err) {
    flash(err instanceof Error ? err.message : "''' + T["upload_fail"] + '''", false);
  } finally {
    busy.value = false;
    (e.target as HTMLInputElement).value = "";
  }
}
async function saveCourse() {
  busy.value = true;
  try {
    const saved = await saveAdminCourse(courseForm, courseIsNew.value);
    flash("''' + T["course_ok"] + '''");
    courseIsNew.value = false;
    Object.assign(courseForm, JSON.parse(JSON.stringify(saved)));
    await loadCourses();
  } catch (err) {
    flash(err instanceof Error ? err.message : "''' + T["save_fail"] + '''", false);
  } finally {
    busy.value = false;
  }
}
async function removeCourse() {
  if (!courseForm.id || !confirm("''' + T["confirm_del_course"] + '''")) return;
  busy.value = true;
  try {
    await deleteAdminCourse(courseForm.id);
    flash("''' + T["deleted"] + '''");
    Object.assign(courseForm, emptyCourse());
    courseForm.id = "";
    await loadCourses();
  } catch (err) {
    flash(err instanceof Error ? err.message : "''' + T["save_fail"] + '''", false);
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
    flash("''' + T["img_ok"] + '''");
  } catch (err) {
    flash(err instanceof Error ? err.message : "''' + T["upload_fail"] + '''", false);
  } finally {
    busy.value = false;
    (e.target as HTMLInputElement).value = "";
  }
}
async function saveNews() {
  busy.value = true;
  try {
    const saved = await saveAdminNews(newsForm, newsIsNew.value);
    flash("''' + T["news_ok"] + '''");
    newsIsNew.value = false;
    Object.assign(newsForm, saved);
    await loadNews();
  } catch (err) {
    flash(err instanceof Error ? err.message : "''' + T["save_fail"] + '''", false);
  } finally {
    busy.value = false;
  }
}
async function removeNews() {
  if (!newsForm.id || !confirm("''' + T["confirm_del_news"] + '''")) return;
  busy.value = true;
  try {
    await deleteAdminNews(newsForm.id);
    flash("''' + T["deleted"] + '''");
    Object.assign(newsForm, emptyNews());
    newsForm.id = "";
    await loadNews();
  } catch (err) {
    flash(err instanceof Error ? err.message : "''' + T["save_fail"] + '''", false);
  } finally {
    busy.value = false;
  }
}

onMounted(() => { void loadAll(); });
</script>
'''

STYLE = '''
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
'''

OUT.write_text(vue + SCRIPT + STYLE, encoding="utf-8", newline="\n")
print("Wrote", OUT)
