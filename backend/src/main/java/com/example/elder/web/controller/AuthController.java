package com.example.elder.web.controller;

import com.example.elder.service.auth.AuthDbService;
import com.example.elder.web.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthDbService authDbService;

    public AuthController(AuthDbService authDbService) {
        this.authDbService = authDbService;
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@RequestBody RegisterRequest req) {
        String desiredUserId = (req.userId() == null || req.userId().isBlank())
                ? authDbService.allocateNextSystemUserId(req.role())
                : req.userId();
        String userId = authDbService.register(new AuthDbService.AuthRegisterCommand(
                desiredUserId,
                req.role(),
                req.userName(),
                req.password(),
                req.gender(),
                req.age(),
                req.address(),
                req.linkedElderId(),
                req.linkedChildId(),
                req.keyHealthNotes(),
                req.relationDesc()
        ));
        return ApiResponse.ok(new RegisterResponse(userId));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest req) {
        Map<String, Object> user = authDbService.findUserByRoleAndName(req.role(), req.userName().trim())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        String password = String.valueOf(user.get("password"));
        if (!password.equals(req.password())) throw new IllegalArgumentException("密码错误");

        String role = String.valueOf(user.get("role_code"));
        String userId = String.valueOf(user.get("user_id"));
        String elderId = null;
        List<String> linkedElderIds = List.of();
        if ("ELDER".equals(role)) {
            elderId = userId;
            linkedElderIds = List.of(userId);
        } else if ("CHILD".equals(role)) {
            linkedElderIds = authDbService.listLinkedEldersForChild(userId);
            // 不再阻止登录；若为空，由前端登录后引导绑定
            elderId = linkedElderIds.isEmpty() ? null : linkedElderIds.get(0);
        }
        String address = Optional.ofNullable(user.get("address")).map(String::valueOf).orElse("");
        return ApiResponse.ok(new LoginResponse(role, userId, elderId, linkedElderIds, address));
    }

    @PostMapping("/bind/child-elders")
    public ApiResponse<Object> bindChildElders(@RequestBody BindChildEldersRequest req) {
        authDbService.bindEldersForChild(req.childUserId(), req.elderUserIds());
        return ApiResponse.ok(null);
    }

    @PostMapping("/bind/elder-child")
    public ApiResponse<Object> bindElderChild(@RequestBody BindElderChildRequest req) {
        authDbService.bindChildForElder(req.elderUserId(), req.childUserId());
        return ApiResponse.ok(null);
    }

    @GetMapping("/system-id")
    public ApiResponse<SystemIdResponse> lookupSystemId(
            @RequestParam("role") @NotBlank String role,
            @RequestParam("userName") @NotBlank String userName
    ) {
        String userId = authDbService.findSystemIdByRoleAndName(role, userName).orElse(null);
        return ApiResponse.ok(new SystemIdResponse(userId));
    }

    @GetMapping("/allocate-id")
    public ApiResponse<AllocateIdResponse> allocateSystemId(@RequestParam("role") @NotBlank String role) {
        String userId = authDbService.allocateNextSystemUserId(role);
        return ApiResponse.ok(new AllocateIdResponse(userId));
    }

    @GetMapping("/next-id")
    public ApiResponse<AllocateIdResponse> previewNextSystemId(@RequestParam("role") @NotBlank String role) {
        String userId = authDbService.previewNextSystemUserId(role);
        return ApiResponse.ok(new AllocateIdResponse(userId));
    }

    @GetMapping("/user-by-id")
    public ApiResponse<UserByIdResponse> getById(@RequestParam("userId") @NotBlank String userId) {
        var user = authDbService.findUserBySystemId(userId)
                .orElseThrow(() -> new IllegalArgumentException("未找到账号"));
        return ApiResponse.ok(new UserByIdResponse(
                String.valueOf(user.get("user_id")),
                String.valueOf(user.get("role_code")),
                String.valueOf(user.get("user_name"))
        ));
    }

    public record RegisterRequest(
            String userId,
            String role,
            String userName,
            String password,
            String gender,
            Integer age,
            String address,
            String linkedElderId,
            String linkedChildId,
            String keyHealthNotes,
            String relationDesc
    ) {
    }

    public record RegisterResponse(String userId) {
    }

    public record LoginRequest(String role, String userName, String password) {
    }

    public record LoginResponse(
            String role,
            String userId,
            String elderId,
            List<String> linkedElderIds,
            String address
    ) {
    }

    public record SystemIdResponse(String userId) {
    }

    public record UserByIdResponse(String userId, String role, String userName) {
    }

    public record AllocateIdResponse(String userId) {
    }

    public record BindChildEldersRequest(String childUserId, List<String> elderUserIds) {
    }

    public record BindElderChildRequest(String elderUserId, String childUserId) {
    }
}

