package com.example.elder.dto;

import java.time.Instant;

public record FamilyAlbumPhotoDto(
        String id,
        String elderId,
        String uploadedByRole,
        String uploadedByUserId,
        String fileName,
        String url,
        Instant createdAt
) {
}
