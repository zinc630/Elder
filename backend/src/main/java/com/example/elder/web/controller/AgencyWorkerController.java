package com.example.elder.web.controller;

import com.example.elder.domain.ServiceWorker;
import com.example.elder.domain.Role;
import com.example.elder.dto.ServiceWorkerDto;
import com.example.elder.dto.WorkerCreateRequest;
import com.example.elder.service.agency.AgencyDbService;
import com.example.elder.web.ApiResponse;
import com.example.elder.web.ForbiddenException;
import com.example.elder.web.RoleResolver;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/agency")
public class AgencyWorkerController {
    private final AgencyDbService agencyDbService;

    public AgencyWorkerController(AgencyDbService agencyDbService) {
        this.agencyDbService = agencyDbService;
    }

    @GetMapping("/workers")
    public ApiResponse<List<ServiceWorkerDto>> listWorkers(@RequestParam(value = "keyword", required = false) String keyword,
                                                              @RequestParam(value = "serviceType", required = false) String serviceType,
                                                              @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        Role role = RoleResolver.resolve(roleHeader);
        if (!Role.agencyOrAdmin(role)) throw new ForbiddenException("agency_only");

        List<ServiceWorker> workers = agencyDbService.listWorkers(keyword, serviceType);
        List<ServiceWorkerDto> dtos = workers.stream().map(this::toDto).toList();
        return ApiResponse.ok(dtos);
    }

    @PostMapping("/workers")
    public ApiResponse<Object> addWorker(@Valid @RequestBody WorkerCreateRequest req,
                                          @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        Role role = RoleResolver.resolve(roleHeader);
        if (!Role.agencyOrAdmin(role)) throw new ForbiddenException("agency_only");

        ServiceWorker.OnlineStatus onlineStatus = parseOnlineStatus(req.onlineStatus());
        String serviceType = deriveServiceType(req.position());

        ServiceWorker w = new ServiceWorker(req.name(), req.position(), req.phone(), onlineStatus, serviceType);
        agencyDbService.addWorker(w);
        return ApiResponse.ok(w.getId());
    }

    @DeleteMapping("/workers/{workerId}")
    public ApiResponse<Object> delete(@PathVariable("workerId") String workerId,
                                       @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        Role role = RoleResolver.resolve(roleHeader);
        if (!Role.agencyOrAdmin(role)) throw new ForbiddenException("agency_only");
        agencyDbService.deleteWorker(workerId);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/workers/{workerId}")
    public ApiResponse<Object> updateWorkerStatus(@PathVariable("workerId") String workerId,
                                                    @RequestBody WorkerCreateRequest req,
                                                    @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        Role role = RoleResolver.resolve(roleHeader);
        if (!Role.agencyOrAdmin(role)) throw new ForbiddenException("agency_only");

        ServiceWorker existing = agencyDbService.getWorker(workerId);
        if (existing == null) return ApiResponse.fail("worker_not_found");

        ServiceWorker patch = new ServiceWorker(workerId, req.name(), req.position(), req.phone(),
                parseOnlineStatus(req.onlineStatus()), deriveServiceType(req.position()), Instant.now());
        agencyDbService.updateWorker(workerId, patch);
        return ApiResponse.ok(null);
    }

    private ServiceWorkerDto toDto(ServiceWorker w) {
        return new ServiceWorkerDto(
                w.getId(),
                w.getName(),
                w.getPosition(),
                w.getPhone(),
                w.getOnlineStatus(),
                w.getServiceType(),
                w.getCreatedAt()
        );
    }

    private ServiceWorker.OnlineStatus parseOnlineStatus(String raw) {
        if (raw == null || raw.isBlank()) return ServiceWorker.OnlineStatus.ONLINE;
        String s = raw.trim().toUpperCase();
        return s.equals("OFFLINE") ? ServiceWorker.OnlineStatus.OFFLINE : ServiceWorker.OnlineStatus.ONLINE;
    }

    // Simplified mapping: use position keywords to derive capability type.
    private String deriveServiceType(String position) {
        if (position == null) return "NURSING";
        if (position.contains("助餐")) return "NURSING";
        if (position.contains("助浴")) return "BATH";
        if (position.contains("保洁") || position.contains("清洁")) return "HOUSEKEEPING";
        if (position.contains("陪诊") || position.contains("陪护")) return "ACCOMPANY";
        return "NURSING";
    }
}

