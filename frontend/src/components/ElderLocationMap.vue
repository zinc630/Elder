<template>
  <div class="eld-map">
    <p v-if="fenceHint" class="fence-hint" :class="{ out: !snapshot?.insideFence && snapshot?.elderLocation }">
      {{ fenceHint }}
    </p>
    <p v-if="providerBanner" class="provider-banner">{{ providerBanner }}</p>
    <div class="toolbar">
      <button type="button" class="btn" :disabled="loading" @click="load">
        {{ loading ? "加载中…" : "刷新位置" }}
      </button>
      <button type="button" class="btn primary" :disabled="locating" @click="locateMe">
        {{ locating ? "定位中…" : "定位我的位置" }}
      </button>
      <button
        type="button"
        class="btn accent"
        :disabled="!lastBrowserPos || syncing"
        title="演示：把浏览器定位结果写入当前长者的服务端位置"
        @click="syncBrowserPosToElder"
      >
        {{ syncing ? "同步中…" : "同步到长者位置（演示）" }}
      </button>
      <button
        type="button"
        class="btn"
        :disabled="ensuringFence"
        title="当未配置围栏时，按最近定位自动补建默认围栏"
        @click="repairDefaultFence"
      >
        {{ ensuringFence ? "补建中…" : "补建默认围栏" }}
      </button>
      <button
        v-if="mapBackend === 'amap'"
        type="button"
        class="btn"
        title="高德底图异常时可切换兼容底图"
        @click="switchToLeafletFallback"
      >
        切换兼容底图
      </button>
      <span class="status">{{ statusText }}</span>
    </div>
    <p v-if="err" class="err">{{ err }}</p>
    <div ref="mapContainer" class="map-root" :style="{ height: height }" />
    <p class="footnote">{{ footnote }}</p>
  </div>
</template>

<script setup lang="ts">
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
import type { ElderLocationSnapshot } from "../api/location";
import { ensureDefaultGeofence, getElderLocationSnapshot, reportElderLocation } from "../api/location";
import {
  amapPlugin,
  getAmapKey,
  getWindowAMap,
  loadAmapScript,
  type AMapLike,
  type AMapMapInstance,
  type AMapOverlay,
  type AMapPlaceSearch,
  type AMapPoi
} from "../utils/amapLoader";
import { wgs84ToGcj02 } from "../utils/coordChina";
import { createGaodeRoadTileLayer, createOsmTileLayer } from "../utils/leafletGaodeTiles";

const props = withDefaults(
  defineProps<{
    elderId: string;
    height?: string;
  }>(),
  { height: "320px" }
);

type Backend = "leaflet" | "amap";

const mapContainer = ref<HTMLElement | null>(null);
const mapBackend = ref<Backend>("leaflet");
/** Leaflet 下：高德路网瓦片需 GCJ-02 叠加；切 OSM 后改回 WGS84 */
const leafletTileKind = ref<"gaode" | "osm">("gaode");

let leafletMap: L.Map | null = null;
let leafletFence: L.Circle | null = null;
let leafletElder: L.Marker | null = null;
let leafletMe: L.Marker | null = null;
const leafletPoiMarkers: L.Marker[] = [];

let amapMap: AMapMapInstance | null = null;
let amapCircle: AMapOverlay | null = null;
let amapElder: AMapOverlay | null = null;
let amapMe: AMapOverlay | null = null;
const amapPoiMarkers: AMapOverlay[] = [];
let amapPlaceSearch: AMapPlaceSearch | null = null;

const snapshot = ref<ElderLocationSnapshot | null>(null);
const err = ref("");
const loading = ref(false);
const locating = ref(false);
const syncing = ref(false);
const statusText = ref("");
const providerBanner = ref("");
const lastBrowserPos = ref<{ lat: number; lng: number; accuracy: number | null } | null>(null);
const ensuringFence = ref(false);

const footnote = computed(() => {
  if (mapBackend.value === "amap") {
    return "底图与周边 POI 由高德 JS API 提供（已配置 Key）；坐标已按 WGS84→GCJ-02 纠偏。";
  }
  if (leafletTileKind.value === "gaode") {
    return "底图使用高德公开路网瓦片（无需 Key，样式接近国内导航图）；蓝点为演示周边兴趣点。配置 VITE_AMAP_KEY 可启用官方 JS API 与实时 POI 检索。";
  }
  return "当前为 OpenStreetMap。若瓦片加载失败会自动切换至此；在国内可配置 VITE_AMAP_KEY 使用高德 JS API。";
});

