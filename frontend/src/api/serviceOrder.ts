import axios from "axios";
import http from "./http";

export function formatServiceOrderError(e: unknown): string {
  if (axios.isAxiosError(e)) {
    const body = e.response?.data as { message?: string } | undefined;
    if (body?.message) return body.message;
    if (e.response?.status === 404) {
      return "预约接口未就绪，请重启后端服务后重试";
    }
  }
  return (e as { message?: string })?.message ?? "请稍后重试";
}

export type ServiceOrderDto = {
  id: string;
  elderId: string;
  serviceType: string;
  serviceTypeLabel: string;
  status: string;
  statusLabel: string;
  appointmentTime: string;
  notes: string;
  bookedByRole: string;
  bookedByName: string;
  createdAt: string;
};

export type ServiceOrderCreateRequest = {
  serviceType: string;
  appointmentTimeMillis: number;
  notes?: string;
  bookedByRole: "ELDER" | "CHILD";
  bookedByUserId?: string;
  bookedByName?: string;
};

export async function createServiceOrder(
  elderId: string,
  req: ServiceOrderCreateRequest
): Promise<ServiceOrderDto> {
  const resp = await http.post(`/v1/elders/${elderId}/service-orders`, req);
  return resp.data.data;
}

export async function listServiceOrders(elderId: string): Promise<ServiceOrderDto[]> {
  const resp = await http.get(`/v1/elders/${elderId}/service-orders`);
  return resp.data.data ?? [];
}

export function serviceOrderStatusClass(status: string): string {
  switch (status) {
    case "COMPLETED":
      return "completed";
    case "IN_PROGRESS":
    case "ASSIGNED":
    case "ARRIVING":
      return "processing";
    default:
      return "pending";
  }
}
