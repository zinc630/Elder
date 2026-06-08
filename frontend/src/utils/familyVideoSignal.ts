/** 子女端 ↔ 老人端 视频通话信令（后端同步，双端轮询） */

import {
  fetchFamilyVideoCall,
  patchFamilyVideoCall,
  startFamilyVideoCall
} from "../api/family";

export type FamilyVideoCallStatus = "ringing" | "connected" | "ended" | "declined";
export type FamilyVideoInitiator = "ELDER" | "CHILD";

export type FamilyVideoCallSession = {
  callId: string;
  elderId: string;
  childId: string;
  childName: string;
  initiatorRole: FamilyVideoInitiator;
  elderDisplayName: string;
  status: FamilyVideoCallStatus;
  updatedAt: number;
};

function toSession(dto: {
  callId: string;
  elderId: string;
  childId: string | null;
  childName: string;
  initiatorRole?: string;
  elderDisplayName?: string | null;
  status: string;
  updatedAt: string;
}): FamilyVideoCallSession {
  const initiator = (dto.initiatorRole === "ELDER" ? "ELDER" : "CHILD") as FamilyVideoInitiator;
  return {
    callId: dto.callId,
    elderId: dto.elderId,
    childId: dto.childId ?? "",
    childName: dto.childName,
    initiatorRole: initiator,
    elderDisplayName: dto.elderDisplayName ?? "",
    status: dto.status as FamilyVideoCallStatus,
    updatedAt: new Date(dto.updatedAt).getTime()
  };
}

/** 来电显示名称 */
export function familyCallDisplayName(session: FamilyVideoCallSession): string {
  if (session.initiatorRole === "ELDER") {
    return session.elderDisplayName || "老人";
  }
  return session.childName || "子女";
}

export async function readFamilyVideoSession(elderId: string): Promise<FamilyVideoCallSession | null> {
  if (!elderId) return null;
  try {
    const dto = await fetchFamilyVideoCall(elderId);
    if (!dto?.callId) return null;
    return toSession(dto);
  } catch {
    return null;
  }
}

export async function writeFamilyVideoSession(
  session: Omit<FamilyVideoCallSession, "callId" | "updatedAt" | "status"> & { status?: FamilyVideoCallStatus }
): Promise<FamilyVideoCallSession> {
  const dto = await startFamilyVideoCall(session.elderId, {
    childUserId: session.childId || undefined,
    callerDisplayName: session.childName,
    initiatorRole: session.initiatorRole,
    elderDisplayName: session.elderDisplayName || undefined
  });
  return toSession(dto);
}

export async function clearFamilyVideoSession(elderId: string, callId?: string): Promise<void> {
  if (!elderId) return;
  const current = await readFamilyVideoSession(elderId);
  const id = callId ?? current?.callId;
  if (!id) return;
  await patchFamilyVideoSession(elderId, id, "ended");
}

export async function patchFamilyVideoSession(
  elderId: string,
  callId: string,
  status: FamilyVideoCallStatus
): Promise<FamilyVideoCallSession | null> {
  try {
    const dto = await patchFamilyVideoCall(elderId, callId, status);
    return toSession(dto);
  } catch {
    return null;
  }
}

/** 监听信令变化（轮询后端） */
export function subscribeFamilyVideoSession(
  elderId: string,
  onChange: (session: FamilyVideoCallSession | null) => void,
  pollMs = 800
) {
  let stopped = false;

  const emit = async () => {
    if (stopped || !elderId) return;
    try {
      onChange(await readFamilyVideoSession(elderId));
    } catch {
      onChange(null);
    }
  };

  const timer = window.setInterval(() => void emit(), pollMs);
  void emit();

  return () => {
    stopped = true;
    window.clearInterval(timer);
  };
}
