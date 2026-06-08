package com.example.elder.dto;

public record AdminDashboardStatsDto(
        long totalElders,
        long todayAlerts,
        long monthlyServiceOrders,
        long boundDevices,
        long pendingOrders,
        long pendingAlarms,
        long completedOrdersMonth,
        long totalAgencies,
        long totalChildren
) {
}
