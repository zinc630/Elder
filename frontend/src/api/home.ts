import http from "./http";

export type PublicHomeSummary = {
  roleCount: number;
  moduleCount: number;
  elderCount: number;
  todayAlarms: number;
  monthlyTasks: number;
  deviceCount: number;
  deepSeekConfigured: boolean;
  notice: string;
};

type ApiWrap<T> = { success: boolean; data: T };

export async function fetchHomeSummary(): Promise<PublicHomeSummary> {
  const resp = await http.get<ApiWrap<PublicHomeSummary>>("/v1/public/home/summary");
  return resp.data.data;
}
