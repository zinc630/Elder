package com.example.elder.dto;

public record NearbyHospitalDto(
        String id,
        String name,
        String address,
        String distance,
        String phone
) {
}
