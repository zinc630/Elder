package com.example.elder.web.controller;

import com.example.elder.domain.ChildProfile;
import com.example.elder.dto.ChildProfileDto;
import com.example.elder.dto.ChildProfileUpsertRequest;
import com.example.elder.service.child.ChildDbService;
import com.example.elder.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ChildProfileController {

    private final ChildDbService childDbService;

    public ChildProfileController(ChildDbService childDbService) {
        this.childDbService = childDbService;
    }

    @GetMapping("/children/{childId}/profile")
    public ApiResponse<ChildProfileDto> profile(@PathVariable("childId") String childId) {
        ChildProfile p = childDbService.getChildProfile(childId)
                .orElseGet(() -> new ChildProfile(childId, "未配置子女", null, "", "", ""));
        return ApiResponse.ok(toDto(p));
    }

    @PostMapping("/children/{childId}/profile")
    public ApiResponse<ChildProfileDto> upsertProfile(
            @PathVariable("childId") String childId,
            @Valid @RequestBody ChildProfileUpsertRequest req
    ) {
        ChildProfile p = new ChildProfile(
                childId,
                req.name(),
                req.age(),
                req.gender(),
                req.address() == null ? "" : req.address(),
                req.relationDesc() == null ? "" : req.relationDesc()
        );
        return ApiResponse.ok(toDto(childDbService.upsertChildProfile(p)));
    }

    private static ChildProfileDto toDto(ChildProfile p) {
        return new ChildProfileDto(
                p.getChildId(),
                p.getName(),
                p.getAge(),
                p.getGender(),
                p.getAddress(),
                p.getRelationDesc()
        );
    }
}
