import axios from "axios";
import http from "./http";
import { getPortalActorId } from "../utils/portalActor";

type ApiWrap<T> = { success: boolean; message?: string; data: T };

export type PortalActivity = {
  id: string;
  icon: string;
  tag: string;
  title: string;
  timeLabel: string;
  location: string;
  description: string;
  capacity: number;
  enrolledCount: number;
  remaining: number;
  open: boolean;
  enrolled: boolean;
  imageUrl?: string | null;
};

export type PortalLifeService = {
  id: string;
  icon: string;
  title: string;
  description: string;
  features: string[];
  bgGradient: string;
  priceHint: string;
  slaHours: number;
  booked: boolean;
};

export type PortalCourse = {
  id: string;
  icon: string;
  category: string;
  title: string;
  description: string;
  duration: string;
  viewsLabel: string;
  rating: number;
  lessonCount: number;
  progressPercent: number;
  enrolled: boolean;
  imageUrl?: string | null;
};

export type PortalLesson = {
  id: string;
  title: string;
  content: string;
  durationMinutes: number;
  videoUrl?: string;
  completed: boolean;
};

export type PortalCourseDetail = PortalCourse & {
  lessons: PortalLesson[];
  completedLessonIds: string[];
};

export type PortalNews = {
  id: string;
  icon: string;
  title: string;
  summary: string;
  publishDate: string;
  source: string;
  viewsLabel: string;
  imageUrl?: string | null;
};

export type PortalNewsDetail = PortalNews & { body: string; imageUrl?: string | null };

export type PortalEnrollPayload = {
  contactName: string;
  contactPhone: string;
  note?: string;
  preferredTime?: string;
};

export type PortalActionResult = {
  success: boolean;
  message: string;
  registrationId: string | null;
};

function portalHeaders() {
  return { "X-Portal-Actor": getPortalActorId() };
}

function unwrapApiError(e: unknown): Error {
  if (axios.isAxiosError(e)) {
    const data = e.response?.data as { message?: string } | undefined;
    const status = e.response?.status;
    if (status === 404) return new Error("接口不存在，请重启后端服务后再试");
    if (data?.message) return new Error(data.message);
    if (e.code === "ERR_NETWORK") return new Error("无法连接后端，请确认已在 8081 端口启动服务");
    return new Error(e.message);
  }
  return e instanceof Error ? e : new Error(String(e));
}

export async function fetchActivities(): Promise<PortalActivity[]> {
  try {
    const resp = await http.get<ApiWrap<PortalActivity[]>>("/v1/public/portal/activities", {
      headers: portalHeaders()
    });
    return resp.data.data;
  } catch (e) {
    throw unwrapApiError(e);
  }
}

export async function fetchActivity(id: string): Promise<PortalActivity> {
  try {
    const resp = await http.get<ApiWrap<PortalActivity>>(`/v1/public/portal/activities/${id}`, {
      headers: portalHeaders()
    });
    return resp.data.data;
  } catch (e) {
    throw unwrapApiError(e);
  }
}

export async function enrollActivity(id: string, payload: PortalEnrollPayload): Promise<PortalActionResult> {
  try {
    const resp = await http.post<ApiWrap<PortalActionResult>>(
      `/v1/public/portal/activities/${id}/enroll`,
      payload,
      { headers: portalHeaders() }
    );
    return resp.data.data;
  } catch (e) {
    throw unwrapApiError(e);
  }
}

export async function fetchLifeServices(): Promise<PortalLifeService[]> {
  const resp = await http.get<ApiWrap<PortalLifeService[]>>("/v1/public/portal/life-services", {
    headers: portalHeaders()
  });
  return resp.data.data;
}

export async function bookLifeService(id: string, payload: PortalEnrollPayload): Promise<PortalActionResult> {
  const resp = await http.post<ApiWrap<PortalActionResult>>(
    `/v1/public/portal/life-services/${id}/book`,
    payload,
    { headers: portalHeaders() }
  );
  return resp.data.data;
}

export async function fetchCourses(): Promise<PortalCourse[]> {
  const resp = await http.get<ApiWrap<PortalCourse[]>>("/v1/public/portal/courses", {
    headers: portalHeaders()
  });
  return resp.data.data;
}

export async function fetchCourseDetail(id: string): Promise<PortalCourseDetail> {
  try {
    const resp = await http.get<ApiWrap<PortalCourseDetail>>(`/v1/public/portal/courses/${id}`, {
      headers: portalHeaders()
    });
    return resp.data.data;
  } catch (e) {
    throw unwrapApiError(e);
  }
}

export async function enrollCourse(id: string, payload: PortalEnrollPayload): Promise<PortalActionResult> {
  const resp = await http.post<ApiWrap<PortalActionResult>>(
    `/v1/public/portal/courses/${id}/enroll`,
    payload,
    { headers: portalHeaders() }
  );
  return resp.data.data;
}

export async function completeCourseLesson(courseId: string, lessonId: string): Promise<PortalActionResult> {
  const resp = await http.post<ApiWrap<PortalActionResult>>(
    `/v1/public/portal/courses/${courseId}/lessons/complete`,
    { lessonId },
    { headers: portalHeaders() }
  );
  return resp.data.data;
}

export async function fetchNews(): Promise<PortalNews[]> {
  const resp = await http.get<ApiWrap<PortalNews[]>>("/v1/public/portal/news");
  const data = resp.data?.data;
  return Array.isArray(data) ? data : [];
}

export async function fetchNewsDetail(id: string): Promise<PortalNewsDetail> {
  const resp = await http.get<ApiWrap<PortalNewsDetail>>(`/v1/public/portal/news/${id}`);
  return resp.data.data;
}
