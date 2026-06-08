import axios from "axios";

const instance = axios.create({
  baseURL: "/api",
  timeout: 15000
});

instance.interceptors.request.use((config) => {
  const url = String(config.url ?? "");
  let role = localStorage.getItem("elder.role") ?? "";
  // 机构端接口必须使用 AGENCY/ADMIN 角色，避免老人端登录态导致 403、工单列表为空
  if (url.includes("/v1/agency/")) {
    role = role === "ADMIN" ? "ADMIN" : "AGENCY";
  } else if (url.includes("/v1/admin/")) {
    role = "ADMIN";
  }
  if (role) config.headers = { ...config.headers, "X-Role": role };
  if (config.data instanceof FormData) {
    const headers = config.headers;
    if (headers && typeof (headers as { delete?: (k: string) => void }).delete === "function") {
      (headers as { delete: (k: string) => void }).delete("Content-Type");
    } else if (headers && typeof headers === "object") {
      delete (headers as Record<string, unknown>)["Content-Type"];
    }
  }
  return config;
});

export default instance;

