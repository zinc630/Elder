<template>
  <div class="auth-shell">
    <div class="auth-decor" aria-hidden="true" />
    <div class="auth-panel">
      <nav class="auth-top" aria-label="页面导航">
        <button type="button" class="text-link" @click="router.push('/')">功能首页</button>
        <span class="sep">·</span>
        <button type="button" class="text-link" @click="router.push({ path: '/login', query: { role: role } })">
          返回登录
        </button>
      </nav>

      <header class="auth-header">
        <p class="auth-kicker">银发智盾</p>
        <h1 class="auth-title">注册</h1>
        <p class="auth-sub">创建账号并保存系统分配的 ID，用于家属与机构绑定（演示）</p>
      </header>

      <div class="card">
        <div class="row id-row">
        <label>系统账号ID</label>
        <input class="readonly-input" :value="assignedUserId" readonly tabindex="-1" />
        </div>
        <p class="id-note">由系统按当前身份分配独立前缀 ID，注册成功后请保存，用于与家属、机构绑定。</p>

        <div class="row">
          <label>身份角色</label>
          <select v-model="role">
            <option value="ELDER">老人</option>
            <option value="CHILD">子女</option>
            <option value="AGENCY">服务机构</option>
          </select>
        </div>

        <div class="row">
          <label>用户姓名</label>
          <input v-model="userName" placeholder="请输入用户姓名（登录用）" autocomplete="username" />
        </div>

        <div class="row">
          <label>用户密码</label>
          <input v-model="password" placeholder="请输入用户密码" type="password" autocomplete="new-password" />
        </div>

        <div v-if="role === 'ELDER' || role === 'CHILD'" class="divider" />

        <div v-if="role === 'ELDER'">
          <div class="row">
            <label>性别</label>
            <select v-model="gender">
              <option value="">请选择</option>
              <option value="男">男</option>
              <option value="女">女</option>
            </select>
          </div>

          <div class="row">
            <label>地址</label>
            <div class="input-with-action">
              <input v-model="address" placeholder="请输入地址" />
              <button type="button" class="loc-btn" :disabled="locatingAddress" @click="fillAddressFromLocation">
                {{ locatingAddress ? "定位中..." : "定位填充" }}
              </button>
              <button type="button" class="map-btn" @click="openMapAdjust">地图微调</button>
            </div>
          </div>
          <p v-if="locationStatus" class="loc-status" :class="`is-${locationStatus.tone}`">{{ locationStatus.message }}</p>

          <div class="row">
            <label>年龄</label>
            <input v-model.number="age" placeholder="请输入年龄" type="number" min="0" max="130" />
          </div>

          <!-- 关联关系改为登录后绑定 -->
        </div>

        <div v-else-if="role === 'CHILD'">
          <div class="row">
            <label>性别</label>
            <select v-model="gender">
              <option value="">请选择</option>
              <option value="男">男</option>
              <option value="女">女</option>
            </select>
          </div>

          <div class="row">
            <label>地址</label>
            <div class="input-with-action">
              <input v-model="address" placeholder="请输入地址" />
              <button type="button" class="loc-btn" :disabled="locatingAddress" @click="fillAddressFromLocation">
                {{ locatingAddress ? "定位中..." : "定位填充" }}
              </button>
              <button type="button" class="map-btn" @click="openMapAdjust">地图微调</button>
            </div>
          </div>
          <p v-if="locationStatus" class="loc-status" :class="`is-${locationStatus.tone}`">{{ locationStatus.message }}</p>

          <div class="row">
            <label>年龄</label>
            <input v-model.number="age" placeholder="请输入年龄" type="number" min="0" max="130" />
          </div>

          <!-- 关联关系改为登录后绑定 -->
        </div>

        <div v-else-if="role === 'AGENCY'">
          <div class="row">
            <label>机构地址</label>
            <div class="input-with-action">
              <input v-model="address" placeholder="请输入机构地址" />
              <button type="button" class="loc-btn" :disabled="locatingAddress" @click="fillAddressFromLocation">
                {{ locatingAddress ? "定位中..." : "定位填充" }}
              </button>
              <button type="button" class="map-btn" @click="openMapAdjust">地图微调</button>
            </div>
          </div>
          <p v-if="locationStatus" class="loc-status" :class="`is-${locationStatus.tone}`">{{ locationStatus.message }}</p>
        </div>

        <div class="btn-row">
          <button type="button" class="btn-primary" @click="onSubmit">注册</button>
          <button type="button" class="btn-secondary" @click="onBack">返回登录</button>
        </div>
      </div>
    </div>
  </div>

  <div v-if="showMapAdjust" class="map-adjust-overlay">
    <div class="map-adjust-card">
      <div class="map-adjust-head">
        <strong>地图微调地址</strong>
        <button type="button" class="map-close-btn" @click="closeMapAdjust">✕</button>
      </div>
      <p class="map-adjust-tip">拖拽地图上的定位点，放开后自动解析地址。</p>
      <div ref="mapContainerRef" class="map-adjust-canvas">
        <div v-if="mapLoading" class="map-loading">地图加载中...</div>
      </div>
      <p class="map-adjust-result">{{ adjustedAddress || "请拖拽定位点以微调地址" }}</p>
      <div class="map-adjust-actions">
        <button type="button" class="btn-secondary" @click="closeMapAdjust">取消</button>
        <button type="button" class="btn-primary" :disabled="!mapAdjustReady" @click="applyMapAdjust">使用该地址</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import type { RegisterRole } from "../api/authServer";
