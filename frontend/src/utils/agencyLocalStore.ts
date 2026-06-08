/** 机构端扩展模块本地数据（活动/收费/通知/设备/排班/评价） */

export type AgencyActivity = {
  id: string;
  title: string;
  startTime: string;
  endTime: string;
  location: string;
  maxParticipants: number;
  registered: number;
  status: "upcoming" | "ongoing" | "ended";
  description: string;
};

export type AgencyFinance = {
  id: string;
  elderName: string;
  serviceType: string;
  amount: number;
  status: "paid" | "pending";
  paidAt: string | null;
};

export type AgencyNotification = {
  id: string;
  title: string;
  content: string;
  status: "published" | "draft" | "expired";
  createdAt: string;
  author: string;
  views: number;
};

export type AgencyDevice = {
  id: string;
  name: string;
  type: string;
  elderName: string | null;
  battery: number;
  status: "online" | "offline" | "low";
  lastOnline: string;
};

export type AgencySchedule = {
  id: string;
  day: string;
  hour: number;
  workerName: string;
  type: "morning" | "afternoon" | "night";
};

export type AgencyEvaluation = {
  id: string;
  elderName: string;
  workerName: string;
  serviceType: string;
  rating: number;
  attitudeRating?: number;
  skillRating?: number;
  responseRating?: number;
  punctualityRating?: number;
  communicationRating?: number;
  comment: string;
  tags?: string[];
  isAnonymous?: boolean;
  serviceDurationMinutes?: number;
  taskId?: string;
  createdAt: string;
};

function key(name: string) {
  return `agency.local.${name}`;
}

function load<T>(name: string, fallback: T): T {
  try {
    const raw = localStorage.getItem(key(name));
    if (!raw) return fallback;
    return JSON.parse(raw) as T;
  } catch {
    return fallback;
  }
}

function save<T>(name: string, data: T) {
  localStorage.setItem(key(name), JSON.stringify(data));
}

const defaultActivities: AgencyActivity[] = [
  {
    id: "A001",
    title: "重阳节敬老活动",
    startTime: "2026-05-20T09:00:00",
    endTime: "2026-05-20T17:00:00",
    location: "养老院活动中心",
    maxParticipants: 50,
    registered: 32,
    status: "upcoming",
    description: "组织老人参观公园、制作重阳糕"
  },
  {
    id: "A002",
    title: "书法兴趣班",
    startTime: "2026-05-18T14:00:00",
    endTime: "2026-05-18T16:00:00",
    location: "书画室",
    maxParticipants: 20,
    registered: 18,
    status: "ongoing",
    description: "邀请书法老师指导老人练习书法"
  }
];

const defaultFinance: AgencyFinance[] = [
  { id: "F001", elderName: "张大爷", serviceType: "NURSING", amount: 25, status: "paid", paidAt: "2026-05-18T12:00:00" },
  { id: "F002", elderName: "李奶奶", serviceType: "HOUSEKEEPING", amount: 120, status: "pending", paidAt: null }
];

const defaultNotifications: AgencyNotification[] = [
  {
    id: "N001",
    title: "系统升级通知",
    content: "系统将于5月20日凌晨2:00-5:00进行升级维护。",
    status: "published",
    createdAt: "2026-05-18T10:00:00",
    author: "管理员",
    views: 156
  }
];

const defaultDevices: AgencyDevice[] = [
  {
    id: "D001",
    name: "智能手环 - 客厅",
    type: "智能手环",
    elderName: "张大爷",
    battery: 85,
    status: "online",
    lastOnline: new Date().toISOString()
  },
  {
    id: "D002",
    name: "跌倒检测器",
    type: "跌倒检测器",
    elderName: null,
    battery: 23,
    status: "low",
    lastOnline: new Date().toISOString()
  }
];

const defaultSchedules: AgencySchedule[] = [
  { id: "S001", day: "周一", hour: 9, workerName: "王阿姨", type: "morning" },
  { id: "S002", day: "周二", hour: 14, workerName: "李师傅", type: "afternoon" }
];

const defaultEvaluations: AgencyEvaluation[] = [
  {
    id: "E001",
    elderName: "张大爷",
    workerName: "王阿姨",
    serviceType: "NURSING",
    rating: 5,
    attitudeRating: 5,
    skillRating: 5,
    responseRating: 5,
    punctualityRating: 5,
    communicationRating: 5,
    comment: "送餐准时，服务非常周到，老人很满意。",
    tags: ["细心负责", "热情周到"],
    serviceDurationMinutes: 45,
    taskId: "TASK-DEMO-001",
    createdAt: "2026-05-18T10:30:00"
  }
];

export function loadActivities() {
  const data = load("activities", defaultActivities);
  save("activities", data);
  return data;
}
export function saveActivities(list: AgencyActivity[]) {
  save("activities", list);
}

export function loadFinance() {
  const data = load("finance", defaultFinance);
  save("finance", data);
  return data;
}
export function saveFinance(list: AgencyFinance[]) {
  save("finance", list);
}

export function loadNotifications() {
  const data = load("notifications", defaultNotifications);
  save("notifications", data);
  return data;
}
export function saveNotifications(list: AgencyNotification[]) {
  save("notifications", list);
}

export function loadDevices() {
  const data = load("devices", defaultDevices);
  save("devices", data);
  return data;
}
export function saveDevices(list: AgencyDevice[]) {
  save("devices", list);
}

export function loadSchedules() {
  const data = load("schedules", defaultSchedules);
  save("schedules", data);
  return data;
}
export function saveSchedules(list: AgencySchedule[]) {
  save("schedules", list);
}

export function loadEvaluations() {
  const data = load("evaluations", defaultEvaluations);
  save("evaluations", data);
  return data;
}
export function saveEvaluations(list: AgencyEvaluation[]) {
  save("evaluations", list);
}
