package com.example.elder.dto;

public record FamilyVideoSignalRequest(
        String offerSdp,
        String answerSdp
) {
}