const fenceHint = computed(() => {
  const s = snapshot.value;
  if (!s?.geofence) return "暂无电子围栏数据；新注册长者会在建档时生成默认围栏（演示）。";
  if (!s.elderLocation) return `电子围栏：${s.geofence.label}（暂无长者定位点）`;
  return s.insideFence
    ? `在围栏内：${s.geofence.label}`
    : `已偏离围栏：${s.geofence.label}（距中心约 ${s.distanceToFenceCenterMeters != null ? Math.round(s.distanceToFenceCenterMeters) : "—"} m）`;
});

function divIcon(label: string, kind: "elder" | "me") {
  return L.divIcon({
    className: `eld-marker eld-marker--${kind}`,
    html: `<span>${label}</span>`,
    iconSize: [48, 28],
    iconAnchor: [24, 28]
  });
}

function clearLeafletLayers() {
  if (leafletFence && leafletMap) {
    leafletMap.removeLayer(leafletFence);
    leafletFence = null;
  }
  if (leafletElder && leafletMap) {
    leafletMap.removeLayer(leafletElder);
    leafletElder = null;
  }
  if (leafletMap) {
    for (const m of leafletPoiMarkers) {
      leafletMap.removeLayer(m);
    }
    leafletPoiMarkers.length = 0;
  }
}

function leafletPoiDivIcon(title: string) {
  return L.divIcon({
    className: "leaflet-poi-dummy",
    html: `<div class="poi-pin"><div class="poi-dot"></div><div class="poi-txt">${title}</div><div class="poi-sub">演示</div></div>`,
    iconSize: [96, 44],
    iconAnchor: [48, 40]
  });
}

const LEAFLET_DEMO_POIS = [
  { name: "社区卫生服务站", dlat: 0.0022, dlng: 0.0011 },
  { name: "连锁便利店", dlat: -0.0018, dlng: 0.0009 },
  { name: "公交站点", dlat: 0.0009, dlng: -0.0014 },
  { name: "街心花园", dlat: -0.0025, dlng: -0.0006 },
  { name: "药店", dlat: 0.0015, dlng: 0.002 },
  { name: "社区食堂", dlat: -0.0008, dlng: 0.0018 },
  { name: "银行网点", dlat: 0.0028, dlng: -0.001 },
  { name: "快递自提点", dlat: -0.0012, dlng: -0.002 }
] as const;

/** 将后端 WGS84 点转为当前 Leaflet 底图所用坐标系下的 [lat, lng] */
function leafletDisplayLatLng(wgsLng: number, wgsLat: number): [number, number] {
  if (leafletTileKind.value === "gaode") {
    const [lng, lat] = toGcj(wgsLng, wgsLat);
    return [lat, lng];
  }
  return [wgsLat, wgsLng];
}

function addLeafletDemoPois(centerLat: number, centerLng: number) {
  if (!leafletMap) return;
  for (const d of LEAFLET_DEMO_POIS) {
    const m = L.marker([centerLat + d.dlat, centerLng + d.dlng], {
      icon: leafletPoiDivIcon(d.name)
    }).addTo(leafletMap);
    m.bindPopup(`<strong>${d.name}</strong><br/><span style="font-size:12px;color:#666">演示周边兴趣点</span>`);
    leafletPoiMarkers.push(m);
  }
}

function clearAmapMain() {
  if (amapMap && amapCircle) {
    amapMap.remove(amapCircle);
    amapCircle = null;
  }
  if (amapMap && amapElder) {
    amapMap.remove(amapElder);
    amapElder = null;
  }
  if (amapMap) {
    for (const m of amapPoiMarkers) {
      amapMap.remove(m);
    }
    amapPoiMarkers.length = 0;
  }
}

function clearAmapMe() {
  if (amapMap && amapMe) {
    amapMap.remove(amapMe);
    amapMe = null;
  }
}

