package com.example.elder.dto;

public record ChildProfileDto(
        String childId,
        String name,
        Integer age,
        String gender,
        String address,
        String relationDesc
) {
}
