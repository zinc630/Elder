package com.example.elder.dto;

import java.util.List;

public record NearbyMedicalResponseDto<T>(
        String source,
        String userAddress,
        String regionLabel,
        String location,
        List<T> items
) {
}
