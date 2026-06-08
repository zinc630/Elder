import type { AlarmDto } from "../api/alarm";

/** 子女端 / 机构端轮询告警间隔 */
export const ALARM_POLL_INTERVAL_MS = 8000;

export function isPendingAlarm(a: Pick<AlarmDto, "status">): boolean {
  const s = a.status;
  return s !== "CONFIRMED" && s !== "CLOSED";
}

export function isSosAlarm(a: Pick<AlarmDto, "type">): boolean {
  return a.type === "SOS_SUSPECTED";
}

export function alarmTypeLabel(type: string): string {
  switch (type) {
    case "FALL_SUSPECTED":
      return "跌倒预警";
    case "SOS_SUSPECTED":
      return "SOS 紧急求助";
    case "VITALS_LONG_ABNORMAL":
      return "体征异常";
    default:
      return "设备告警";
  }
}

export function alarmStatusLabel(status: string): string {
  switch (status) {
    case "CHILD_NOTIFIED":
      return "待响应";
    case "PENDING_CONFIRM":
      return "待确认";
    case "CALL_PENDING":
      return "电话升级中";
    case "CONFIRMED":
      return "已确认";
    case "CLOSED":
      return "已关闭";
    default:
      return status;
  }
}

export function alarmDescription(a: Pick<AlarmDto, "type">): string {
  if (a.type === "SOS_SUSPECTED") {
    return "老人端已触发一键呼救，请立即联系老人并通知服务机构到场处置。";
  }
  if (a.type === "FALL_SUSPECTED") {
    return "系统检测到疑似跌倒，请尽快确认老人安全状况。";
  }
  if (a.type === "VITALS_LONG_ABNORMAL") {
    return "心率等指标持续偏离阈值，建议查看处置指南。";
  }
  return "系统已检测到异常情况，建议查看处置指南。";
}

export function formatAlarmTime(iso: string): string {
  if (!iso) return "";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleString();
}
