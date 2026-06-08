import http from "./http";
import { resolveMediaUrl } from "../utils/mediaUrl";

export type FamilyChatMessageDto = {
  id: string;
  elderId: string;
  senderRole: string;
  senderUserId: string | null;
  senderName: string;
  content: string;
  createdAt: string;
};

export type FamilyAlbumPhotoDto = {
  id: string;
  elderId: string;
  uploadedByRole: string;
  uploadedByUserId: string | null;
  fileName: string;
  url: string;
  createdAt: string;
};

export type FamilyVideoCallDto = {
  callId: string;
  elderId: string;
  childId: string | null;
  childName: string;
  initiatorRole: string;
  elderDisplayName: string | null;
  status: string;
  offerSdp: string | null;
  answerSdp: string | null;
  updatedAt: string;
};

export const FAMILY_CHAT_POLL_MS = 3000;

export async function listFamilyMessages(elderId: string, limit = 100): Promise<FamilyChatMessageDto[]> {
  const resp = await http.get(`/v1/elders/${elderId}/family/messages`, { params: { limit } });
  return resp.data.data ?? [];
}

export async function sendFamilyMessage(
  elderId: string,
  body: {
    senderRole: "ELDER" | "CHILD";
    senderUserId?: string;
    senderName: string;
    content: string;
  }
): Promise<FamilyChatMessageDto> {
  const resp = await http.post(`/v1/elders/${elderId}/family/messages`, body);
  return resp.data.data;
}

export async function listFamilyAlbum(elderId: string): Promise<FamilyAlbumPhotoDto[]> {
  const resp = await http.get(`/v1/elders/${elderId}/family/album`);
  return resp.data.data ?? [];
}

export async function uploadFamilyAlbum(
  elderId: string,
  file: File,
  uploadedByRole: "ELDER" | "CHILD",
  uploadedByUserId?: string
): Promise<FamilyAlbumPhotoDto> {
  const form = new FormData();
  form.append("file", file);
  form.append("uploadedByRole", uploadedByRole);
  if (uploadedByUserId) form.append("uploadedByUserId", uploadedByUserId);
  const resp = await http.post(`/v1/elders/${elderId}/family/album`, form);
  return resp.data.data;
}

export async function deleteFamilyAlbum(elderId: string, photoId: string): Promise<void> {
  await http.delete(`/v1/elders/${elderId}/family/album/${photoId}`);
}

export async function fetchFamilyVideoCall(elderId: string): Promise<FamilyVideoCallDto | null> {
  const resp = await http.get(`/v1/elders/${elderId}/family/video-call`);
  return resp.data.data ?? null;
}

export async function startFamilyVideoCall(
  elderId: string,
  body: {
    childUserId?: string;
    callerDisplayName: string;
    initiatorRole?: "ELDER" | "CHILD";
    elderDisplayName?: string;
  }
): Promise<FamilyVideoCallDto> {
  const resp = await http.post(`/v1/elders/${elderId}/family/video-call`, body);
  return resp.data.data;
}

export async function updateFamilyVideoSignal(
  elderId: string,
  callId: string,
  body: { offerSdp?: string; answerSdp?: string }
): Promise<FamilyVideoCallDto> {
  const resp = await http.put(`/v1/elders/${elderId}/family/video-call/${callId}/signal`, body);
  return resp.data.data;
}

export async function patchFamilyVideoCall(
  elderId: string,
  callId: string,
  status: "ringing" | "connected" | "ended" | "declined"
): Promise<FamilyVideoCallDto> {
  const resp = await http.patch(`/v1/elders/${elderId}/family/video-call/${callId}`, { status });
  return resp.data.data;
}

export function formatFamilyMessageTime(iso: string): string {
  if (!iso) return "";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleString(undefined, {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
}

export function mapFamilyChatToUi(
  rows: FamilyChatMessageDto[],
  viewerRole: "ELDER" | "CHILD"
): { id: string; mine: boolean; text: string; meta: string; senderName?: string }[] {
  return rows.map(m => ({
    id: m.id,
    mine: m.senderRole === viewerRole,
    text: m.senderRole === viewerRole ? m.content : `${m.senderName}：${m.content}`,
    meta: formatFamilyMessageTime(m.createdAt),
    senderName: m.senderName
  }));
}

export function mapFamilyAlbumToUi(rows: FamilyAlbumPhotoDto[]): {
  id: string;
  name: string;
  url: string;
  createdAt: string;
}[] {
  return rows.map(p => ({
    id: p.id,
    name: p.fileName,
    url: resolveMediaUrl(p.url),
    createdAt: formatFamilyMessageTime(p.createdAt)
  }));
}
