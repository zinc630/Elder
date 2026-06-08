package com.example.elder.web.controller;

import com.example.elder.domain.Role;
import com.example.elder.dto.portal.admin.PortalActivityAdminDto;
import com.example.elder.dto.portal.admin.PortalActivitySaveRequest;
import com.example.elder.dto.portal.admin.PortalCourseAdminDto;
import com.example.elder.dto.portal.admin.PortalCourseSaveRequest;
import com.example.elder.dto.portal.admin.PortalMediaUploadDto;
import com.example.elder.dto.portal.admin.PortalNewsAdminDto;
import com.example.elder.dto.portal.admin.PortalNewsSaveRequest;
import com.example.elder.service.portal.PortalAdminContentService;
import com.example.elder.service.portal.PortalMediaStorageService;
import com.example.elder.web.ApiResponse;
import com.example.elder.web.ForbiddenException;
import com.example.elder.web.RoleResolver;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/home-content")
public class AdminHomeContentController {
    private final PortalAdminContentService adminContentService;
    private final PortalMediaStorageService mediaStorageService;

    public AdminHomeContentController(
            PortalAdminContentService adminContentService, PortalMediaStorageService mediaStorageService) {
        this.adminContentService = adminContentService;
        this.mediaStorageService = mediaStorageService;
    }

    @PostMapping("/seed-demo")
    public ApiResponse<String> seedDemo(@RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAdmin(roleHeader);
        adminContentService.reseedDemoContent();
        return ApiResponse.ok("已导入示例数据");
    }

    @PostMapping("/media/upload")
    public ApiResponse<PortalMediaUploadDto> uploadMedia(
            @RequestHeader(name = "X-Role", required = false) String roleHeader,
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "kind", defaultValue = "image") String kind)
            throws IOException {
        requireAdmin(roleHeader);
        return ApiResponse.ok(mediaStorageService.store(file, kind));
    }

    @GetMapping("/activities")
    public ApiResponse<List<PortalActivityAdminDto>> listActivities(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAdmin(roleHeader);
        return ApiResponse.ok(adminContentService.listActivities());
    }

    @GetMapping("/activities/{id}")
    public ApiResponse<PortalActivityAdminDto> getActivity(
            @RequestHeader(name = "X-Role", required = false) String roleHeader, @PathVariable("id") String id) {
        requireAdmin(roleHeader);
        return ApiResponse.ok(adminContentService.getActivity(id));
    }

    @PostMapping("/activities")
    public ApiResponse<PortalActivityAdminDto> createActivity(
            @RequestHeader(name = "X-Role", required = false) String roleHeader,
            @Valid @RequestBody PortalActivitySaveRequest req) {
        requireAdmin(roleHeader);
        return ApiResponse.ok(adminContentService.saveActivity(req, true));
    }

    @PutMapping("/activities/{id}")
    public ApiResponse<PortalActivityAdminDto> updateActivity(
            @RequestHeader(name = "X-Role", required = false) String roleHeader,
            @PathVariable("id") String id,
            @Valid @RequestBody PortalActivitySaveRequest req) {
        requireAdmin(roleHeader);
        return ApiResponse.ok(adminContentService.saveActivity(
                new PortalActivitySaveRequest(
                        id,
                        req.icon(),
                        req.tag(),
                        req.title(),
                        req.timeLabel(),
                        req.location(),
                        req.description(),
                        req.capacity(),
                        req.open(),
                        req.imageUrl()),
                false));
    }

    @DeleteMapping("/activities/{id}")
    public ApiResponse<Void> deleteActivity(
            @RequestHeader(name = "X-Role", required = false) String roleHeader, @PathVariable("id") String id) {
        requireAdmin(roleHeader);
        adminContentService.deleteActivity(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/courses")
    public ApiResponse<List<PortalCourseAdminDto>> listCourses(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAdmin(roleHeader);
        return ApiResponse.ok(adminContentService.listCourses());
    }

    @GetMapping("/courses/{id}")
    public ApiResponse<PortalCourseAdminDto> getCourse(
            @RequestHeader(name = "X-Role", required = false) String roleHeader, @PathVariable("id") String id) {
        requireAdmin(roleHeader);
        return ApiResponse.ok(adminContentService.getCourse(id));
    }

    @PostMapping("/courses")
    public ApiResponse<PortalCourseAdminDto> createCourse(
            @RequestHeader(name = "X-Role", required = false) String roleHeader,
            @Valid @RequestBody PortalCourseSaveRequest req) {
        requireAdmin(roleHeader);
        return ApiResponse.ok(adminContentService.saveCourse(req, true));
    }

    @PutMapping("/courses/{id}")
    public ApiResponse<PortalCourseAdminDto> updateCourse(
            @RequestHeader(name = "X-Role", required = false) String roleHeader,
            @PathVariable("id") String id,
            @Valid @RequestBody PortalCourseSaveRequest req) {
        requireAdmin(roleHeader);
        return ApiResponse.ok(adminContentService.saveCourse(
                new PortalCourseSaveRequest(
                        id,
                        req.icon(),
                        req.category(),
                        req.title(),
                        req.description(),
                        req.duration(),
                        req.rating(),
                        req.lessons(),
                        req.imageUrl()),
                false));
    }

    @DeleteMapping("/courses/{id}")
    public ApiResponse<Void> deleteCourse(
            @RequestHeader(name = "X-Role", required = false) String roleHeader, @PathVariable("id") String id) {
        requireAdmin(roleHeader);
        adminContentService.deleteCourse(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/news")
    public ApiResponse<List<PortalNewsAdminDto>> listNews(
            @RequestHeader(name = "X-Role", required = false) String roleHeader) {
        requireAdmin(roleHeader);
        return ApiResponse.ok(adminContentService.listNews());
    }

    @GetMapping("/news/{id}")
    public ApiResponse<PortalNewsAdminDto> getNews(
            @RequestHeader(name = "X-Role", required = false) String roleHeader, @PathVariable("id") String id) {
        requireAdmin(roleHeader);
        return ApiResponse.ok(adminContentService.getNews(id));
    }

    @PostMapping("/news")
    public ApiResponse<PortalNewsAdminDto> createNews(
            @RequestHeader(name = "X-Role", required = false) String roleHeader,
            @Valid @RequestBody PortalNewsSaveRequest req) {
        requireAdmin(roleHeader);
        return ApiResponse.ok(adminContentService.saveNews(req, true));
    }

    @PutMapping("/news/{id}")
    public ApiResponse<PortalNewsAdminDto> updateNews(
            @RequestHeader(name = "X-Role", required = false) String roleHeader,
            @PathVariable("id") String id,
            @Valid @RequestBody PortalNewsSaveRequest req) {
        requireAdmin(roleHeader);
        return ApiResponse.ok(adminContentService.saveNews(
                new PortalNewsSaveRequest(
                        id,
                        req.icon(),
                        req.title(),
                        req.summary(),
                        req.publishDate(),
                        req.source(),
                        req.body(),
                        req.imageUrl()),
                false));
    }

    @DeleteMapping("/news/{id}")
    public ApiResponse<Void> deleteNews(
            @RequestHeader(name = "X-Role", required = false) String roleHeader, @PathVariable("id") String id) {
        requireAdmin(roleHeader);
        adminContentService.deleteNews(id);
        return ApiResponse.ok(null);
    }

    private void requireAdmin(String roleHeader) {
        Role role = RoleResolver.resolve(roleHeader);
        if (role != Role.ADMIN) throw new ForbiddenException("admin_only");
    }
}
