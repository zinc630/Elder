package com.example.elder.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AgencyFinanceDto(
        String id,
        String elderName,
        String serviceType,
        BigDecimal amount,
        String status,
        Instant paidAt
) {
}
