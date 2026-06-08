package com.example.elder.web.controller;

import com.example.elder.domain.ElderGeofence;
import com.example.elder.domain.ElderLastLocation;
import com.example.elder.domain.ElderProfile;
import com.example.elder.dto.ElderLocationSnapshotDto;
import com.example.elder.dto.ElderProfileDto;
import com.example.elder.dto.ElderProfileUpsertRequest;
import com.example.elder.dto.NearbyDoctorDto;
import com.example.elder.dto.NearbyHospitalDto;
import com.example.elder.dto.NearbyMedicalResponseDto;
import com.example.elder.service.elder.NearbyMedicalResourceService;
import com.example.elder.dto.GeofenceDto;
import com.example.elder.dto.LocationPointDto;
import com.example.elder.dto.LocationReportRequest;
import com.example.elder.service.elder.ElderDbService;
import com.example.elder.util.GeoUtils;
import com.example.elder.web.ApiResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ElderProfileController {
    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;

    private final ElderDbService elderDbService;
    private final NearbyMedicalResourceService nearbyMedicalResourceService;

    public ElderProfileController(ElderDbService elderDbService,
                                  NearbyMedicalResourceService nearbyMedicalResourceService) {
        this.elderDbService = elderDbService;
        this.nearbyMedicalResourceService = nearbyMedicalResourceService;
    }

    @GetMapping("/elders/{elderId}/profile")
    public ApiResponse<ElderProfileDto> profile(@PathVariable("elderId") String elderId) {
        ElderProfile p = elderDbService.getElderProfile(elderId)
                .orElseGet(() -> new ElderProfile(elderId, "未配置老人", null, "", "", ""));

        ElderProfileDto dto = new ElderProfileDto(
                p.getElderId(),
                p.getName(),
                p.getAge(),
                p.getGender(),
                p.getAddress(),
                p.getKeyHealthNotes()
        );
        return ApiResponse.ok(dto);
    }

    @PostMapping("/elders/{elderId}/profile")
    public ApiResponse<ElderProfileDto> upsertProfile(@PathVariable("elderId") String elderId,
                                                        @Valid @org.springframework.web.bind.annotation.RequestBody ElderProfileUpsertRequest req) {
        // In this simplified demo, elderId is provided by client (we map elderId=elder account userName).
        ElderProfile p = new ElderProfile(elderId, req.name(), req.age(), req.gender(), req.address(), req.keyHealthNotes());
        elderDbService.upsertElderProfile(p);

        if (elderDbService.getGeofence(elderId).isEmpty()) {
            elderDbService.upsertGeofence(new ElderGeofence(
                    elderId,
                    31.2304,
                    121.4737,
                    500,
                    "默认电子围栏（新档案演示）"
            ));
        }

        ElderProfileDto dto = new ElderProfileDto(
                p.getElderId(),
                p.getName(),
                p.getAge(),
                p.getGender(),
                p.getAddress(),
                p.getKeyHealthNotes()
        );
        return ApiResponse.ok(dto);
    }

    @GetMapping("/elders/{elderId}/nearby-hospitals")
    public ApiResponse<NearbyMedicalResponseDto<NearbyHospitalDto>> nearbyHospitals(
            @PathVariable("elderId") String elderId
    ) {
        return ApiResponse.ok(nearbyMedicalResourceService.hospitalsForElder(elderId));
    }

    @GetMapping("/elders/{elderId}/nearby-doctors")
    public ApiResponse<NearbyMedicalResponseDto<NearbyDoctorDto>> nearbyDoctors(
            @PathVariable("elderId") String elderId
    ) {
        return ApiResponse.ok(nearbyMedicalResourceService.doctorsForElder(elderId));
    }

    @GetMapping("/elders")
    public ApiResponse<List<ElderProfileDto>> listElders() {
        List<ElderProfile> list = elderDbService.listElders();
        List<ElderProfileDto> dtos = list.stream()
                .map(p -> new ElderProfileDto(
                        p.getElderId(),
                        p.getName(),
                        p.getAge(),
                        p.getGender(),
                        p.getAddress(),
                        p.getKeyHealthNotes()
                ))
                .toList();
        return ApiResponse.ok(dtos);
    }

    /**
     * 长者位置快照（与档案同控制器注册，避免部分环境下单独 Controller 未生效导致 404）。
     */
    @GetMapping("/elders/{elderId}/location")
    public ApiResponse<ElderLocationSnapshotDto> locationSnapshot(@PathVariable("elderId") String elderId) {
        return ApiResponse.ok(buildLocationSnapshot(elderId));
    }

    @PostMapping("/elders/{elderId}/location/geofence/default")
    public ApiResponse<ElderLocationSnapshotDto> ensureDefaultGeofence(@PathVariable("elderId") String elderId) {
        var geofenceOpt = elderDbService.getGeofence(elderId);
        if (geofenceOpt.isEmpty()) {
            double centerLat = 31.2304;
            double centerLng = 121.4737;
            String label = "默认电子围栏（系统补建）";

            var locationOpt = elderDbService.getLastLocation(elderId);
            if (locationOpt.isPresent()) {
                centerLat = locationOpt.get().lat();
                centerLng = locationOpt.get().lng();
                label = "默认电子围栏（按最近定位生成）";
            } else {
                var profileOpt = elderDbService.getElderProfile(elderId);
                String address = profileOpt.map(ElderProfile::getAddress).orElse("");
                if (address != null && address.contains("杭州")) {
                    centerLat = 30.274084;
                    centerLng = 120.155070;
                } else if (address != null && address.contains("北京")) {
                    centerLat = 39.9042;
                    centerLng = 116.4074;
                } else if (address != null && address.contains("深圳")) {
                    centerLat = 22.5431;
                    centerLng = 114.0579;
                }
            }

            elderDbService.upsertGeofence(new ElderGeofence(
                    elderId,
                    centerLat,
                    centerLng,
                    500,
                    label
            ));
        }
        return ApiResponse.ok(buildLocationSnapshot(elderId));
    }

    @PostMapping("/elders/{elderId}/location")
    public ApiResponse<ElderLocationSnapshotDto> reportLocation(
            @PathVariable("elderId") String elderId,
            @Valid @RequestBody LocationReportRequest req
    ) {
        String source = req.source() == null || req.source().isBlank() ? "client_report" : req.source().trim();
        ElderLastLocation loc = new ElderLastLocation(
                req.lat(),
                req.lng(),
                req.accuracyMeters(),
                source,
                Instant.now()
        );
        elderDbService.upsertLastLocation(elderId, loc);
        return ApiResponse.ok(buildLocationSnapshot(elderId));
    }

    private ElderLocationSnapshotDto buildLocationSnapshot(String elderId) {
        var locOpt = elderDbService.getLastLocation(elderId);
        var fenceOpt = elderDbService.getGeofence(elderId);

        LocationPointDto locDto = locOpt
                .map(l -> new LocationPointDto(
                        l.lat(),
                        l.lng(),
                        l.accuracyMeters(),
                        l.source(),
                        ISO_INSTANT.format(l.updatedAt())
                ))
                .orElse(null);

        GeofenceDto fenceDto = fenceOpt
                .map(f -> new GeofenceDto(f.centerLat(), f.centerLng(), f.radiusMeters(), f.label()))
                .orElse(null);

        boolean inside = false;
        Double dist = null;
        if (locOpt.isPresent() && fenceOpt.isPresent()) {
            ElderLastLocation l = locOpt.get();
            ElderGeofence f = fenceOpt.get();
            dist = GeoUtils.distanceMeters(l.lat(), l.lng(), f.centerLat(), f.centerLng());
            inside = GeoUtils.isInsideCircle(
                    l.lat(), l.lng(),
                    f.centerLat(), f.centerLng(),
                    f.radiusMeters()
            );
        }

        return new ElderLocationSnapshotDto(elderId, locDto, fenceDto, inside, dist);
    }
}

