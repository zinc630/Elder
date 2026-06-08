import axios from "axios";
import http from "./http";

export function formatAdminApiError(e: unknown): string {
  if (axios.isAxiosError(e)) {
    const body = e.response?.data as { message?: string } | undefined;
    if (body?.message?.includes("admin_only")) {
      return "请使用管理员账号登录后查看";
    }
    if (body?.message) return body.message;
  }
  return (e as { message?: string })?.message ?? "加载失败";
}

export type AlarmEventAdminDto = {
  id: string;
  elderId: string;
  type: string;
  status: string;
  triggeredAt: string;
  riskScore?: number | null;
  abnormalPointCount?: number | null;
  abnormalPointThreshold?: number | null;
  confirmationSource?: string | null;
};

export type CallAttemptDto = {
  id: string;
  alarmEventId: string;
  stage: string;
  attemptNo: number;
  createdAt: string;
  result: string | null;
  resultDetail?: string | null;
};

export async function listPendingAlarms(): Promise<AlarmEventAdminDto[]> {
  const resp = await http.get(`/v1/admin/alarms/pending`);
  return resp.data.data ?? [];
}

export async function listCallAttempts(alarmEventId: string): Promise<CallAttemptDto[]> {
  const resp = await http.get(`/v1/admin/alarms/${alarmEventId}/call-attempts`);
  return resp.data.data ?? [];
}

export async function setCallAttemptResult(callAttemptId: string, result: string, detail?: string): Promise<void> {
  await http.post(`/v1/admin/call-attempts/${callAttemptId}/result`, {
    result,
    detail
  });
}

export type AdminDashboardStats = {
  totalElders: number;
  todayAlerts: number;
  monthlyServiceOrders: number;
  boundDevices: number;
  pendingOrders: number;
  pendingAlarms: number;
  completedOrdersMonth: number;
  totalAgencies: number;
  totalChildren: number;
};

export type AdminNamedCount = { label: string; count: number };

export type AdminAlarmTypeTrendPoint = {
  label: string;
  fallCount: number;
  sosCount: number;
  vitalsCount: number;
};

export type AdminAlertTrendPoint = { label: string; count: number };
export type AdminHighRiskRow = {
  alarmId: string;
  elderName: string;
  abnormalityType: string;
  statusLabel: string;
};
export type AdminElderVitalsRow = {
  elderId: string;
  name: string;
  age: number | null;
  heartRateBpm: number;
  bloodPressureDisplay: string;
  bloodOxygenPercent: number;
  latestWarning: string;
  warningLevel: string;
};
export type AdminCriticalAlert = {
  id: string;
  title: string;
  occurredAt: string;
  handled: boolean;
};

export async function getDashboardStats(): Promise<AdminDashboardStats> {
  const resp = await http.get(`/v1/admin/dashboard/stats`);
  return resp.data.data;
}

export async function getAlertTrend(): Promise<AdminAlertTrendPoint[]> {
  const resp = await http.get(`/v1/admin/dashboard/alert-trend`);
  return resp.data.data ?? [];
}

export async function getHighRiskAlarms(): Promise<AdminHighRiskRow[]> {
  const resp = await http.get(`/v1/admin/dashboard/high-risk`);
  return resp.data.data ?? [];
}

export async function getServiceTrend(): Promise<AdminAlertTrendPoint[]> {
  const resp = await http.get(`/v1/admin/dashboard/service-trend`);
  return resp.data.data ?? [];
}

export async function getAlarmTypeStats(): Promise<AdminNamedCount[]> {
  const resp = await http.get(`/v1/admin/dashboard/alarm-type-stats`);
  return resp.data.data ?? [];
}

export async function getAlarmTypeTrend(): Promise<AdminAlarmTypeTrendPoint[]> {
  const resp = await http.get(`/v1/admin/dashboard/alarm-type-trend`);
  return resp.data.data ?? [];
}

export async function getServiceTypeStats(): Promise<AdminNamedCount[]> {
  const resp = await http.get(`/v1/admin/dashboard/service-type-stats`);
  return resp.data.data ?? [];
}

export async function getHealthDistribution(): Promise<AdminNamedCount[]> {
  const resp = await http.get(`/v1/admin/dashboard/health-distribution`);
  return resp.data.data ?? [];
}

export async function getMonitoringElders(): Promise<AdminElderVitalsRow[]> {
  const resp = await http.get(`/v1/admin/monitoring/elders`);
  return resp.data.data ?? [];
}

export async function getCriticalAlerts(): Promise<AdminCriticalAlert[]> {
  const resp = await http.get(`/v1/admin/monitoring/critical-alerts`);
  return resp.data.data ?? [];
}

