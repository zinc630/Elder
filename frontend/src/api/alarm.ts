import http from "./http";

export type AlarmDto = {
  id: string;
  elderId: string;
  type: string;
  status: string;
  triggeredAt: string;
  riskScore: number | null;
  abnormalPointCount: number | null;
  abnormalPointThreshold: number | null;
  confirmationSource: string | null;
};

export async function listAlarms(elderId: string, limit = 20): Promise<AlarmDto[]> {
  const resp = await http.get(`/v1/elders/${elderId}/alarms`, { params: { limit } });
  return resp.data.data ?? [];
}

export async function getAlarm(alarmEventId: string): Promise<AlarmDto> {
  const resp = await http.get(`/v1/alarms/${alarmEventId}`);
  return resp.data.data;
}

export async function confirmAlarm(alarmEventId: string, confirmationSource: string): Promise<void> {
  await http.post(`/v1/alarms/${alarmEventId}/confirm`, { confirmationSource });
}

export async function triggerSosAlarm(elderId: string): Promise<AlarmDto> {
  const resp = await http.post(`/v1/elders/${elderId}/alarms/sos`);
  return resp.data.data;
}

