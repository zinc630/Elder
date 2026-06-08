import axios from "axios";
import http from "./http";

function explainLocationApiError(e: unknown): string {
  if (axios.isAxiosError(e)) {
    const st = e.response?.status;
    if (st === 404) {
      return (
        "位置接口返回 404：请确认已重新编译并启动后端（Spring Boot 8081），" +
        "且前端使用 npm run dev 或 npm run preview（需配置 /api 代理到后端）。不要直接用浏览器打开 dist/index.html。"
      );
    }
    if (!e.response) {
      return "无法连接后端（网络或后端未启动）。请启动 Elder/backend 并检查端口 8081。";
    }
    return e.response.data?.message ?? e.message ?? "请求失败";
  }
  return (e as Error)?.message ?? "请求失败";
}

export type GeofenceDto = {
  centerLat: number;
  centerLng: number;
  radiusMeters: number;
  label: string;
};

export type LocationPointDto = {
  lat: number;
  lng: number;
  accuracyMeters: number | null;
  source: string;
  updatedAtIso: string;
};

export type ElderLocationSnapshot = {
  elderId: string;
  elderLocation: LocationPointDto | null;
  geofence: GeofenceDto | null;
  insideFence: boolean;
  distanceToFenceCenterMeters: number | null;
};

export async function getElderLocationSnapshot(elderId: string): Promise<ElderLocationSnapshot> {
  try {
    const resp = await http.get(`/v1/elders/${encodeURIComponent(elderId)}/location`);
    return resp.data.data;
  } catch (e) {
    throw new Error(explainLocationApiError(e));
  }
}

export async function reportElderLocation(
  elderId: string,
  body: { lat: number; lng: number; accuracyMeters?: number | null; source?: string }
): Promise<ElderLocationSnapshot> {
  try {
    const resp = await http.post(`/v1/elders/${encodeURIComponent(elderId)}/location`, {
      lat: body.lat,
      lng: body.lng,
      accuracyMeters: body.accuracyMeters ?? null,
      source: body.source ?? "browser_geolocation"
    });
    return resp.data.data;
  } catch (e) {
    throw new Error(explainLocationApiError(e));
  }
}

export async function ensureDefaultGeofence(elderId: string): Promise<ElderLocationSnapshot> {
  try {
    const resp = await http.post(`/v1/elders/${encodeURIComponent(elderId)}/location/geofence/default`);
    return resp.data.data;
  } catch (e) {
    throw new Error(explainLocationApiError(e));
  }
}
