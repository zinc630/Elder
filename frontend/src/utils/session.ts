import type { LoginResult } from "../api/authServer";

/** 按角色写入登录态，避免机构/子女姓名污染老人端展示 */
export function persistLoginSession(res: LoginResult, loginDisplayName: string) {
  const name = loginDisplayName.trim();
  localStorage.setItem("elder.role", res.role);
  localStorage.setItem("elder.userId", res.userId);

  localStorage.removeItem("elder.userName");
  localStorage.removeItem("elder.accountName");
  localStorage.removeItem("child.userId");
  localStorage.removeItem("child.userName");
  localStorage.removeItem("agency.userId");
  localStorage.removeItem("agency.userName");

  if (res.role === "ELDER") {
    localStorage.setItem("elder.id", res.userId);
    localStorage.setItem("elder.userName", name);
    localStorage.setItem("elder.accountName", name);
  } else if (res.role === "CHILD") {
    localStorage.setItem("child.userId", res.userId);
    localStorage.setItem("child.userName", name);
    if (res.elderId) {
      localStorage.setItem("elder.id", res.elderId);
    } else {
      localStorage.removeItem("elder.id");
    }
  } else if (res.role === "AGENCY") {
    localStorage.setItem("agency.userId", res.userId);
    localStorage.setItem("agency.userName", name);
    localStorage.removeItem("elder.id");
  } else {
    localStorage.removeItem("elder.id");
  }

  if (res.linkedElderIds && res.linkedElderIds.length > 0) {
    localStorage.setItem("elder.linkedElders", JSON.stringify(res.linkedElderIds));
  } else if (res.role === "ELDER" && res.userId) {
    localStorage.setItem("elder.linkedElders", JSON.stringify([res.userId]));
  } else {
    localStorage.setItem("elder.linkedElders", JSON.stringify(res.elderId ? [res.elderId] : []));
  }
}

export function childSessionUserId(): string {
  return localStorage.getItem("child.userId") || localStorage.getItem("elder.userId") || "";
}

export function childSessionUserName(): string {
  return localStorage.getItem("child.userName") || "";
}
