export type RegisterRole = "ELDER" | "CHILD" | "AGENCY" | "ADMIN";

export type UserRecord = {
  /** System-assigned unique account ID (shown at register / login). */
  userId: string;
  role: RegisterRole;
  userName: string;
  password: string;

  gender?: string;
  /** 住址；机构/管理员注册时亦用作机构或院区展示名 */
  address?: string;
  age?: number | null;

  // Linked info for elder/child relationship (demo-only)
  // Values are the related account's system userId (comma-separated for multiple elders).
  linkedElderId?: string; // when role=CHILD
  linkedChildId?: string; // when role=ELDER
};

/** 按角色前缀隔离系统账号 ID，各身份独立递增序号。 */
const ROLE_ID_PREFIX: Record<RegisterRole, string> = {
  ELDER: "E",
  CHILD: "C",
  AGENCY: "G",
  ADMIN: "A"
};

function seqStorageKey(role: RegisterRole): string {
  return `elder.systemIdSeq.${role}`;
}

/**
 * 为指定角色分配下一个系统账号 ID（每角色独立序号，前缀区分身份）。
 * 历史数据可能仍为 `U…` 等旧格式，仍视为合法账号 ID。
 */
export function allocateSystemUserId(role: RegisterRole): string {
  const key = seqStorageKey(role);
  let n = Number(localStorage.getItem(key) ?? "100000");
  if (!Number.isFinite(n)) n = 100000;
  n += 1;
  localStorage.setItem(key, String(n));
  return `${ROLE_ID_PREFIX[role]}${n}`;
}

function canonicalStoredId(u: Pick<UserRecord, "userId" | "userName">): string {
  return (u.userId || u.userName).trim();
}

function isUserIdTaken(users: UserRecord[], userId: string): boolean {
  const t = userId.trim();
  return users.some((u) => canonicalStoredId(u) === t);
}

function parseLinkedElders(input?: string): string[] {
  return normalizeLinkedIds(input);
}

const LS_USERS_KEY = "elder.users.v1";

function readUsers(): UserRecord[] {
  const raw = localStorage.getItem(LS_USERS_KEY);
  if (!raw) return [];
  try {
    const parsed = JSON.parse(raw) as UserRecord[];
    if (!Array.isArray(parsed)) return [];
    let migrated = false;
    for (const u of parsed) {
      if (!u.userId) {
        (u as UserRecord).userId = u.userName;
        migrated = true;
      }
    }
    if (migrated) writeUsers(parsed);
    return parsed;
  } catch {
    return [];
  }
}

function writeUsers(users: UserRecord[]) {
  localStorage.setItem(LS_USERS_KEY, JSON.stringify(users));
}

function normalizeLinkedIds(input?: string): string[] {
  if (!input) return [];
  const parts = input
    .split(/[,\uFF0C;\uFF1B\s]+/)
    .map((s) => s.trim().toUpperCase())
    .filter(Boolean);
  return Array.from(new Set(parts));
}

function joinLinkedIds(ids: string[]): string {
  return ids.join(",");
}

function findUserBySystemId(users: UserRecord[], userId: string): UserRecord | undefined {
  const id = userId.trim().toUpperCase();
  return users.find((u) => canonicalStoredId(u).toUpperCase() === id);
}

function appendLinkedId(existing: string | undefined, targetId: string): string {
  const ids = normalizeLinkedIds(existing);
  const normalized = targetId.trim().toUpperCase();
  if (!ids.includes(normalized)) ids.push(normalized);
  return joinLinkedIds(ids);
}

/**
 * Saves a new user. If record.userId is omitted, allocates one.
 * @returns The system userId stored for this account.
 */
export function registerUser(record: Omit<UserRecord, "userId"> & { userId?: string }): string {
  const users = readUsers();
  const exists = users.some((u) => u.role === record.role && u.userName === record.userName);
  if (exists) throw new Error("用户已存在");
  const userId = (record.userId?.trim() || allocateSystemUserId(record.role)).toUpperCase();
  if (isUserIdTaken(users, userId)) throw new Error("系统账号ID已被占用");

  const full: UserRecord = { ...record, userId };
  if (record.role === "CHILD") {
    const linkedElderIds = normalizeLinkedIds(record.linkedElderId);
    if (linkedElderIds.length === 0) throw new Error("请至少关联一个老人系统账号ID");
    for (const elderId of linkedElderIds) {
      const elder = findUserBySystemId(users, elderId);
      if (!elder) throw new Error(`未找到关联老人账号：${elderId}`);
      if (elder.role !== "ELDER") throw new Error(`关联账号不是老人身份：${elderId}`);
    }
    full.linkedElderId = joinLinkedIds(linkedElderIds);
  }
  if (record.role === "ELDER") {
    const linkedChildId = record.linkedChildId?.trim().toUpperCase();
    if (!linkedChildId) throw new Error("请输入关联子女系统账号ID");
    const child = findUserBySystemId(users, linkedChildId);
    if (!child) throw new Error(`未找到关联子女账号：${linkedChildId}`);
    if (child.role !== "CHILD") throw new Error(`关联账号不是子女身份：${linkedChildId}`);
    full.linkedChildId = linkedChildId;
  }

  users.push(full);

  // 自动补全双向关系，减少“单边绑定”导致的切换失败。
  if (full.role === "CHILD") {
    const elderIds = normalizeLinkedIds(full.linkedElderId);
    for (const elderId of elderIds) {
      const elder = findUserBySystemId(users, elderId);
      if (elder) elder.linkedChildId = appendLinkedId(elder.linkedChildId, full.userId);
    }
  }
  if (full.role === "ELDER" && full.linkedChildId) {
    const child = findUserBySystemId(users, full.linkedChildId);
    if (child) child.linkedElderId = appendLinkedId(child.linkedElderId, full.userId);
  }

  writeUsers(users);
  return userId;
}

export function loginUser(
  role: RegisterRole,
  userName: string,
  password: string
): { role: RegisterRole; userId: string; elderId?: string; linkedElderIds?: string[] } {
  const users = readUsers();
  const u = users.find((x) => x.role === role && x.userName === userName);
  if (!u) throw new Error("用户不存在");
  if (u.password !== password) throw new Error("密码错误");

  const userId = u.userId || u.userName;

  if (role === "ELDER") {
    // elderId for API/profile is the system userId (legacy: same as userName).
    return { role, userId, elderId: userId };
  }
  if (role === "CHILD") {
    const linked = parseLinkedElders(u.linkedElderId);
    if (linked.length === 0) throw new Error("未配置已绑定的老人系统ID");
    return { role, userId, elderId: linked[0], linkedElderIds: linked };
  }
  return { role, userId };
}

export function getUserByRoleAndNo(role: RegisterRole, userName: string): UserRecord | null {
  const users = readUsers();
  return users.find((x) => x.role === role && x.userName === userName) ?? null;
}

export function getUserBySystemId(userId: string): UserRecord | null {
  const users = readUsers();
  return findUserBySystemId(users, userId) ?? null;
}

/** If an account exists for this role + display name, return its system userId (for login page preview). */
export function getSystemUserIdIfExists(role: RegisterRole, userName: string): string | null {
  const u = getUserByRoleAndNo(role, userName.trim());
  if (!u) return null;
  return u.userId || u.userName;
}

