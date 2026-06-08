package com.example.elder.web.controller;

import com.example.elder.dto.portal.PortalActionResultDto;
import com.example.elder.dto.portal.PortalActivityDto;
import com.example.elder.dto.portal.PortalCourseDetailDto;
import com.example.elder.dto.portal.PortalCourseDto;
import com.example.elder.dto.portal.PortalCourseProgressRequest;
import com.example.elder.dto.portal.PortalEnrollRequest;
import com.example.elder.dto.portal.PortalLifeServiceDto;
import com.example.elder.dto.portal.PortalNewsDetailDto;
import com.example.elder.dto.portal.PortalNewsDto;
import com.example.elder.service.portal.PortalContentService;
import com.example.elder.web.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/portal")
public class PublicPortalController {
    private final PortalContentService portalContentService;

    public PublicPortalController(PortalContentService portalContentService) {
        this.portalContentService = portalContentService;
    }

    @GetMapping("/activities")
    public ApiResponse<List<PortalActivityDto>> listActivities(
            @RequestHeader(name = "X-Portal-Actor", required = false) String actor) {
        return ApiResponse.ok(portalContentService.listActivities(resolveActor(actor)));
    }

    @GetMapping("/activities/{id}")
    public ApiResponse<PortalActivityDto> getActivity(
            @PathVariable("id") String id,
            @RequestHeader(name = "X-Portal-Actor", required = false) String actor) {
        return ApiResponse.ok(portalContentService.getActivity(id, resolveActor(actor)));
    }

    @PostMapping("/activities/{id}/enroll")
    public ApiResponse<PortalActionResultDto> enrollActivity(
            @PathVariable("id") String id,
            @RequestHeader(name = "X-Portal-Actor", required = false) String actor,
            @Valid @RequestBody PortalEnrollRequest req) {
        return ApiResponse.ok(portalContentService.enrollActivity(id, resolveActor(actor), req));
    }

    @GetMapping("/life-services")
    public ApiResponse<List<PortalLifeServiceDto>> listLifeServices(
            @RequestHeader(name = "X-Portal-Actor", required = false) String actor) {
        return ApiResponse.ok(portalContentService.listLifeServices(resolveActor(actor)));
    }

    @GetMapping("/life-services/{id}")
    public ApiResponse<PortalLifeServiceDto> getLifeService(
            @PathVariable("id") String id,
            @RequestHeader(name = "X-Portal-Actor", required = false) String actor) {
        return ApiResponse.ok(portalContentService.getLifeService(id, resolveActor(actor)));
    }

    @PostMapping("/life-services/{id}/book")
    public ApiResponse<PortalActionResultDto> bookLifeService(
            @PathVariable("id") String id,
            @RequestHeader(name = "X-Portal-Actor", required = false) String actor,
            @Valid @RequestBody PortalEnrollRequest req) {
        return ApiResponse.ok(portalContentService.bookLifeService(id, resolveActor(actor), req));
    }

    @GetMapping("/courses")
    public ApiResponse<List<PortalCourseDto>> listCourses(
            @RequestHeader(name = "X-Portal-Actor", required = false) String actor) {
        return ApiResponse.ok(portalContentService.listCourses(resolveActor(actor)));
    }

    @GetMapping("/courses/{id}")
    public ApiResponse<PortalCourseDetailDto> getCourse(
            @PathVariable("id") String id,
            @RequestHeader(name = "X-Portal-Actor", required = false) String actor) {
        return ApiResponse.ok(portalContentService.getCourseDetail(id, resolveActor(actor)));
    }

    @PostMapping("/courses/{id}/enroll")
    public ApiResponse<PortalActionResultDto> enrollCourse(
            @PathVariable("id") String id,
            @RequestHeader(name = "X-Portal-Actor", required = false) String actor,
            @Valid @RequestBody PortalEnrollRequest req) {
        return ApiResponse.ok(portalContentService.enrollCourse(id, resolveActor(actor), req));
    }

    @PostMapping("/courses/{id}/lessons/complete")
    public ApiResponse<PortalActionResultDto> completeLesson(
            @PathVariable("id") String id,
            @RequestHeader(name = "X-Portal-Actor", required = false) String actor,
            @Valid @RequestBody PortalCourseProgressRequest req) {
        return ApiResponse.ok(portalContentService.completeLesson(id, resolveActor(actor), req.lessonId()));
    }

    @GetMapping("/news")
    public ApiResponse<List<PortalNewsDto>> listNews() {
        return ApiResponse.ok(portalContentService.listNews());
    }

    @GetMapping("/news/{id}")
    public ApiResponse<PortalNewsDetailDto> getNews(@PathVariable("id") String id) {
        return ApiResponse.ok(portalContentService.getNewsDetail(id));
    }

    private static String resolveActor(String header) {
        if (header != null && !header.isBlank()) {
            return header.trim();
        }
        return "guest-anonymous";
    }
}
