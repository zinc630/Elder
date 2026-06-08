package com.example.elder.dto;

public record PublicHomeSummaryDto(
        int roleCount,
        int moduleCount,
        long elderCount,
        long todayAlarms,
        long monthlyTasks,
        long deviceCount,
        boolean deepSeekConfigured,
        String notice
) {}
