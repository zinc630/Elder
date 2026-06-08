import { computed, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import PortalHomeModals from "../../components/portal/PortalHomeModals.vue";
import {
  fetchActivities,
  fetchCourses,
  fetchNews,
  fetchActivity,
  fetchLifeServices,
  type PortalActivity,
  type PortalCourse,
  type PortalNews
} from "../../api/portal";
import { resolveMediaUrl } from "../../utils/mediaUrl";
import { reportElderLocation } from "../../api/location";

const router = useRouter();
const currentTab = ref<"home" | "health" | "mine">("home");

const elderName = computed(() => localStorage.getItem("elder.userName") ?? "长者");
const elderId = computed(() => localStorage.getItem("elder.id") ?? "");

const portalModals = ref<InstanceType<typeof PortalHomeModals> | null>(null);
const activities = ref<PortalActivity[]>([]);
const courses = ref<PortalCourse[]>([]);
const newsList = ref<PortalNews[]>([]);
const portalMode = ref<"activity" | "course" | "news" | null>(null);
const portalSectionOpen = ref(true);

type SearchItem = { id: string; name: string; icon: string; action: string };
const searchKeyword = ref("");
const showSearchResults = ref(false);
const searchResults = ref<SearchItem[]>([]);

const allFunctions: SearchItem[] = [
  { id: "1", name: "服务预约", icon: "📅", action: "service" },
  { id: "2", name: "健康上报", icon: "📊", action: "health" },
  { id: "3", name: "亲情互动", icon: "💬", action: "family" },
  { id: "4", name: "文娱活动", icon: "🎨", action: "activity" },
  { id: "5", name: "学习赋能", icon: "📚", action: "course" },
  { id: "6", name: "新闻资讯", icon: "📰", action: "news" },
  { id: "7", name: "上报位置", icon: "📍", action: "location" },
  { id: "8", name: "健康档案", icon: "📋", action: "health" },
  { id: "9", name: "吃药提醒", icon: "💊", action: "family" },
  { id: "10", name: "健康评估", icon: "📊", action: "health" },
  { id: "11", name: "个人信息", icon: "👤", action: "profile" },
  { id: "12", name: "修改密码", icon: "🔒", action: "password" },
  { id: "13", name: "地址管理", icon: "📍", action: "address" },
  { id: "14", name: "我的订单", icon: "📋", action: "orders" }
];

const portalSectionTitle = computed(() => {
  if (portalMode.value === "activity") return "文娱活动";
  if (portalMode.value === "course") return "学习赋能";
  if (portalMode.value === "news") return "新闻资讯";
  return "";
});

const portalListItems = computed(() => {
  if (portalMode.value === "activity") {
    return activities.value.map((a) => ({
      id: a.id,
      title: a.title,
      subtitle: a.timeLabel,
      icon: a.icon,
      imageUrl: a.imageUrl,
      raw: a
    }));
  }
  if (portalMode.value === "course") {
    return courses.value.map((c) => ({
      id: c.id,
      title: c.title,
      subtitle: c.category,
      icon: c.icon,
      imageUrl: c.imageUrl,
      raw: c
    }));
  }
  if (portalMode.value === "news") {
    return newsList.value.map((n) => ({
      id: n.id,
      title: n.title,
      subtitle: n.publishDate,
      icon: n.icon,
      imageUrl: n.imageUrl,
      raw: n
    }));
  }
  return [];
});

async function loadPortalContent() {
  try {
    const [a, c, n] = await Promise.all([fetchActivities(), fetchCourses(), fetchNews()]);
    activities.value = a;
    courses.value = c;
    newsList.value = n;
    await fetchLifeServices();
  } catch {
    /* 后端未启动时保留空列表 */
  }
}

function togglePortalMode(mode: "activity" | "course" | "news") {
  portalMode.value = portalMode.value === mode ? null : mode;
  portalSectionOpen.value = true;
}

function onPortalActivity() {
  togglePortalMode("activity");
  if (activities.value.length === 1) {
    portalModals.value?.openActivity(activities.value[0]);
  }
}

function onPortalCourse() {
  togglePortalMode("course");
  if (courses.value.length === 1) {
    portalModals.value?.openCourse(courses.value[0]);
  }
}

function onPortalNews() {
  togglePortalMode("news");
  if (newsList.value.length === 1) {
    portalModals.value?.openNews(newsList.value[0]);
  }
}

function openPortalItem(item: { id: string }) {
  if (portalMode.value === "activity") {
    const act = activities.value.find((a) => a.id === item.id);
    if (act) portalModals.value?.openActivity(act);
  } else if (portalMode.value === "course") {
    portalModals.value?.openCourse({ id: item.id });
  } else if (portalMode.value === "news") {
    portalModals.value?.openNews({ id: item.id });
  }
}

function onSearchInput() {
  const keyword = searchKeyword.value.trim().toLowerCase();
  if (!keyword) {
    searchResults.value = [];
    return;
  }
  searchResults.value = allFunctions.filter(
    (item) => item.name.includes(keyword) || item.name.toLowerCase().includes(keyword)
  );
}

function focusSearch() {
  showSearchResults.value = true;
  onSearchInput();
}

function hideSearchResults() {
  setTimeout(() => {
    showSearchResults.value = false;
  }, 200);
}

function onSearchResultClick(result: SearchItem) {
  showSearchResults.value = false;
  searchKeyword.value = "";
  switch (result.action) {
    case "service":
      onServiceOrder();
      break;
    case "health":
      onHealthReport();
      break;
    case "family":
      onFamily();
      break;
    case "location":
      onReportLocation();
      break;
    case "activity":
      onPortalActivity();
      break;
    case "course":
      onPortalCourse();
      break;
    case "news":
      onPortalNews();
      break;
    case "profile":
      openProfile();
      break;
    case "password":
      openChangePassword();
      break;
    case "address":
      openAddressManage();
      break;
    case "orders":
      openMyOrders();
      break;
  }
}

const showAiChat = ref(false);
const aiInput = ref("");
const aiMessages = ref<Array<{ id: number; role: string; content: string }>>([]);

function toggleAiChat() {
  showAiChat.value = !showAiChat.value;
}

function sendAiMessage() {
  const text = aiInput.value.trim();
  if (!text) return;
  aiMessages.value.push({ id: Date.now(), role: "user", content: text });
  aiInput.value = "";
  setTimeout(() => {
    aiMessages.value.push({
      id: Date.now() + 1,
      role: "ai",
      content: `收到您的提问："${text}"。我正在为您查询相关信息，请稍候...`
    });
  }, 800);
}

const vitalsForm = reactive({
  hr: 78,
  sbp: 125,
  dbp: 80,
  spo2: 96,
  glucose: 5.6,
  weight: 65,
  height: 167,
  bmi: 23.3,
  temperature: 36.5
});

const steps = ref(3534);
const stepsYesterday = ref(3200);
const rank = ref(15);

const networkLabel = computed(() => (navigator.onLine ? "在线" : "离线"));
const emergencyContact = computed(() => "孙儿 138****9102");

const showSosConfirm = ref(false);
const showVitalsModal = ref(false);
const showServiceOrderModal = ref(false);
const showFamilyModal = ref(false);
const familyTab = ref<"chat" | "album" | "video">("chat");
const familyChatInput = ref("");
const chatMessages = ref([
  { id: "m1", mine: false, text: "女儿张敏：爸爸，今天天气好，记得多喝水哦～", meta: "09:32" },
  { id: "m2", mine: true, text: "好的，孩子。晚上我再量一量血压。", meta: "18:12" }
]);

const showToast = ref(false);
const toastTitle = ref("");
const toastDesc = ref("");
const toastIcon = ref("✓");

function showToastMessage(title: string, desc: string, icon = "✓") {
  toastTitle.value = title;
  toastDesc.value = desc;
  toastIcon.value = icon;
  showToast.value = true;
  setTimeout(() => {
    showToast.value = false;
  }, 3000);
}

function vitalsStorageKey() {
  return elderId.value ? `elder.vitals.${elderId.value}` : "";
}

function updateBmi() {
  const w = Number(vitalsForm.weight);
  const h = Number(vitalsForm.height) / 100;
  if (w > 0 && h > 0) {
    vitalsForm.bmi = Math.round((w / (h * h)) * 10) / 10;
  }
}

function saveVitalsToLocal() {
  const key = vitalsStorageKey();
  if (!key) return;
  localStorage.setItem(key, JSON.stringify({ ...vitalsForm }));
}

function loadVitalsFromLocal() {
  const key = vitalsStorageKey();
  if (!key) return;
  const raw = localStorage.getItem(key);
  if (!raw) return;
  try {
    const parsed = JSON.parse(raw) as Partial<typeof vitalsForm>;
    Object.assign(vitalsForm, parsed);
    updateBmi();
  } catch {
    /* ignore */
  }
}

function onSOS() {
  showSosConfirm.value = true;
}

function closeSosConfirm() {
  showSosConfirm.value = false;
}

function confirmSOS() {
  showSosConfirm.value = false;
  showToastMessage("SOS 求助已触发", "已发送告警至家属及养老服务机构", "⚠️");
}

function onServiceOrder() {
  showServiceOrderModal.value = true;
}

function onHealthReport() {
  showVitalsModal.value = true;
}

function onFamily() {
  showFamilyModal.value = true;
  familyTab.value = "chat";
}

function onReportLocation() {
  const id = elderId.value;
  if (!id) {
    showToastMessage("未登录", "未找到长者账号，请重新登录", "❌");
    return;
  }
  if (!navigator.geolocation) {
    showToastMessage("定位失败", "当前浏览器不支持定位", "❌");
    return;
  }
  navigator.geolocation.getCurrentPosition(
    async (pos) => {
      try {
        await reportElderLocation(id, {
          lat: pos.coords.latitude,
          lng: pos.coords.longitude,
          accuracyMeters: pos.coords.accuracy,
          source: "elder_app_browser"
        });
        showToastMessage("位置已上报", "家属与机构端地图将显示最新坐标", "📍");
      } catch (e: unknown) {
        showToastMessage("上报失败", (e as { message?: string })?.message ?? "请检查网络后重试", "❌");
      }
    },
    () => showToastMessage("获取位置失败", "请在系统设置中授权定位权限", "❌"),
    { enableHighAccuracy: true, timeout: 20000, maximumAge: 0 }
  );
}

function onHealthAlarms() {
  router.push("/elder/alarms");
}

function onTrendReport() {
  showToastMessage("生成报告", "健康报告生成中，请稍候...", "📊");
}

function onCallFamily() {
  showToastMessage("一键联系家属", "正在呼叫紧急联系人...", "📞");
}

function onSafetyGuide() {
  showToastMessage("处置指南", "正在加载安全处置指南...", "📖");
}

function submitVitals() {
  vitalsForm.hr = Math.max(30, Math.min(220, Number(vitalsForm.hr) || vitalsForm.hr));
  vitalsForm.sbp = Math.max(70, Math.min(260, Number(vitalsForm.sbp) || vitalsForm.sbp));
  vitalsForm.dbp = Math.max(40, Math.min(180, Number(vitalsForm.dbp) || vitalsForm.dbp));
  vitalsForm.spo2 = Math.max(60, Math.min(100, Number(vitalsForm.spo2) || vitalsForm.spo2));
  vitalsForm.glucose = Math.max(1, Math.min(30, Number(vitalsForm.glucose) || vitalsForm.glucose));
  updateBmi();
  saveVitalsToLocal();
  showVitalsModal.value = false;
  showToastMessage("健康数据已提交", "数据已同步至家属端，请保持良好生活习惯", "✅");
}

function confirmServiceOrder() {
  showServiceOrderModal.value = false;
  showToastMessage("预约成功", "服务已确认，工作人员将尽快联系您", "✅");
}

function sendFamilyMessage() {
  const text = familyChatInput.value.trim();
  if (!text) return;
  chatMessages.value.push({
    id: `m_${Date.now()}`,
    mine: true,
    text,
    meta: new Date().toLocaleTimeString().slice(0, 5)
  });
  familyChatInput.value = "";
}

type AlbumItem = { id: string; name: string; url: string; createdAt: string };
const albumInputRef = ref<HTMLInputElement | null>(null);
const albumUploading = ref(false);
const albumItems = ref<AlbumItem[]>([
  { id: "ea_1", name: "周末团聚", url: "https://picsum.photos/seed/elder-family-1/420/300", createdAt: "2026-04-21 16:20" },
  { id: "ea_2", name: "公园散步", url: "https://picsum.photos/seed/elder-family-2/420/300", createdAt: "2026-04-18 10:08" }
]);
const albumPreview = ref<AlbumItem | null>(null);

function triggerAlbumUpload() {
  albumInputRef.value?.click();
}

function onAlbumFileChange(event: Event) {
  const input = event.target as HTMLInputElement;
  const files = input.files;
  if (!files || files.length === 0) return;
  const file = files[0];
  albumUploading.value = true;
  const reader = new FileReader();
  reader.onload = () => {
    albumItems.value.unshift({
      id: `ea_${Date.now()}`,
      name: file.name,
      url: String(reader.result ?? ""),
      createdAt: new Date().toLocaleString()
    });
    albumUploading.value = false;
    input.value = "";
    showToastMessage("上传成功", "照片已添加到家庭相册", "✅");
  };
  reader.onerror = () => {
    albumUploading.value = false;
    input.value = "";
    showToastMessage("上传失败", "请重试", "❌");
  };
  reader.readAsDataURL(file);
}

function removeAlbumItem(id: string) {
  albumItems.value = albumItems.value.filter((item) => item.id !== id);
}

const videoState = reactive({
  status: "idle" as "idle" | "dialing" | "connected" | "ended",
  muted: false,
  cameraOn: true,
  speakerOn: true,
  durationSec: 0
});
let videoCallTimer: number | null = null;
let videoConnectTimer: number | null = null;

const videoStatusText = computed(() => {
  if (videoState.status === "dialing") return "正在呼叫家属...";
  if (videoState.status === "connected") return "通话中";
  if (videoState.status === "ended") return "通话已结束";
  return "待发起";
});
const videoDurationText = computed(() => {
  const mm = String(Math.floor(videoState.durationSec / 60)).padStart(2, "0");
  const ss = String(videoState.durationSec % 60).padStart(2, "0");
  return `${mm}:${ss}`;
});

function clearVideoTimers() {
  if (videoCallTimer != null) {
    window.clearInterval(videoCallTimer);
    videoCallTimer = null;
  }
  if (videoConnectTimer != null) {
    window.clearTimeout(videoConnectTimer);
    videoConnectTimer = null;
  }
}

function startVideoCall() {
  if (videoState.status === "dialing" || videoState.status === "connected") return;
  clearVideoTimers();
  videoState.status = "dialing";
  videoState.durationSec = 0;
  videoConnectTimer = window.setTimeout(() => {
    videoState.status = "connected";
    videoCallTimer = window.setInterval(() => {
      videoState.durationSec += 1;
    }, 1000);
  }, 1500);
}

function endVideoCall() {
  clearVideoTimers();
  videoState.status = "ended";
}

function resetVideoCall() {
  clearVideoTimers();
  videoState.status = "idle";
  videoState.durationSec = 0;
  videoState.muted = false;
  videoState.cameraOn = true;
  videoState.speakerOn = true;
}

function closeFamilyModal() {
  showFamilyModal.value = false;
  resetVideoCall();
}

const showProfileModal = ref(false);
const userProfile = reactive({
  name: "张三",
  age: 72,
  gender: "男",
  phone: "138****1234",
  address: "北京市朝阳区幸福小区3号楼201室"
});

function openProfile() {
  userProfile.name = elderName.value;
  showProfileModal.value = true;
}

const showChangePasswordModal = ref(false);
const passwordForm = reactive({ oldPassword: "", newPassword: "", confirmPassword: "" });

function openChangePassword() {
  passwordForm.oldPassword = "";
  passwordForm.newPassword = "";
  passwordForm.confirmPassword = "";
  showChangePasswordModal.value = true;
}

function confirmChangePassword() {
  if (!passwordForm.oldPassword) {
    showToastMessage("提示", "请输入原密码", "⚠️");
    return;
  }
  if (!passwordForm.newPassword) {
    showToastMessage("提示", "请输入新密码", "⚠️");
    return;
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    showToastMessage("提示", "两次输入的密码不一致", "⚠️");
    return;
  }
  showChangePasswordModal.value = false;
  showToastMessage("密码修改成功", "请使用新密码重新登录", "✅");
}

const showAddressModal = ref(false);
const addressList = ref([
  { id: 1, name: "家庭住址", address: "北京市朝阳区幸福小区3号楼201室", isDefault: true },
  { id: 2, name: "子女家", address: "北京市海淀区科技园8号楼602室", isDefault: false }
]);

function openAddressManage() {
  showAddressModal.value = true;
}

function deleteAddress(id: number) {
  if (confirm("确定要删除该地址吗？")) {
    addressList.value = addressList.value.filter((addr) => addr.id !== id);
    showToastMessage("地址已删除", "地址管理已更新", "✅");
  }
}

function editAddress(addr: { name: string }) {
  showToastMessage("编辑地址", `正在编辑: ${addr.name}`, "📝");
}

function addNewAddress() {
  const newAddr = prompt("请输入新地址：");
  if (newAddr) {
    addressList.value.push({ id: Date.now(), name: "新地址", address: newAddr, isDefault: false });
    showToastMessage("地址已添加", "新地址已保存", "✅");
  }
}

const showRelatedUsersModal = ref(false);
const relatedUsers = ref([
  { id: 1, name: "张明", relation: "儿子", phone: "139****5678" },
  { id: 2, name: "张丽", relation: "女儿", phone: "138****9012" }
]);

function openRelatedUsers() {
  showRelatedUsersModal.value = true;
}

function deleteRelatedUser(id: number) {
  if (confirm("确定要移除该关联用户吗？")) {
    relatedUsers.value = relatedUsers.value.filter((user) => user.id !== id);
    showToastMessage("已移除关联用户", "关联关系已解除", "✅");
  }
}

function addRelatedUser() {
  const name = prompt("请输入关联用户姓名：");
  if (name) {
    relatedUsers.value.push({ id: Date.now(), name, relation: "亲友", phone: "请完善" });
    showToastMessage("关联用户已添加", "请完善用户信息", "✅");
  }
}

const showVolunteerModal = ref(false);
const volunteerInfo = ref({
  name: "李华",
  organization: "阳光志愿者协会",
  phone: "188****3456",
  description: "擅长老年人心理疏导、健康咨询，每周三下午提供上门服务"
});

function openVolunteer() {
  showVolunteerModal.value = true;
}

const showButlerModal = ref(false);
const butlerInfo = ref({
  name: "王管家",
  title: "资深养老管家",
  phone: "177****7890",
  description: "从事养老服务8年，擅长老年人生活照料、健康管理、活动组织"
});

function openMyButler() {
  showButlerModal.value = true;
}

function callButler() {
  showToastMessage("呼叫管家", "正在为您联系王管家...", "📞");
}

const showOrdersModal = ref(false);
const orderList = ref([
  { id: "20250101001", service: "助餐服务（送餐上门）", time: "2025-01-01 12:00", amount: 25.0, status: "已完成", statusClass: "completed" },
  { id: "20250102002", service: "家政保洁（全屋清洁）", time: "2025-01-02 14:00", amount: 120.0, status: "进行中", statusClass: "processing" },
  { id: "20250103003", service: "陪诊陪护（医院陪同）", time: "2025-01-03 09:00", amount: 80.0, status: "待确认", statusClass: "pending" }
]);

function openMyOrders() {
  showOrdersModal.value = true;
}

const showMemberModal = ref(false);
const memberInfo = reactive({
  level: "👑",
  levelName: "黄金会员",
  balance: 568.5,
  points: 2340,
  coupons: 3
});

function openMemberAccount() {
  showMemberModal.value = true;
}

function toggleSetting(type: "alert" | "info") {
  const status = type === "alert" ? "告警提示" : "完善信息";
  showToastMessage(`${status}已切换`, "当前状态：已开启", "⚙️");
}

const service = reactive({
  serviceType: "NURSING",
  appointmentTime: "",
  notes: ""
});

onMounted(() => {
  const now = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  service.appointmentTime = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours() + 1)}:${pad(now.getMinutes())}`;
  loadPortalContent();
  loadVitalsFromLocal();
});

onBeforeUnmount(() => {
  clearVideoTimers();
});
