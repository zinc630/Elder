package com.example.elder.service.inference;

public record LongAbnormalResult(
        boolean longAbnormal,
        int abnormalPointCount,
        int abnormalPointThreshold,
        double riskScore
) {
}

