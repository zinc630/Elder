import axios from "axios";
import http from "./http";

export function formatAgencyApiError(e: unknown): string {
  if (axios.isAxiosError(e)) {
    const body = e.response?.data as { message?: string } | undefined;
    if (body?.message?.includes("agency_only") || body?.message?.includes("missing_X_Role")) {
      return "请使用服务机构账号登录后查看工单";
    }
    if (body?.message) return body.message;
    if (e.response?.status === 404) return "派单接口未就绪，请重启后端后刷新";
  }
  return (e as { message?: string })?.message ?? "加载失败";
}

export type ServiceWorkerDto = {
  id: string;
  name: string;
  position: string;
  phone: string;
  onlineStatus: "ONLINE" | "OFFLINE";
  serviceType: string;
  createdAt: string;
};

export type DispatchTaskDto = {
  id: string;
  elderId: string;
  serviceType: string;
  status: "NEW" | "ASSIGNED" | "ARRIVING" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED";
  appointmentTime: string;
  notes: string | null;
  assignedWorkerId: string | null;
  createdAt: string;
  updatedAt?: string;
  bookedByRole?: string;
  bookedByUserId?: string;
  bookedByName?: string;
};

export function bookedByLabel(role?: string, name?: string): string {
  if (!role) return "机构录入";
  if (role === "ELDER") return name ? `老人端 · ${name}` : "老人端预约";
  if (role === "CHILD") return name ? `子女端 · ${name}` : "子女端预约";
  if (role === "AGENCY") return "机构录入";
  return role;
}

export type AgencyDashboardStatsDto = {
  totalElders: number;
  agencyCount: number;
  todayOrders: number;
  pendingOrders: number;
  completedTotal: number;
  completionRatePercent: number;
  onlineWorkers: number;
  totalWorkers: number;
};

export async function fetchAgencyDashboardStats(): Promise<AgencyDashboardStatsDto> {
  const resp = await http.get("/v1/agency/dashboard/stats");
  return resp.data.data;
}

export async function listWorkers(keyword?: string, serviceType?: string): Promise<ServiceWorkerDto[]> {
  const resp = await http.get(`/v1/agency/workers`, {
    params: {
      keyword: keyword || undefined,
      serviceType: serviceType || undefined
    }
  });
  return resp.data.data ?? [];
}

export async function addWorker(req: {
  name: string;
  position: string;
  phone: string;
  onlineStatus?: "ONLINE" | "OFFLINE";
}): Promise<string> {
  const resp = await http.post(`/v1/agency/workers`, req);
  return resp.data.data;
}

export async function deleteWorker(workerId: string): Promise<void> {
  await http.delete(`/v1/agency/workers/${workerId}`);
}

export async function updateWorker(
  workerId: string,
  req: {
    name: string;
    position: string;
    phone: string;
    onlineStatus?: "ONLINE" | "OFFLINE";
  }
): Promise<void> {
  await http.patch(`/v1/agency/workers/${workerId}`, req);
}

export async function listTasks(status?: string): Promise<DispatchTaskDto[]> {
  const resp = await http.get(`/v1/agency/dispatch/tasks`, {
    params: {
      status: status || undefined
    }
  });
  return resp.data.data ?? [];
}

export async function createTask(req: {
  elderId: string;
  serviceType: string;
  appointmentTimeMillis: number;
  notes?: string;
}): Promise<string> {
  const resp = await http.post(`/v1/agency/dispatch/tasks`, req);
  return resp.data.data;
}

export async function assignWorker(taskId: string, workerId: string): Promise<DispatchTaskDto> {
  const resp = await http.post(`/v1/agency/dispatch/tasks/${taskId}/assign`, {
    workerId
  });
  return resp.data.data;
}

export async function updateTaskStatus(taskId: string, status: DispatchTaskDto["status"]): Promise<DispatchTaskDto> {
  const resp = await http.patch(`/v1/agency/dispatch/tasks/${taskId}/status`, { status });
  return resp.data.data;
}

export type AgencyAnomalyReportDto = {
  id: string;
  reporterName: string;
  elderId: string;
  elderName: string;
  anomalyType: string;
  description: string;
  reportedAt: string;
  status: "PENDING" | "PROCESSED";
};

export type AgencyClockInRecordDto = {
  id: string;
  workerName: string;
  elderName: string;
  serviceTypeLabel: string;
  clockAt: string;
  address: string;
  statusLabel: string;
};

export async function listAnomalies(): Promise<AgencyAnomalyReportDto[]> {
  const resp = await http.get(`/v1/agency/anomalies`);
  return resp.data.data ?? [];
}

export async function processAnomaly(anomalyId: string): Promise<void> {
  await http.post(`/v1/agency/anomalies/${anomalyId}/process`);
}

export async function listClockIns(): Promise<AgencyClockInRecordDto[]> {
  const resp = await http.get(`/v1/agency/clock-ins`);
  return resp.data.data ?? [];
}

export type AgencyAlertDto = {
  id: string;
  elderName: string;
  type: string;
  triggeredAt: string;
  status: "PENDING" | "PROCESSING" | "RESOLVED";
  processLog: string;
  source: "ALARM" | "ANOMALY";
};

