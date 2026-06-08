package com.example.elder.dto;

public record ElderProfileDto(
        String elderId,
        String name,
        Integer age,
        String gender,
        String address,
        String keyHealthNotes
) {
}