import { getUserBySystemIdServer, previewNextSystemUserIdServer, registerUserServer } from "../api/authServer";
import { upsertElderProfile } from "../api/elder";
import { getAmapKey, getAmapWebServiceKey, getWindowAMap, loadAmapScript } from "../utils/amapLoader";

const route = useRoute();
const router = useRouter();

const initialRole = computed<RegisterRole>(() => {
  const q = route.query.role;
  if (q === "ELDER" || q === "CHILD" || q === "AGENCY") return q;
  return "ELDER";
});

const role = ref<RegisterRole>("ELDER");
const userName = ref("");
const password = ref("");

const gender = ref("");
const address = ref("");
const age = ref<number | null>(null);

// 关联关系改为登录后绑定，不在注册时填写
const locatingAddress = ref(false);
const locationStatus = ref<{ tone: "success" | "warn" | "error"; message: string } | null>(null);
const showMapAdjust = ref(false);
const mapLoading = ref(false);
const mapAdjustReady = ref(false);
const mapContainerRef = ref<HTMLElement | null>(null);
const adjustedAddress = ref("");
const adjustedLngLat = ref<{ lng: number; lat: number } | null>(null);
const lastLocatedLngLat = ref<{ lng: number; lat: number } | null>(null);
let adjustMap: any = null;
let adjustMarker: any = null;

const assignedUserId = ref("");

function refreshAssignedId() {
  previewNextSystemUserIdServer(role.value)
    .then((id) => {
      assignedUserId.value = id;
    })
    .catch(() => {
      assignedUserId.value = "";
      locationStatus.value = { tone: "error", message: "系统账号ID分配失败，请检查后端服务" };
    });
}

onMounted(() => {
  role.value = initialRole.value;
  refreshAssignedId();
});

watch(
  () => role.value,
  () => {
    refreshAssignedId();
  }
);

function onBack() {
  router.push({ path: "/login" });
}

function normalizeIdList(input: string): string[] {
  return Array.from(
    new Set(
      input
        .split(/[,\uFF0C;\uFF1B\s]+/)
        .map((s) => s.trim().toUpperCase())
        .filter(Boolean)
    )
  );
}

function normalizeSingleId(input: string): string {
  return input.trim().toUpperCase();
}

function pickInitialLngLat(): { lng: number; lat: number } {
  if (lastLocatedLngLat.value) return { ...lastLocatedLngLat.value };
  const m = address.value.match(/定位坐标\(([-\d.]+)\s*,\s*([-\d.]+)\)/);
  if (m) {
    const lng = Number(m[1]);
    const lat = Number(m[2]);
    if (Number.isFinite(lng) && Number.isFinite(lat)) return { lng, lat };
  }
  return { lng: 116.397428, lat: 39.90923 };
}

function isChinaCoord(lng: number, lat: number): boolean {
  return lng >= 73 && lng <= 135 && lat >= 3 && lat <= 54;
}

