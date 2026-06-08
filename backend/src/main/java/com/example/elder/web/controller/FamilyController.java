package com.example.elder.web.controller;

import com.example.elder.domain.Role;
import com.example.elder.dto.FamilyAlbumPhotoDto;
import com.example.elder.dto.FamilyChatMessageDto;
import com.example.elder.dto.FamilySendMessageRequest;
import com.example.elder.dto.FamilyStartVideoCallRequest;
import com.example.elder.dto.FamilyVideoCallDto;
import com.example.elder.dto.FamilyVideoCallPatchRequest;
import com.example.elder.dto.FamilyVideoSignalRequest;
import com.example.elder.service.family.FamilyInteractionService;
import com.example.elder.web.ApiResponse;
import com.example.elder.web.ForbiddenException;
import com.example.elder.web.RoleResolver;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/elders/{elderId}/family")
public class FamilyController {
    private final FamilyInteractionService familyService;

    public FamilyController(FamilyInteractionService familyService) {
        this.familyService = familyService;
    }

    @GetMapping("/messages")
    public ApiResponse<List<FamilyChatMessageDto>> listMessages(
            @PathVariable("elderId") String elderId,
            @RequestParam(defaultValue = "100") int limit,
            @RequestHeader(name = "X-Role", required = false) String roleHeader
    ) {
        requireFamilyRole(roleHeader);
        return ApiResponse.ok(familyService.listMessages(elderId, limit));
    }

    @PostMapping("/messages")
    public ApiResponse<FamilyChatMessageDto> sendMessage(
            @PathVariable("elderId") String elderId,
            @RequestHeader(name = "X-Role", required = false) String roleHeader,
            @Valid @RequestBody FamilySendMessageRequest req
    ) {
        requireFamilyRole(roleHeader);
        return ApiResponse.ok(familyService.sendMessage(elderId, req));
    }

    @GetMapping("/album")
    public ApiResponse<List<FamilyAlbumPhotoDto>> listAlbum(
            @PathVariable("elderId") String elderId,
            @RequestHeader(name = "X-Role", required = false) String roleHeader
    ) {
        requireFamilyRole(roleHeader);
        return ApiResponse.ok(familyService.listAlbum(elderId));
    }

    @PostMapping("/album")
    public ApiResponse<FamilyAlbumPhotoDto> uploadAlbum(
            @PathVariable("elderId") String elderId,
            @RequestHeader(name = "X-Role", required = false) String roleHeader,
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "uploadedByRole", defaultValue = "CHILD") String uploadedByRole,
            @RequestParam(name = "uploadedByUserId", required = false) String uploadedByUserId
    ) throws IOException {
        requireFamilyRole(roleHeader);
        return ApiResponse.ok(familyService.uploadAlbum(elderId, uploadedByRole, uploadedByUserId, file));
    }

    @DeleteMapping("/album/{photoId}")
    public ApiResponse<Object> deleteAlbum(
            @PathVariable("elderId") String elderId,
            @PathVariable("photoId") String photoId,
            @RequestHeader(name = "X-Role", required = false) String roleHeader
    ) {
        requireFamilyRole(roleHeader);
        familyService.deleteAlbumPhoto(elderId, photoId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/video-call")
    public ApiResponse<FamilyVideoCallDto> getVideoCall(
            @PathVariable("elderId") String elderId,
            @RequestHeader(name = "X-Role", required = false) String roleHeader
    ) {
        requireFamilyRole(roleHeader);
        return ApiResponse.ok(familyService.getActiveVideoCall(elderId));
    }

    @PostMapping("/video-call")
    public ApiResponse<FamilyVideoCallDto> startVideoCall(
            @PathVariable("elderId") String elderId,
            @RequestHeader(name = "X-Role", required = false) String roleHeader,
            @Valid @RequestBody FamilyStartVideoCallRequest req
    ) {
        requireFamilyRole(roleHeader);
        return ApiResponse.ok(familyService.startVideoCall(elderId, req));
    }

    @PatchMapping("/video-call/{callId}")
    public ApiResponse<FamilyVideoCallDto> patchVideoCall(
            @PathVariable("elderId") String elderId,
            @PathVariable("callId") String callId,
            @RequestHeader(name = "X-Role", required = false) String roleHeader,
            @Valid @RequestBody FamilyVideoCallPatchRequest req
    ) {
        requireFamilyRole(roleHeader);
        return ApiResponse.ok(familyService.patchVideoCall(elderId, callId, req.status()));
    }

    @PutMapping("/video-call/{callId}/signal")
    public ApiResponse<FamilyVideoCallDto> updateVideoSignal(
            @PathVariable("elderId") String elderId,
            @PathVariable("callId") String callId,
            @RequestHeader(name = "X-Role", required = false) String roleHeader,
            @RequestBody FamilyVideoSignalRequest req
    ) {
        requireFamilyRole(roleHeader);
        return ApiResponse.ok(familyService.updateVideoSignal(
                elderId,
                callId,
                req.offerSdp(),
                req.answerSdp()
        ));
    }

    private static void requireFamilyRole(String roleHeader) {
        Role role = RoleResolver.resolve(roleHeader);
        if (role != Role.ELDER && role != Role.CHILD && role != Role.ADMIN) {
            throw new ForbiddenException("role_not_allowed_for_family");
        }
    }
}