async function load() {
  if (!props.elderId) return;
  err.value = "";
  loading.value = true;
  try {
    snapshot.value = await getElderLocationSnapshot(props.elderId);
    if (!snapshot.value.geofence) {
      try {
        snapshot.value = await ensureDefaultGeofence(props.elderId);
      } catch {
        // keep old behavior and allow manual repair from toolbar
      }
    }
    applyLayers();
    const poiHint =
      mapBackend.value === "leaflet" && leafletTileKind.value === "gaode"
        ? ` · 演示周边 POI ${LEAFLET_DEMO_POIS.length} 个`
        : "";
    statusText.value = snapshot.value.elderLocation
      ? `长者位置已更新：${new Date(snapshot.value.elderLocation.updatedAtIso).toLocaleString()}${poiHint}`
      : `暂无长者定位记录${poiHint || "（围栏与演示 POI 仍显示）"}`;
  } catch (e: unknown) {
    err.value = (e as { message?: string })?.message ?? "加载位置失败";
  } finally {
    loading.value = false;
  }
}

async function repairDefaultFence() {
  if (!props.elderId) return;
  ensuringFence.value = true;
  err.value = "";
  try {
    snapshot.value = await ensureDefaultGeofence(props.elderId);
    applyLayers();
    statusText.value = "已补建默认围栏";
  } catch (e: unknown) {
    err.value = (e as { message?: string })?.message ?? "补建默认围栏失败";
  } finally {
    ensuringFence.value = false;
  }
}

/** 后端统一按 WGS84 存经纬度；高德底图用 GCJ-02 */
function toGcj(lng: number, lat: number): [number, number] {
  return wgs84ToGcj02(lng, lat);
}

function applyLayers() {
  if (!snapshot.value) return;
  if (mapBackend.value === "leaflet" && leafletMap) applyLeafletLayers();
  if (mapBackend.value === "amap" && amapMap) void applyAmapLayers();
}

function applyLeafletLayers() {
  if (!leafletMap || !snapshot.value) return;
  clearLeafletLayers();
  const s = snapshot.value;
  const bounds: L.LatLngExpression[] = [];

  if (s.geofence) {
    const g = s.geofence;
    const [clat, clng] = leafletDisplayLatLng(g.centerLng, g.centerLat);
    leafletFence = L.circle([clat, clng], {
      radius: g.radiusMeters,
      color: "#1f6aa5",
      fillColor: "#1f6aa5",
      fillOpacity: 0.12,
      weight: 2
    }).addTo(leafletMap);
    leafletFence.bindTooltip(g.label, { sticky: true });
    bounds.push([clat, clng]);
  }

  if (s.elderLocation) {
    const p = s.elderLocation;
    const [plat, plng] = leafletDisplayLatLng(p.lng, p.lat);
    leafletElder = L.marker([plat, plng], { icon: divIcon("长者", "elder") }).addTo(leafletMap);
    leafletElder.bindPopup(
      `${p.source}<br/>${new Date(p.updatedAtIso).toLocaleString()}<br/>${p.accuracyMeters != null ? `精度约 ${Math.round(p.accuracyMeters)} m` : ""}`
    );
    bounds.push([plat, plng]);
  }

  let poiCenterLat: number;
  let poiCenterLng: number;
  if (s.elderLocation) {
    [poiCenterLat, poiCenterLng] = leafletDisplayLatLng(s.elderLocation.lng, s.elderLocation.lat);
  } else if (s.geofence) {
    [poiCenterLat, poiCenterLng] = leafletDisplayLatLng(s.geofence.centerLng, s.geofence.centerLat);
  } else {
    [poiCenterLat, poiCenterLng] = leafletDisplayLatLng(121.4737, 31.2304);
  }
  addLeafletDemoPois(poiCenterLat, poiCenterLng);
  for (const m of leafletPoiMarkers) {
    bounds.push(m.getLatLng());
  }

  if (bounds.length > 0) {
    leafletMap.fitBounds(L.latLngBounds(bounds), { padding: [40, 40], maxZoom: 16 });
  } else {
    const [dlat, dlng] = leafletDisplayLatLng(121.4737, 31.2304);
    leafletMap.setView([dlat, dlng], 11);
  }
}

function buildPoiMarkerHtml(name: string, type?: string) {
  const sub = type ? `<div class="poi-sub">${type}</div>` : "";
  return `<div class="poi-pin"><div class="poi-dot"></div><div class="poi-txt">${name}</div>${sub}</div>`;
}

