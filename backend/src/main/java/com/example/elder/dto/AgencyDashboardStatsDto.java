package com.example.elder.dto;

/** 服务机构运营总览（与前端四张指标卡同步） */
public record AgencyDashboardStatsDto(
        long totalElders,
        long agencyCount,
        long todayOrders,
        long pendingOrders,
        /** 累计已完成工单数 */
        long completedTotal,
        int completionRatePercent,
        long onlineWorkers,
        long totalWorkers
) {
}