async function reverseHumanAddress(lat: number, lng: number): Promise<string> {
  const key = getAmapWebServiceKey() || getAmapKey();
  if (!key) return `定位坐标(${lng.toFixed(6)}, ${lat.toFixed(6)})`;
  try {
    const resp = await fetch(
      `https://restapi.amap.com/v3/geocode/regeo?key=${encodeURIComponent(key)}&location=${encodeURIComponent(`${lng},${lat}`)}&extensions=base`
    );
    if (resp.ok) {
      const data = (await resp.json()) as { status?: string; regeocode?: { formatted_address?: string } };
      const addr = data.status === "1" ? data.regeocode?.formatted_address?.trim() : "";
      if (addr) return addr;
    }
  } catch {
    // fallback below
  }
  return `定位坐标(${lng.toFixed(6)}, ${lat.toFixed(6)})`;
}

async function openMapAdjust() {
  const key = getAmapKey();
  if (!key) {
    locationStatus.value = { tone: "error", message: "未配置 VITE_AMAP_KEY，无法打开地图微调" };
    return;
  }

  showMapAdjust.value = true;
  mapLoading.value = true;
  mapAdjustReady.value = false;
  adjustedAddress.value = "";
  adjustedLngLat.value = null;

  try {
    await nextTick();
    await loadAmapScript(key);
    const AMap = getWindowAMap() as any;
    let initial = pickInitialLngLat();
    // 若原始定位在境外（例如 IP 粗定位偏到美国），高德底图会只显示灰底；
    // 此时切换到国内城市中心，确保能看到道路/建筑后再拖拽微调。
    if (!isChinaCoord(initial.lng, initial.lat)) {
      try {
        const cityCenter = await new Promise<{ lng: number; lat: number } | null>((resolve) => {
          try {
            AMap.plugin(["AMap.CitySearch"], () => {
              const cs = new AMap.CitySearch();
              cs.getLocalCity((status: string, result: any) => {
                if (status !== "complete" || !result) return resolve(null);
                const center = result?.bounds?.getCenter?.();
                const lng = center?.lng ?? center?.getLng?.();
                const lat = center?.lat ?? center?.getLat?.();
                if (Number.isFinite(lng) && Number.isFinite(lat)) {
                  resolve({ lng, lat });
                } else {
                  resolve(null);
                }
              });
            });
          } catch {
            resolve(null);
          }
        });
        if (cityCenter) initial = cityCenter;
      } catch {
        // ignore and fallback to Beijing below
      }
      if (!isChinaCoord(initial.lng, initial.lat)) {
        initial = { lng: 116.397428, lat: 39.90923 };
      }
      locationStatus.value = {
        tone: "warn",
        message: "原始定位点在境外，已切换到国内底图中心，请在地图中拖拽到正确位置"
      };
    }
    if (!mapContainerRef.value) throw new Error("地图容器未就绪");

    if (adjustMap) {
      try {
        adjustMap.destroy();
      } catch {
        // ignore
      }
      adjustMap = null;
      adjustMarker = null;
    }

    const baseLayer = new AMap.TileLayer();
    const roadNetLayer = new AMap.TileLayer.RoadNet();
    const buildingsLayer = new AMap.Buildings({
      zooms: [16, 20],
      zIndex: 11,
      opacity: 0.95
    });

    adjustMap = new AMap.Map(mapContainerRef.value, {
      zoom: 16,
      center: [initial.lng, initial.lat],
      viewMode: "2D",
      mapStyle: "amap://styles/normal",
      features: ["bg", "road", "building", "point"],
      layers: [baseLayer, roadNetLayer]
    });
    adjustMap.add(buildingsLayer);
    adjustMarker = new AMap.Marker({ position: [initial.lng, initial.lat], draggable: true });
    adjustMap.add(adjustMarker);
    setTimeout(() => {
      try {
        adjustMap?.resize?.();
        adjustMap?.setFitView?.([adjustMarker], false, [90, 90, 90, 90], 17);
      } catch {
        // ignore
      }
    }, 80);

    adjustMap.on("complete", () => {
      try {
        adjustMap?.setFeatures?.(["bg", "road", "building", "point"]);
        adjustMap?.resize?.();
      } catch {
        // ignore
      }
    });

    adjustedLngLat.value = { ...initial };
    adjustedAddress.value = await reverseHumanAddress(initial.lat, initial.lng);
    mapAdjustReady.value = true;

    adjustMarker.on("dragend", async () => {
      const p = adjustMarker.getPosition?.();
      const lng = p?.lng ?? p?.getLng?.();
      const lat = p?.lat ?? p?.getLat?.();
      if (!Number.isFinite(lng) || !Number.isFinite(lat)) return;
      adjustedLngLat.value = { lng, lat };
      adjustedAddress.value = "正在解析地址...";
      adjustedAddress.value = await reverseHumanAddress(lat, lng);
    });
  } catch {
    locationStatus.value = { tone: "error", message: "地图加载失败，请检查高德 Key 或网络后重试" };
    showMapAdjust.value = false;
  } finally {
    mapLoading.value = false;
  }
}