export async function listAlerts(): Promise<AgencyAlertDto[]> {
  const resp = await http.get(`/v1/agency/alerts`);
  return resp.data.data ?? [];
}

export async function processAgencyAlert(alertId: string, source: "ALARM" | "ANOMALY"): Promise<void> {
  await http.post(`/v1/agency/alerts/${alertId}/process`, null, { params: { source } });
}

export type AgencyScheduleDto = {
  id: string;
  day: string;
  hour: number;
  workerName: string;
  type: "morning" | "afternoon" | "night";
};

export async function listSchedules(): Promise<AgencyScheduleDto[]> {
  const resp = await http.get(`/v1/agency/schedules`);
  return resp.data.data ?? [];
}

export async function createSchedule(req: {
  day: string;
  hour: number;
  workerName: string;
  type: string;
}): Promise<string> {
  const resp = await http.post(`/v1/agency/schedules`, req);
  return resp.data.data;
}

export async function deleteSchedule(scheduleId: string): Promise<void> {
  await http.delete(`/v1/agency/schedules/${scheduleId}`);
}

export type AgencyEvaluationDto = {
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
  tags?: string[];
  comment: string;
  isAnonymous?: boolean;
  serviceDurationMinutes?: number;
  taskId?: string;
  taskIdLabel?: string;
  images?: string[];
  createdAt: string;
};

export async function listEvaluations(): Promise<AgencyEvaluationDto[]> {
  const resp = await http.get(`/v1/agency/evaluations`);
  return resp.data.data ?? [];
}

export type AgencyActivityDto = {
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

export async function listActivities(): Promise<AgencyActivityDto[]> {
  const resp = await http.get(`/v1/agency/activities`);
  return resp.data.data ?? [];
}

export async function createActivity(req: {
  title: string;
  startTime: string;
  endTime: string;
  location: string;
  maxParticipants: number;
  description?: string;
  tag?: string;
  icon?: string;
}): Promise<string> {
  const resp = await http.post(`/v1/agency/activities`, req);
  return resp.data.data;
}

export async function activityCheckin(activityId: string): Promise<void> {
  await http.post(`/v1/agency/activities/${activityId}/checkin`);
}

export type AgencyFinanceDto = {
  id: string;
  elderName: string;
  serviceType: string;
  amount: number;
  status: "paid" | "pending";
  paidAt: string | null;
};

export async function listFinance(): Promise<AgencyFinanceDto[]> {
  const resp = await http.get(`/v1/agency/finance`);
  return resp.data.data ?? [];
}

export async function createFinance(req: {
  elderName: string;
  serviceType: string;
  amount: number;
  status: string;
}): Promise<string> {
  const resp = await http.post(`/v1/agency/finance`, req);
  return resp.data.data;
}

export async function markFinancePaid(financeId: string): Promise<void> {
  await http.post(`/v1/agency/finance/${financeId}/pay`);
}

export type AgencyNotificationDto = {
  id: string;
  title: string;
  content: string;
  status: "published" | "draft" | "expired";
  createdAt: string;
  author: string;
  views: number;
};

export async function listNotifications(): Promise<AgencyNotificationDto[]> {
  const resp = await http.get(`/v1/agency/notifications`);
  return resp.data.data ?? [];
}

export async function createNotification(req: {
  title: string;
  content: string;
  status: string;
  author?: string;
}): Promise<string> {
  const resp = await http.post(`/v1/agency/notifications`, req);
  return resp.data.data;
}

export async function updateNotificationStatus(notificationId: string, status: string): Promise<void> {
  await http.patch(`/v1/agency/notifications/${notificationId}`, { status });
}

export async function updateNotification(
  notificationId: string,
  req: { title?: string; content?: string; status?: string }
): Promise<void> {
  await http.patch(`/v1/agency/notifications/${notificationId}`, req);
}

export async function patchDeviceElder(deviceId: string, elderName: string | null): Promise<void> {
  await http.patch(`/v1/agency/devices/${deviceId}`, { elderName });
}

export async function deleteNotification(notificationId: string): Promise<void> {
  await http.delete(`/v1/agency/notifications/${notificationId}`);
}

export type AgencyDeviceDto = {
  id: string;
  name: string;
  type: string;
  elderName: string | null;
  battery: number;
  status: "online" | "offline" | "low";
  lastOnline: string | null;
};

export async function listDevices(): Promise<AgencyDeviceDto[]> {
  const resp = await http.get(`/v1/agency/devices`);
  return resp.data.data ?? [];
}

export async function createDevice(req: {
  name: string;
  type: string;
  elderName?: string;
  battery?: number;
  status?: string;
}): Promise<string> {
  const resp = await http.post(`/v1/agency/devices`, req);
  return resp.data.data;
}

export async function deleteDevice(deviceId: string): Promise<void> {
  await http.delete(`/v1/agency/devices/${deviceId}`);
}

export type AgencyHealthRecordDto = {
  elderId: string;
  name: string;
  age: number | null;
  gender: string;
  healthStatus: string;
  heartRate: number | null;
  bloodPressure: string;
  bloodOxygen: number | null;
  lastUpdated: string | null;
};

export async function listHealthRecords(): Promise<AgencyHealthRecordDto[]> {
  const resp = await http.get(`/v1/agency/health-records`);
  return resp.data.data ?? [];
}