async function applyAmapLayers() {
  const AMap = getWindowAMap() as AMapLike;
  if (!amapMap || !snapshot.value) return;
  clearAmapMain();

  const s = snapshot.value;
  const overlays: AMapOverlay[] = [];

  if (s.geofence) {
    const g = s.geofence;
    const [clng, clat] = toGcj(g.centerLng, g.centerLat);
    amapCircle = new AMap.Circle({
      center: [clng, clat],
      radius: g.radiusMeters,
      strokeColor: "#1f6aa5",
      strokeWeight: 2,
      fillColor: "#1f6aa5",
      fillOpacity: 0.14
    });
    amapMap.add(amapCircle);
    overlays.push(amapCircle);
  }

  if (s.elderLocation) {
    const p = s.elderLocation;
    const [lng, lat] = toGcj(p.lng, p.lat);
    amapElder = new AMap.Marker({
      position: [lng, lat],
      title: "长者位置",
      content: buildPoiMarkerHtml("长者", p.source),
      offset: new AMap.Pixel(0, 0),
      anchor: "bottom-center"
    });
    amapMap.add(amapElder);
    overlays.push(amapElder);
    (amapElder as unknown as { on?: (ev: string, fn: () => void) => void }).on?.("click", () => {
      const iw = new (AMap as unknown as { InfoWindow: new (o: Record<string, unknown>) => { open: (m: unknown, pos: unknown) => void } }).InfoWindow({
        content: `<div style="padding:8px 10px;font-size:13px;line-height:1.45">${p.source}<br/>${new Date(p.updatedAtIso).toLocaleString()}</div>`
      });
      iw.open(amapMap, [lng, lat]);
    });
  }

  if (overlays.length > 0) {
    amapMap.setFitView(overlays, false, [48, 48, 48, 48], 17);
  } else {
    const c = toGcj(121.4737, 31.2304);
    amapMap.setZoomAndCenter(11, c);
  }

  await searchNearbyPois(AMap, s);
}

async function searchNearbyPois(AMap: AMapLike, s: ElderLocationSnapshot) {
  if (!amapMap || !amapPlaceSearch) return;

  let lng: number;
  let lat: number;
  if (s.elderLocation) {
    [lng, lat] = toGcj(s.elderLocation.lng, s.elderLocation.lat);
  } else if (s.geofence) {
    [lng, lat] = toGcj(s.geofence.centerLng, s.geofence.centerLat);
  } else {
    [lng, lat] = toGcj(121.4737, 31.2304);
  }

  const radius = Math.min(1200, Math.max(400, s.geofence ? s.geofence.radiusMeters + 400 : 800));

  amapPlaceSearch.searchNearBy(
    "餐饮服务|购物服务|生活服务|医疗保健服务|风景名胜|交通设施服务|住宿服务|商务住宅|政府机构及社会团体|科教文化服务",
    [lng, lat],
    radius,
    (status, result) => {
      if (status !== "complete" || result.info !== "OK" || !amapMap) return;
      const pois = result.poiList?.pois ?? [];
      const slice = pois.slice(0, 35);
      for (const poi of slice) {
        addOnePoiMarker(AMap, poi);
      }
      if (slice.length > 0) {
        statusText.value = `已加载周边兴趣点 ${slice.length} 个（高德 POI）`;
      }
    }
  );
}

function addOnePoiMarker(AMap: AMapLike, poi: AMapPoi) {
  if (!amapMap || !poi.location) return;
  const { lng, lat } = poi.location;
  const marker = new AMap.Marker({
    position: [lng, lat],
    title: poi.name,
    content: buildPoiMarkerHtml(poi.name, poi.type),
    anchor: "bottom-center",
    zIndex: 80
  });
  amapMap.add(marker);
  amapPoiMarkers.push(marker);
  (marker as unknown as { on?: (ev: string, fn: () => void) => void }).on?.("click", () => {
    const addr = poi.address ? `<div style="color:#666;font-size:12px;margin-top:4px">${poi.address}</div>` : "";
    const iw = new (AMap as unknown as { InfoWindow: new (o: Record<string, unknown>) => { open: (m: unknown, pos: unknown) => void } }).InfoWindow({
      content: `<div style="padding:8px 10px;max-width:220px"><strong>${poi.name}</strong>${addr}</div>`
    });
    iw.open(amapMap, [lng, lat]);
  });
}

