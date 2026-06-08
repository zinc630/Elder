/** 将后端返回的 /api/... 媒体路径转为可访问 URL（走 Vite 代理） */
export function resolveMediaUrl(url: string | null | undefined): string {
  if (!url) return "";
  if (url.startsWith("http://") || url.startsWith("https://")) return url;
  if (url.startsWith("/api/")) return url;
  if (url.startsWith("/")) return `/api${url}`;
  return url;
}
