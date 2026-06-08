/** 家庭监控设备（按老人维度本地配置，可后续对接 IoT / 国标 GB28181） */

export type MonitorDeviceStatus = "online" | "offline";

export type HomeMonitorDevice = {
  id: string;
  name: string;
  location: string;
  status: MonitorDeviceStatus;
  /** 演示用预览图（在线时轮播刷新模拟实况） */
  previewSeed: string;
  deviceSn: string;
};

const DEFAULT_DEVICES: Omit<HomeMonitorDevice, "id">[] = [
  { name: "客厅摄像头", location: "客厅", status: "online", previewSeed: "living-room", deviceSn: "CAM-LR-001" },
  { name: "卧室摄像头", location: "卧室", status: "online", previewSeed: "bedroom", deviceSn: "CAM-BR-002" },
  { name: "门口摄像头", location: "玄关", status: "online", previewSeed: "doorway", deviceSn: "CAM-DW-003" },
  { name: "厨房摄像头", location: "厨房", status: "offline", previewSeed: "kitchen", deviceSn: "CAM-KT-004" }
];

function storageKey(elderId: string) {
  return `child.monitorDevices.${elderId}`;
}

export function loadMonitorDevices(elderId: string): HomeMonitorDevice[] {
  if (!elderId) return [];
  try {
    const raw = localStorage.getItem(storageKey(elderId));
    if (raw) {
      const parsed = JSON.parse(raw) as HomeMonitorDevice[];
      if (Array.isArray(parsed) && parsed.length > 0) return parsed;
    }
  } catch {
    /* ignore */
  }
  const seeded = DEFAULT_DEVICES.map((d, i) => ({
    ...d,
    id: `mon_${i + 1}`
  }));
  saveMonitorDevices(elderId, seeded);
  return seeded;
}

export function saveMonitorDevices(elderId: string, devices: HomeMonitorDevice[]) {
  localStorage.setItem(storageKey(elderId), JSON.stringify(devices));
}

export function monitorPreviewUrl(device: HomeMonitorDevice, tick = 0) {
  return `https://picsum.photos/seed/${device.previewSeed}-${tick}/960/540`;
}