function closeMapAdjust() {
  showMapAdjust.value = false;
}

function applyMapAdjust() {
  if (adjustedAddress.value) address.value = adjustedAddress.value;
  if (adjustedLngLat.value) {
    const { lng, lat } = adjustedLngLat.value;
    lastLocatedLngLat.value = { lng, lat };
    locationStatus.value = { tone: "success", message: `已根据地图微调更新地址（${lng.toFixed(6)}, ${lat.toFixed(6)}）` };
  } else {
    locationStatus.value = { tone: "success", message: "已根据地图微调更新地址" };
  }
  showMapAdjust.value = false;
}

async function fillAddressFromLocation() {
  if (locatingAddress.value) return;
  if (!("geolocation" in navigator)) {
    locationStatus.value = { tone: "error", message: "当前浏览器不支持定位，请手动输入地址" };
    return;
  }

  locatingAddress.value = true;
  locationStatus.value = { tone: "warn", message: "正在获取定位..." };
  const withTimeout = async <T,>(promise: Promise<T>, ms: number, message: string): Promise<T> => {
    let timer: ReturnType<typeof setTimeout> | null = null;
    try {
      return await Promise.race([
        promise,
        new Promise<T>((_, reject) => {
          timer = setTimeout(() => reject(new Error(message)), ms);
        })
      ]);
    } finally {
      if (timer) clearTimeout(timer);
    }
  };

  const getCurrentPosition = (opts: PositionOptions) =>
    new Promise<GeolocationPosition>((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject, opts);
    });

  const fetchJsonWithTimeout = async <T,>(url: string, ms = 6000): Promise<T | null> => {
    const c = new AbortController();
    const t = setTimeout(() => c.abort(), ms);
    try {
      const resp = await fetch(url, { signal: c.signal });
      if (!resp.ok) return null;
      return (await resp.json()) as T;
    } catch {
      return null;
    } finally {
      clearTimeout(t);
    }
  };

  const reverseByNominatim = async (lat: number, lng: number): Promise<string> => {
    const data = await fetchJsonWithTimeout<{
      display_name?: string;
      address?: Record<string, string | undefined>;
    }>(
      `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${encodeURIComponent(String(lat))}&lon=${encodeURIComponent(String(lng))}&accept-language=zh-CN,zh;q=0.9,en`,
      7000
    );
    if (!data) return "";
    const a = data.address ?? {};
    const compact = [a.country, a.state, a.city ?? a.town ?? a.county, a.suburb, a.road, a.house_number]
      .filter(Boolean)
      .join(" ");
    return compact || (data.display_name?.trim() ?? "");
  };

  const reverseByAmap = async (lat: number, lng: number): Promise<string> => {
    const key = getAmapWebServiceKey() || getAmapKey();
    if (!key) return "";
    const data = await fetchJsonWithTimeout<{
      status?: string;
      regeocode?: { formatted_address?: string };
    }>(
      `https://restapi.amap.com/v3/geocode/regeo?key=${encodeURIComponent(key)}&location=${encodeURIComponent(`${lng},${lat}`)}&extensions=base`,
      7000
    );
    if (!data) return "";
    if (data.status !== "1") return "";
    return data.regeocode?.formatted_address?.trim() ?? "";
  };

  const resolveHumanAddress = async (lat: number, lng: number): Promise<string> => {
    // 中国区域优先高德，其余区域走 Nominatim。
    const inChina = lng >= 73 && lng <= 135 && lat >= 3 && lat <= 54;
    if (inChina) {
      const amap = await reverseByAmap(lat, lng);
      if (amap) return amap;
    }
    return await reverseByNominatim(lat, lng);
  };

  try {
    let pos: GeolocationPosition;
    let bestAccuracy: number | null = null;
    try {
      // 第一次快速定位，优先拿到可用坐标
      pos = await withTimeout(
        getCurrentPosition({
          enableHighAccuracy: false,
          timeout: 7000,
          maximumAge: 120000
        }),
        8000,
        "定位超时"
      );
      bestAccuracy = Number.isFinite(pos.coords.accuracy) ? pos.coords.accuracy : null;
    } catch {
      // 首次失败再尝试高精度
      pos = await withTimeout(
        getCurrentPosition({
          enableHighAccuracy: true,
          timeout: 12000,
          maximumAge: 0
        }),
        13000,
        "定位超时"
      );
      bestAccuracy = Number.isFinite(pos.coords.accuracy) ? pos.coords.accuracy : null;
    }

    // 如果首次精度较差，自动进行高精度二次定位并择优
    if (bestAccuracy == null || bestAccuracy > 800) {
      locationStatus.value = {
        tone: "warn",
        message: bestAccuracy != null ? `当前定位精度约 ${Math.round(bestAccuracy)} 米，正在高精度重试...` : "正在高精度重试..."
      };
      try {
        const precisePos = await withTimeout(
          getCurrentPosition({
            enableHighAccuracy: true,
            timeout: 12000,
            maximumAge: 0
          }),
          13000,
          "高精度定位超时"
        );
        const preciseAccuracy = Number.isFinite(precisePos.coords.accuracy) ? precisePos.coords.accuracy : null;
        if (preciseAccuracy != null && (bestAccuracy == null || preciseAccuracy < bestAccuracy)) {
          pos = precisePos;
          bestAccuracy = preciseAccuracy;
        }
      } catch {
        // keep previous position
      }
    }

    const lat = pos.coords.latitude;
    const lng = pos.coords.longitude;
    lastLocatedLngLat.value = { lng, lat };
    address.value = `定位坐标(${lng.toFixed(6)}, ${lat.toFixed(6)})`;
    locationStatus.value = { tone: "warn", message: "已填充坐标，正在解析详细地址..." };

    let resolvedAddress = "";
    try {
      resolvedAddress = await resolveHumanAddress(lat, lng);
    } catch {
      // ignore reverse-geocode failure and fallback to coordinates
    }

    if (resolvedAddress) {
      address.value = resolvedAddress;
      locationStatus.value = {
        tone: "success",
        message: bestAccuracy != null
          ? `定位成功，已自动填充详细地址（精度约 ${Math.round(bestAccuracy)} 米）`
          : "定位成功，已自动填充详细地址"
      };
    } else {
      locationStatus.value = {
        tone: "warn",
        message: bestAccuracy != null
          ? `定位成功，但地址解析失败，已保留坐标信息（精度约 ${Math.round(bestAccuracy)} 米）`
          : "定位成功，但地址解析失败，已保留坐标信息"
      };
    }
  } catch (e) {
    const ge = e as GeolocationPositionError;
    if (ge?.code === ge.PERMISSION_DENIED) {
      locationStatus.value = { tone: "error", message: "你已拒绝定位权限，请在浏览器中允许位置访问后重试" };
    } else {
      // 兜底：IP 粗定位（无权限时可能失败）
      try {
        const ipData = await fetchJsonWithTimeout<{
          city?: string;
          region?: string;
          country_name?: string;
          latitude?: number;
          longitude?: number;
        }>("https://ipapi.co/json/", 5000);
        if (ipData) {
          const hasLatLng = Number.isFinite(ipData.latitude) && Number.isFinite(ipData.longitude);
          const coarseText = [ipData.country_name, ipData.region, ipData.city].filter(Boolean).join(" ");
          if (hasLatLng) {
            const lat = ipData.latitude as number;
            const lng = ipData.longitude as number;
            lastLocatedLngLat.value = { lng, lat };
            address.value = `IP定位(${lng.toFixed(4)}, ${lat.toFixed(4)})`;
            try {
              const resolvedAddress = await resolveHumanAddress(lat, lng);
              if (resolvedAddress) {
                address.value = resolvedAddress;
              } else if (coarseText) {
                address.value = coarseText;
              }
            } catch {
              if (coarseText) address.value = coarseText;
            }
          } else if (coarseText) {
            address.value = coarseText;
          }
          locationStatus.value = coarseText
            ? { tone: "warn", message: `GPS定位失败，已回退到IP粗定位：${coarseText}` }
            : { tone: "warn", message: "GPS定位失败，已尝试IP粗定位" };
        } else {
          locationStatus.value = { tone: "error", message: "定位失败，请检查网络/GPS后重试" };
        }
      } catch {
        locationStatus.value = { tone: "error", message: "定位失败，请检查网络/GPS后重试" };
      }
    }
  } finally {
    locatingAddress.value = false;
  }
}