function initLeaflet() {
  if (!mapContainer.value || leafletMap) return;
  mapBackend.value = "leaflet";
  leafletTileKind.value = "gaode";
  leafletMap = L.map(mapContainer.value, { zoomControl: true });

  const gaode = createGaodeRoadTileLayer();
  let tileErr = 0;
  gaode.on("tileerror", () => {
    tileErr++;
    if (tileErr < 8 || !leafletMap) return;
    if (leafletTileKind.value !== "gaode") return;
    leafletMap.removeLayer(gaode);
    createOsmTileLayer().addTo(leafletMap);
    leafletTileKind.value = "osm";
    providerBanner.value = "高德路网瓦片暂时无法加载，已切换 OpenStreetMap。";
    if (snapshot.value) applyLayers();
  });
  gaode.addTo(leafletMap);

  const [dlat, dlng] = toGcj(121.4737, 31.2304);
  leafletMap.setView([dlat, dlng], 11);
}

async function initAmap() {
  const key = getAmapKey();
  if (!key || !mapContainer.value) return false;
  await loadAmapScript(key);
  await nextTick();
  if (!mapContainer.value) return false;

  const AMap = getWindowAMap();
  if (!AMap) return false;

  if (leafletMap) {
    leafletMap.remove();
    leafletMap = null;
    leafletFence = null;
    leafletElder = null;
    leafletMe = null;
  }

  try {
    amapMap = new AMap.Map(mapContainer.value, {
      zoom: 14,
      center: [121.4737, 31.2304],
      viewMode: "2D",
      mapStyle: "amap://styles/normal"
    });

    await amapPlugin(AMap, ["AMap.PlaceSearch", "AMap.InfoWindow"]);
    amapPlaceSearch = new AMap.PlaceSearch({
      pageSize: 30,
      pageIndex: 1,
      city: "",
      citylimit: false
    });

    mapBackend.value = "amap";
    providerBanner.value = "";
    return true;
  } catch {
    if (amapMap) {
      try {
        amapMap.destroy();
      } catch {
        /* ignore */
      }
      amapMap = null;
    }
    amapPlaceSearch = null;
    throw new Error("amap_init_failed");
  }
}

function switchToLeafletFallback() {
  if (mapBackend.value !== "amap") return;
  if (amapMap) {
    amapMap.destroy();
    amapMap = null;
  }
  amapCircle = null;
  amapElder = null;
  amapMe = null;
  amapPlaceSearch = null;
  if (mapContainer.value) mapContainer.value.innerHTML = "";
  providerBanner.value = "已切换至 Leaflet 兼容底图。";
  initLeaflet();
  if (snapshot.value) applyLayers();
}

function locateMe() {
  if (!navigator.geolocation) {
    statusText.value = "当前浏览器不支持定位";
    return;
  }
  locating.value = true;
  statusText.value = "正在请求浏览器定位权限…";
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      locating.value = false;
      const lat = pos.coords.latitude;
      const lng = pos.coords.longitude;
      const accuracy = pos.coords.accuracy != null ? pos.coords.accuracy : null;
      lastBrowserPos.value = { lat, lng, accuracy };

      if (mapBackend.value === "leaflet" && leafletMap) {
        if (leafletMe) leafletMap.removeLayer(leafletMe);
        const [mlat, mlng] =
          leafletTileKind.value === "gaode" ? leafletDisplayLatLng(lng, lat) : [lat, lng];
        leafletMe = L.marker([mlat, mlng], { icon: divIcon("我", "me") }).addTo(leafletMap);
        leafletMe.bindPopup("您的当前位置（浏览器定位；底图为高德路网时已做坐标纠偏）");
        leafletMap.panTo([mlat, mlng]);
      } else if (mapBackend.value === "amap" && amapMap) {
        clearAmapMe();
        const [glng, glat] = toGcj(lng, lat);
        const AMap = getWindowAMap() as AMapLike;
        amapMe = new AMap.Marker({
          position: [glng, glat],
          title: "我的位置",
          content: buildPoiMarkerHtml("我", "浏览器定位"),
          anchor: "bottom-center",
          zIndex: 120
        });
        amapMap.add(amapMe);
        amapMap.setZoomAndCenter(16, [glng, glat]);
      }
      statusText.value = `已获取您的位置：${lat.toFixed(5)}, ${lng.toFixed(5)}（WGS84）`;
    },
    (geoErr) => {
      locating.value = false;
      statusText.value = geoErr.message || "定位失败（请检查系统/浏览器位置权限）";
    },
    { enableHighAccuracy: true, timeout: 20000, maximumAge: 0 }
  );
}

