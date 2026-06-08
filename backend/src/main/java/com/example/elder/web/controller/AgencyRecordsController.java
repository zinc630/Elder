package com.example.elder.web.controller;

import com.example.elder.domain.AgencyAnomalyReport;
import com.example.elder.domain.AgencyClockInRecord;
import com.example.elder.domain.Role;
import com.example.elder.dto.AgencyAnomalyReportDto;
import com.example.elder.dto.AgencyClockInRecordDto;
import com.example.elder.service.agency.AgencyDbService;
import com.example.elder.web.ApiResponse;
import com.example.elder.web.ForbiddenException;
import com.example.elder.web.RoleResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agency")
public class AgencyRecordsController {
    private final AgencyDbService agencyDbService;

    public AgencyRecordsController(AgencyDbService agencyDbService) {
        this.agencyDbService = agencyDbService;
    }

    @GetMapping("/anomalies")
    public ApiResponse<List<AgencyAnomalyReportDto>> listAnomalies(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        List<AgencyAnomalyReportDto> dtos = agencyDbService.listAnomalies().stream()
                .map(this::toAnomalyDto)
                .toList();
        return ApiResponse.ok(dtos);
    }

    @PostMapping("/anomalies/{anomalyId}/process")
    public ApiResponse<Object> processAnomaly(
            @PathVariable("anomalyId") String anomalyId,
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        agencyDbService.processAnomaly(anomalyId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/clock-ins")
    public ApiResponse<List<AgencyClockInRecordDto>> listClockIns(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAgency(roleHeader);
        List<AgencyClockInRecordDto> dtos = agencyDbService.listClockIns().stream()
                .map(this::toClockDto)
                .toList();
        return ApiResponse.ok(dtos);
    }

    private void requireAgency(String roleHeader) {
        Role role = RoleResolver.resolve(roleHeader);
        if (!Role.agencyOrAdmin(role)) throw new ForbiddenException("agency_only");
    }

    private AgencyAnomalyReportDto toAnomalyDto(AgencyAnomalyReport r) {
        return new AgencyAnomalyReportDto(
                r.getId(),
                r.getReporterName(),
                r.getElderId(),
                r.getElderName(),
                r.getAnomalyType(),
                r.getDescription(),
                r.getReportedAt(),
                r.getStatus()
        );
    }

    private AgencyClockInRecordDto toClockDto(AgencyClockInRecord r) {
        return new AgencyClockInRecordDto(
                r.getId(),
                r.getWorkerName(),
                r.getElderName(),
                r.getServiceTypeLabel(),
                r.getClockAt(),
                r.getAddress(),
                r.getStatusLabel()
        );
    }
}
