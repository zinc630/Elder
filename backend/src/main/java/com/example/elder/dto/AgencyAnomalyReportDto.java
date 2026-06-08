package com.example.elder.dto;

import com.example.elder.domain.AgencyAnomalyReport;

import java.time.Instant;

public record AgencyAnomalyReportDto(
        String id,
        String reporterName,
        String elderId,
        String elderName,
        String anomalyType,
        String description,
        Instant reportedAt,
        AgencyAnomalyReport.Status status
) {
}