async function onSubmit() {
  if (!assignedUserId.value.trim()) return alert("系统账号ID尚未生成，请稍后重试");
  if (!userName.value.trim()) return alert("请输入用户姓名");
  if (!password.value.trim()) return alert("请输入用户密码");

  if (role.value === "ELDER") {
    if (!gender.value) return alert("请选择性别");
    if (!address.value.trim()) return alert("请输入地址");
    if (age.value == null) return alert("请输入年龄");
  }

  if (role.value === "CHILD") {
    if (!gender.value) return alert("请选择性别");
    if (!address.value.trim()) return alert("请输入地址");
    if (age.value == null) return alert("请输入年龄");
  }

  if (role.value === "AGENCY") {
    if (!address.value.trim()) return alert("请输入机构地址");
  }

  try {
    const savedId = await registerUserServer({
      role: role.value,
      userName: userName.value.trim(),
      password: password.value,
      gender: gender.value || undefined,
      address: address.value.trim() || undefined,
      age: age.value,
      linkedElderId: undefined,
      linkedChildId: undefined
    });

    // 老人账号先注册成功后再写档案，避免在账号未入库时先写档案导致 500。
    if (role.value === "ELDER") {
      await upsertElderProfile(savedId, {
        name: userName.value.trim(),
        gender: gender.value,
        address: address.value.trim(),
        age: age.value as number,
        keyHealthNotes: ""
      });
    }
    alert(`注册成功。\n您的系统账号ID：${savedId}\n请妥善保存，返回登录后仍可在登录页输入姓名查看该 ID。`);
    router.push({ path: "/login" });
  } catch (e: any) {
    alert(e?.message ?? "注册失败");
  }
}
</script>