async function syncBrowserPosToElder() {
  const p = lastBrowserPos.value;
  if (!p || !props.elderId) return;
  syncing.value = true;
  err.value = "";
  try {
    snapshot.value = await reportElderLocation(props.elderId, {
      lat: p.lat,
      lng: p.lng,
      accuracyMeters: p.accuracy,
      source: "browser_geolocation_sync"
    });
    applyLayers();
    statusText.value = "已将浏览器定位（WGS84）同步为长者最新位置";
  } catch (e: unknown) {
    err.value = (e as { message?: string })?.message ?? "同步失败";
  } finally {
    syncing.value = false;
  }
}

function destroyMap() {
  clearLeafletLayers();
  if (leafletMe && leafletMap) {
    leafletMap.removeLayer(leafletMe);
    leafletMe = null;
  }
  if (leafletMap) {
    leafletMap.remove();
    leafletMap = null;
  }

  clearAmapMain();
  clearAmapMe();
  if (amapMap) {
    amapMap.destroy();
    amapMap = null;
  }
  amapPlaceSearch = null;
}

onMounted(async () => {
  await nextTick();
  const key = getAmapKey();
  if (key) {
    try {
      const ok = await initAmap();
      if (!ok) throw new Error("init failed");
    } catch {
      if (mapContainer.value) mapContainer.value.innerHTML = "";
      providerBanner.value = "高德 JS API 加载失败，已改用 Leaflet + 路网瓦片。请检查 Key、安全密钥与网络。";
      initLeaflet();
    }
  } else {
    providerBanner.value = "";
    initLeaflet();
  }
  await load();
});

watch(
  () => props.elderId,
  async () => {
    lastBrowserPos.value = null;
    if (leafletMe && leafletMap) {
      leafletMap.removeLayer(leafletMe);
      leafletMe = null;
    }
    clearAmapMe();
    await load();
  }
);

onBeforeUnmount(() => {
  destroyMap();
});
</script>

<style scoped>
.eld-map {
  width: 100%;
}
.provider-banner {
  margin: 0 0 10px;
  padding: 8px 10px;
  border-radius: 8px;
  background: rgba(31, 106, 165, 0.08);
  color: #1565a8;
  font-size: 12px;
  font-weight: 800;
  line-height: 1.45;
}
.fence-hint {
  margin: 0 0 10px;
  font-size: 13px;
  font-weight: 700;
  color: rgba(0, 0, 0, 0.65);
}
.fence-hint.out {
  color: #c62828;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 10px;
}
.btn {
  padding: 8px 12px;
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  background: #fff;
  font-weight: 800;
  font-size: 13px;
  cursor: pointer;
}
.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.btn.primary {
  border-color: #1f6aa5;
  color: #1f6aa5;
}
.btn.accent {
  border-color: #2e7d32;
  color: #2e7d32;
}
.status {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.5);
  flex: 1 1 160px;
}
.err {
  color: #c62828;
  font-size: 13px;
  margin: 0 0 8px;
}
.map-root {
  width: 100%;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  overflow: hidden;
  z-index: 0;
}
.footnote {
  margin: 8px 0 0;
  font-size: 11px;
  color: rgba(0, 0, 0, 0.45);
}
</style>

<style>
.eld-marker {
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  display: flex !important;
  align-items: center;
  justify-content: center;
}
.eld-marker span {
  font-size: 12px;
  font-weight: 900;
  padding: 2px 8px;
}
.eld-marker--elder span {
  color: #c62828;
}
.eld-marker--me span {
  color: #1565c0;
}
.poi-pin {
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 120px;
  pointer-events: auto;
}
.poi-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #1890ff;
  border: 2px solid #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.25);
  flex-shrink: 0;
}
.poi-txt {
  margin-top: 2px;
  font-size: 10px;
  font-weight: 800;
  color: rgba(0, 0, 0, 0.78);
  text-align: center;
  line-height: 1.2;
  max-width: 118px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  background: rgba(255, 255, 255, 0.92);
  padding: 1px 4px;
  border-radius: 4px;
  border: 1px solid rgba(24, 144, 255, 0.35);
}
.poi-sub {
  font-size: 9px;
  color: rgba(0, 0, 0, 0.45);
  max-width: 118px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.leaflet-poi-dummy {
  background: transparent !important;
  border: none !important;
}
</style>
