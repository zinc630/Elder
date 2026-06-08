const SCRIPT_ID = "elder-amap-js-v2";

export function getAmapKey(): string {
  return (import.meta.env.VITE_AMAP_KEY as string | undefined)?.trim() ?? "";
}

/** Web 服务 Key（用于 REST 逆地理等接口）。 */
export function getAmapWebServiceKey(): string {
  return (import.meta.env.VITE_AMAP_WEB_SERVICE_KEY as string | undefined)?.trim() ?? "";
}

/** 部分高德 Key 需在控制台绑定「安全密钥」，在加载脚本前注入 */
export function getAmapSecurityCode(): string {
  return (import.meta.env.VITE_AMAP_SECURITY_JSCODE as string | undefined)?.trim() ?? "";
}

export function loadAmapScript(key: string): Promise<void> {
  if (typeof window === "undefined") return Promise.resolve();
  const w = window as Window & { AMap?: unknown };
  if (w.AMap) return Promise.resolve();

  const sec = getAmapSecurityCode();
  if (sec) {
    (window as Window & { _AMapSecurityConfig?: { securityJsCode: string } })._AMapSecurityConfig = {
      securityJsCode: sec
    };
  }

  return new Promise((resolve, reject) => {
    const existing = document.getElementById(SCRIPT_ID) as HTMLScriptElement | null;
    if (existing) {
      const t0 = Date.now();
      const wait = () => {
        if ((window as Window & { AMap?: unknown }).AMap) resolve();
        else if (Date.now() - t0 > 20000) reject(new Error("高德脚本加载超时"));
        else setTimeout(wait, 50);
      };
      wait();
      return;
    }

    const script = document.createElement("script");
    script.id = SCRIPT_ID;
    script.async = true;
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${encodeURIComponent(key)}`;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error("无法加载高德地图脚本（网络或 Key）"));
    document.head.appendChild(script);
  });
}

/** AMap.plugin 封装 */
export function amapPlugin(amap: AMapLike, names: string[]): Promise<void> {
  return new Promise((resolve, reject) => {
    try {
      amap.plugin(names, () => resolve());
    } catch {
      reject(new Error("高德插件加载失败"));
    }
  });
}

/** 最小类型，避免引入完整 @types/amap */
export type AMapLike = {
  plugin: (names: string[], cb: () => void) => void;
  Map: new (container: HTMLElement | string, opts: Record<string, unknown>) => AMapMapInstance;
  Marker: new (opts: Record<string, unknown>) => AMapOverlay;
  Circle: new (opts: Record<string, unknown>) => AMapOverlay;
  PlaceSearch: new (opts: Record<string, unknown>) => AMapPlaceSearch;
  LngLat: new (lng: number, lat: number) => unknown;
  Pixel: new (x: number, y: number) => unknown;
  Icon: new (opts: { image: string; size: unknown; imageSize: unknown }) => unknown;
};

export type AMapMapInstance = {
  add: (o: AMapOverlay | AMapOverlay[]) => void;
  remove: (o: AMapOverlay) => void;
  destroy: () => void;
  setFitView: (overlays?: unknown, immediately?: boolean, avoid?: number[], maxZoom?: number) => void;
  setZoomAndCenter: (zoom: number, center: unknown) => void;
  on: (ev: string, fn: () => void) => void;
};

export type AMapOverlay = { setMap?: (m: AMapMapInstance | null) => void };

export type AMapPlaceSearch = {
  searchNearBy: (
    keywords: string,
    center: number[] | unknown,
    radius: number,
    cb: (status: string, result: AMapPlaceSearchResult) => void
  ) => void;
};

export type AMapPlaceSearchResult = {
  info?: string;
  poiList?: { pois?: AMapPoi[] };
};

export type AMapPoi = {
  name: string;
  type?: string;
  address?: string;
  location?: { lng: number; lat: number };
};

export function getWindowAMap(): AMapLike {
  return (window as Window & { AMap: AMapLike }).AMap;
}
