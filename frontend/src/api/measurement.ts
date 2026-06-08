import http from "./http";

export type MeasurementIngestPayload = {
  deviceId: string;
  elderId: string;
  timestampMillis: number;
  heartRate?: number | null;
  systolic?: number | null;
  diastolic?: number | null;
};

export type MeasurementIngestResult = {
  deviceId: string;
  elderId: string;
  alarmCreated: boolean;
  alarmEventId: string | null;
};

export async function postMeasurement(payload: MeasurementIngestPayload): Promise<MeasurementIngestResult> {
  const resp = await http.post<{ success: boolean; data: MeasurementIngestResult }>("/v1/measurements", payload);
  return resp.data.data;
}
