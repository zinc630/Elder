package com.example.elder.web.controller;

import com.example.elder.dto.MeasurementIngestRequest;
import com.example.elder.dto.MeasurementIngestResponse;
import com.example.elder.service.AlarmService;
import com.example.elder.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MeasurementController {
    private final AlarmService alarmService;

    public MeasurementController(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    /**
     * Wristband -> platform. HTTP upload.
     */
    @PostMapping("/measurements")
    public ApiResponse<MeasurementIngestResponse> ingest(@Valid @RequestBody MeasurementIngestRequest req) {
        MeasurementIngestResponse resp = alarmService.ingestMeasurement(req);
        return ApiResponse.ok(resp);
    }
}

