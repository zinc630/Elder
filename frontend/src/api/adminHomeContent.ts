import axios from "axios";
import http from "./http";

type ApiWrap<T> = { success: boolean; message?: string; data: T };

function unwrapApiError(e: unknown): Error {
  if (axios.isAxiosError(e)) {
    const data = e.response?.data as ApiWrap<unknown> | undefined;
    const status = e.response?.status;
    if (status === 403) return new Error("需要管理员权限，请使用管理员账号登录");
    if (status === 404) return new Error("接口不存在，请重启后端服务");
    if (data?.message) {
      const m = data.message;
      if (m.includes("multipart")) return new Error("上传格式错误，请重试或刷新页面");
      if (m.startsWith("internal_error: ")) return new Error(m.slice("internal_error: ".length));
      if (m.startsWith("bad_request: ")) return new Error(m.slice("bad_request: ".length));
      return new Error(m);
    }
    if (e.code === "ERR_NETWORK") return new Error("无法连接后端，请确认 8081 端口已启动");
    return new Error(e.message);
  }
  return e instanceof Error ? e : new Error(String(e));
}

function pickData<T>(resp: { data: ApiWrap<T> }): T {
  const wrap = resp.data;
  if (!wrap.success) throw new Error(wrap.message || "请求失败");
  return wrap.data;
}

export type PortalMediaUpload = { url: string; kind: string; originalFilename: string };

export type ActivityAdmin = {
  id: string;
  icon: string;
  tag: string;
  title: string;
  timeLabel: string;
  location: string;
  description: string;
  capacity: number;
  enrolledCount: number;
  open: boolean;
  imageUrl: string | null;
};

export type LessonAdmin = {
  id: string;
  title: string;
  content: string;
  durationMinutes: number;
  videoUrl: string | null;
};

export type CourseAdmin = {
  id: string;
  icon: string;
  category: string;
  title: string;
  description: string;
  duration: string;
  viewCount: number;
  rating: number;
  lessons: LessonAdmin[];
  imageUrl: string | null;
};

export type NewsAdmin = {
  id: string;
  icon: string;
  title: string;
  summary: string;
  publishDate: string;
  source: string;
  body: string;
  viewCount: number;
  imageUrl: string | null;
};

export async function uploadPortalMedia(file: File, kind: "image" | "video"): Promise<PortalMediaUpload> {
  const fd = new FormData();
  fd.append("file", file);
  fd.append("kind", kind);
  try {
    // 勿手动设置 Content-Type：须由浏览器附带 multipart boundary，否则后端无法解析
    const resp = await http.post<ApiWrap<PortalMediaUpload>>("/v1/admin/home-content/media/upload", fd, {
      timeout: 600_000
    });
    return pickData(resp);
  } catch (e) {
    throw unwrapApiError(e);
  }
}

export async function seedDemoContent(): Promise<string> {
  const resp = await http.post<ApiWrap<string>>("/v1/admin/home-content/seed-demo");
  return pickData(resp);
}

export async function listAdminActivities(): Promise<ActivityAdmin[]> {
  try {
    const resp = await http.get<ApiWrap<ActivityAdmin[]>>("/v1/admin/home-content/activities");
    return pickData(resp) ?? [];
  } catch (e) {
    throw unwrapApiError(e);
  }
}

export async function saveAdminActivity(
  payload: Omit<ActivityAdmin, "enrolledCount"> & { id?: string },
  isNew: boolean
): Promise<ActivityAdmin> {
  const body = {
    id: payload.id,
    icon: payload.icon,
    tag: payload.tag,
    title: payload.title,
    timeLabel: payload.timeLabel,
    location: payload.location,
    description: payload.description,
    capacity: payload.capacity,
    open: payload.open,
    imageUrl: payload.imageUrl ?? ""
  };
  try {
    if (isNew) {
      const resp = await http.post<ApiWrap<ActivityAdmin>>("/v1/admin/home-content/activities", body);
      return pickData(resp);
    }
    const resp = await http.put<ApiWrap<ActivityAdmin>>(`/v1/admin/home-content/activities/${payload.id}`, body, {
      timeout: 60_000
    });
    return pickData(resp);
  } catch (e) {
    throw unwrapApiError(e);
  }
}

export async function deleteAdminActivity(id: string): Promise<void> {
  await http.delete(`/v1/admin/home-content/activities/${id}`);
}

export async function listAdminCourses(): Promise<CourseAdmin[]> {
  try {
    const resp = await http.get<ApiWrap<CourseAdmin[]>>("/v1/admin/home-content/courses");
    return pickData(resp) ?? [];
  } catch (e) {
    throw unwrapApiError(e);
  }
}

export async function saveAdminCourse(payload: CourseAdmin, isNew: boolean): Promise<CourseAdmin> {
  const body = {
    id: payload.id,
    icon: payload.icon,
    category: payload.category,
    title: payload.title,
    description: payload.description,
    duration: payload.duration,
    rating: payload.rating,
    lessons: payload.lessons.map((l) => ({
      id: l.id,
      title: l.title,
      content: l.content,
      durationMinutes: l.durationMinutes,
      videoUrl: l.videoUrl ?? ""
    })),
    imageUrl: payload.imageUrl ?? ""
  };
  try {
    if (isNew) {
      const resp = await http.post<ApiWrap<CourseAdmin>>("/v1/admin/home-content/courses", body);
      return pickData(resp);
    }
    const resp = await http.put<ApiWrap<CourseAdmin>>(`/v1/admin/home-content/courses/${payload.id}`, body, {
      timeout: 60_000
    });
    return pickData(resp);
  } catch (e) {
    throw unwrapApiError(e);
  }
}

export async function deleteAdminCourse(id: string): Promise<void> {
  await http.delete(`/v1/admin/home-content/courses/${id}`);
}

export async function listAdminNews(): Promise<NewsAdmin[]> {
  try {
    const resp = await http.get<ApiWrap<NewsAdmin[]>>("/v1/admin/home-content/news");
    return pickData(resp) ?? [];
  } catch (e) {
    throw unwrapApiError(e);
  }
}

export async function saveAdminNews(payload: NewsAdmin, isNew: boolean): Promise<NewsAdmin> {
  const body = {
    id: payload.id,
    icon: payload.icon,
    title: payload.title,
    summary: payload.summary,
    publishDate: payload.publishDate,
    source: payload.source,
    body: payload.body,
    imageUrl: payload.imageUrl ?? ""
  };
  try {
    if (isNew) {
      const resp = await http.post<ApiWrap<NewsAdmin>>("/v1/admin/home-content/news", body);
      return pickData(resp);
    }
    const resp = await http.put<ApiWrap<NewsAdmin>>(`/v1/admin/home-content/news/${payload.id}`, body, {
      timeout: 60_000
    });
    return pickData(resp);
  } catch (e) {
    throw unwrapApiError(e);
  }
}

export async function deleteAdminNews(id: string): Promise<void> {
  await http.delete(`/v1/admin/home-content/news/${id}`);
}
