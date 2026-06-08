package com.example.elder.domain.portal;

import java.time.Instant;

public record PortalRegistration(
        String id,
        PortalRegistrationType type,
        String targetId,
        String userId,
        String contactName,
        String contactPhone,
        String note,
        String status,
        Instant createdAt
) {
    public enum PortalRegistrationType {
        ACTIVITY,
        LIFE,
        COURSE
    }
}