<style scoped>
.auth-shell {
  min-height: 100%;
  position: relative;
  padding: 24px 16px 40px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.auth-decor {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background: radial-gradient(1100px 500px at 8% 0%, rgba(31, 106, 165, 0.12), transparent 52%),
    radial-gradient(800px 400px at 100% 10%, rgba(46, 125, 50, 0.06), transparent 45%),
    linear-gradient(180deg, #eef1f6 0%, #f5f6f8 100%);
}

.auth-panel {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 520px;
}

.auth-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 18px;
  font-size: 13px;
}

.text-link {
  border: 0;
  background: none;
  padding: 0;
  font-weight: 800;
  color: var(--elder-brand, #1f6aa5);
  cursor: pointer;
}
.text-link:hover {
  text-decoration: underline;
  text-underline-offset: 3px;
}

.sep {
  color: rgba(26, 35, 50, 0.28);
  user-select: none;
}

.auth-header {
  margin-bottom: 16px;
}

.auth-kicker {
  margin: 0 0 6px;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.12em;
  color: rgba(26, 35, 50, 0.45);
  text-transform: uppercase;
}

.auth-title {
  margin: 0 0 8px;
  font-size: 1.65rem;
  font-weight: 1000;
  letter-spacing: -0.03em;
  color: #1a2332;
}

.auth-sub {
  margin: 0;
  font-size: 13.5px;
  font-weight: 650;
  color: rgba(26, 35, 50, 0.5);
  line-height: 1.45;
}

.card {
  background: #fff;
  padding: 20px 20px 22px;
  border-radius: var(--elder-radius-lg, 18px);
  border: 1px solid rgba(26, 35, 50, 0.08);
  box-shadow: var(--elder-shadow, 0 4px 28px rgba(26, 35, 50, 0.07));
}

.row {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 13px;
}

.input-with-action {
  flex: 1;
  min-width: 0;
  display: flex;
  gap: 8px;
  align-items: center;
}

label {
  width: 100px;
  flex-shrink: 0;
  font-weight: 800;
  color: rgba(26, 35, 50, 0.72);
  font-size: 14px;
}

input,
select {
  flex: 1;
  min-width: 0;
  padding: 11px 13px;
  border-radius: var(--elder-radius-md, 12px);
  border: 1px solid rgba(26, 35, 50, 0.12);
  background: #fafbfc;
  font-size: 14px;
  font-weight: 650;
  color: #1a2332;
  transition: border-color 0.15s ease, box-shadow 0.15s ease, background 0.15s ease;
}

input:hover,
select:hover {
  border-color: rgba(31, 106, 165, 0.22);
}

input:focus,
select:focus {
  outline: none;
  border-color: rgba(31, 106, 165, 0.5);
  box-shadow: 0 0 0 3px rgba(31, 106, 165, 0.1);
  background: #fff;
}

.loc-btn {
  flex-shrink: 0;
  border: 1px solid rgba(31, 106, 165, 0.22);
  background: rgba(31, 106, 165, 0.06);
  color: var(--elder-brand, #1f6aa5);
  border-radius: 12px;
  padding: 10px 12px;
  font-size: 12px;
  font-weight: 900;
  cursor: pointer;
  transition: filter 0.15s ease, opacity 0.15s ease;
}

.loc-btn:hover:not(:disabled) {
  filter: brightness(1.04);
}

.loc-btn:disabled {
  opacity: 0.65;
  cursor: default;
}

.map-btn {
  flex-shrink: 0;
  border: 1px solid rgba(106, 76, 247, 0.28);
  background: rgba(106, 76, 247, 0.08);
  color: #5b2ce0;
  border-radius: 12px;
  padding: 10px 12px;
  font-size: 12px;
  font-weight: 900;
  cursor: pointer;
}

.map-adjust-overlay {
  position: fixed;
  inset: 0;
  background: rgba(12, 16, 24, 0.45);
  z-index: 90;
  display: grid;
  place-items: center;
  padding: 14px;
}

.map-adjust-card {
  width: min(560px, 100%);
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(26, 35, 50, 0.1);
  padding: 12px;
}

.map-adjust-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.map-close-btn {
  border: 0;
  background: transparent;
  font-size: 18px;
  cursor: pointer;
}

.map-adjust-tip {
  margin: 8px 0;
  font-size: 12px;
  color: rgba(26, 35, 50, 0.62);
}

.map-adjust-canvas {
  height: 320px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(26, 35, 50, 0.1);
  position: relative;
}

.map-loading {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.7);
  font-weight: 800;
}

.map-adjust-result {
  margin: 10px 0;
  font-size: 13px;
  font-weight: 700;
  color: rgba(26, 35, 50, 0.75);
}

.map-adjust-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.loc-status {
  margin: -6px 0 12px 108px;
  font-size: 12px;
  font-weight: 750;
  line-height: 1.45;
}

.loc-status.is-success {
  color: #1b7f3a;
}

.loc-status.is-warn {
  color: #7b5e00;
}

.loc-status.is-error {
  color: #b42318;
}

.btn-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 10px;
}

.btn-primary {
  padding: 13px 14px;
  border-radius: 14px;
  border: 0;
  background: linear-gradient(180deg, #2478b8 0%, #1f6aa5 100%);
  color: white;
  font-weight: 900;
  font-size: 16px;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(31, 106, 165, 0.32);
  transition: transform 0.15s ease, filter 0.15s ease;
}

.btn-primary:hover {
  filter: brightness(1.04);
  transform: translateY(-1px);
}

.btn-secondary {
  padding: 13px 14px;
  border-radius: 14px;
  border: 1px solid rgba(26, 35, 50, 0.12);
  background: #fff;
  color: #1a2332;
  font-weight: 900;
  font-size: 16px;
  cursor: pointer;
  transition: border-color 0.15s ease, background 0.15s ease;
}

.btn-secondary:hover {
  border-color: rgba(31, 106, 165, 0.3);
  background: rgba(31, 106, 165, 0.04);
}

.divider {
  height: 1px;
  background: rgba(26, 35, 50, 0.08);
  margin: 12px 0 14px;
}

.id-row label {
  align-self: center;
}

.readonly-input {
  flex: 1;
  font-weight: 900;
  font-size: 16px;
  color: #1f6aa5;
  background: rgba(31, 106, 165, 0.07) !important;
  cursor: default;
  border-style: dashed !important;
}

.id-note {
  margin: -4px 0 14px;
  padding-left: 108px;
  font-size: 12px;
  color: rgba(26, 35, 50, 0.48);
  line-height: 1.5;
  font-weight: 650;
}

@media (max-width: 560px) {
  .id-note {
    padding-left: 0;
  }

  .loc-status {
    margin-left: 0;
  }
}
</style>

