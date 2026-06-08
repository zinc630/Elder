import http from "./http";

export type RegisterRole = "ELDER" | "CHILD" | "AGENCY" | "ADMIN";

export type LoginResult = {
  role: RegisterRole;
  userId: string;
  elderId?: string;
  linkedElderIds?: string[];
  address?: string;
};

function extractMsg(e: any, fallback: string): string {
  return e?.response?.data?.message ?? e?.message ?? fallback;
}

export async function registerUserServer(payload: {
  userId?: string;
  role: RegisterRole;
  userName: string;
  password: string;
  gender?: string;
  age?: number | null;
  address?: string;
  linkedElderId?: string;
  linkedChildId?: string;
}): Promise<string> {
  try {
    const resp = await http.post("/v1/auth/register", payload);
    const userId = resp.data?.data?.userId as string | undefined;
    if (!userId) throw new Error("注册失败：后端未返回系统账号ID");
    return userId;
  } catch (e) {
    throw new Error(extractMsg(e, "注册失败"));
  }
}

export async function loginUserServer(role: RegisterRole, userName: string, password: string): Promise<LoginResult> {
  try {
    const resp = await http.post("/v1/auth/login", { role, userName, password });
    return resp.data?.data as LoginResult;
  } catch (e) {
    throw new Error(extractMsg(e, "登录失败"));
  }
}

export async function getSystemUserIdIfExistsServer(role: RegisterRole, userName: string): Promise<string | null> {
  if (!userName.trim()) return null;
  try {
    const resp = await http.get("/v1/auth/system-id", { params: { role, userName: userName.trim() } });
    return (resp.data?.data?.userId as string | null) ?? null;
  } catch {
    return null;
  }
}

export async function getUserBySystemIdServer(userId: string): Promise<{ userId: string; role: RegisterRole; userName: string } | null> {
  if (!userId.trim()) return null;
  try {
    const resp = await http.get("/v1/auth/user-by-id", { params: { userId: userId.trim() } });
    return resp.data?.data ?? null;
  } catch {
    return null;
  }
}

export async function allocateSystemUserIdServer(role: RegisterRole): Promise<string> {
  try {
    const resp = await http.get("/v1/auth/allocate-id", { params: { role } });
    const userId = resp.data?.data?.userId as string | undefined;
    if (!userId) throw new Error("后端未返回系统账号ID");
    return userId;
  } catch (e) {
    throw new Error(extractMsg(e, "系统账号ID分配失败"));
  }
}

export async function previewNextSystemUserIdServer(role: RegisterRole): Promise<string> {
  try {
    const resp = await http.get("/v1/auth/next-id", { params: { role } });
    const userId = resp.data?.data?.userId as string | undefined;
    if (!userId) throw new Error("后端未返回系统账号ID");
    return userId;
  } catch (e) {
    throw new Error(extractMsg(e, "系统账号ID预览失败"));
  }
}

export async function bindChildEldersServer(childUserId: string, elderUserIds: string[]): Promise<void> {
  try {
    await http.post("/v1/auth/bind/child-elders", { childUserId, elderUserIds });
  } catch (e) {
    throw new Error(extractMsg(e, "绑定老人失败"));
  }
}

