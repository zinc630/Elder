package com.example.elder.dto;

public record NearbyDoctorDto(
        String id,
        String name,
        String title,
        String hospital,
        String specialty,
        String phone
) {
}
