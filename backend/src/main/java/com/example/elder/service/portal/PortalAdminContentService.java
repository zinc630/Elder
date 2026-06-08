package com.example.elder.service.portal;

import com.example.elder.domain.portal.PortalActivity;
import com.example.elder.domain.portal.PortalCourse;
import com.example.elder.domain.portal.PortalNews;
import com.example.elder.dto.portal.admin.PortalActivityAdminDto;
import com.example.elder.dto.portal.admin.PortalActivitySaveRequest;
import com.example.elder.dto.portal.admin.PortalCourseAdminDto;
import com.example.elder.dto.portal.admin.PortalCourseSaveRequest;
import com.example.elder.dto.portal.admin.PortalLessonAdminDto;
import com.example.elder.dto.portal.admin.PortalNewsAdminDto;
import com.example.elder.dto.portal.admin.PortalNewsSaveRequest;
import com.example.elder.store.PortalContentStore;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PortalAdminContentService {
    private final PortalContentStore store;

    public PortalAdminContentService(PortalContentStore store) {
        this.store = store;
    }

    public List<PortalActivityAdminDto> listActivities() {
        return store.listActivities().stream().map(this::toActivityAdmin).toList();
    }

    public PortalActivityAdminDto getActivity(String id) {
        return store.getActivity(id)
                .map(this::toActivityAdmin)
                .orElseThrow(() -> new IllegalArgumentException("活动不存在"));
    }

    public PortalActivityAdminDto saveActivity(PortalActivitySaveRequest req, boolean create) {
        String id = resolveId(req.id(), create, store::getActivity, store::newActivityId);
        int enrolled = store.getActivity(id).map(PortalActivity::enrolledCount).orElse(0);
        PortalActivity saved = store.saveActivity(new PortalActivity(
                id,
                req.icon().trim(),
                req.tag().trim(),
                req.title().trim(),
                req.timeLabel().trim(),
                req.location().trim(),
                req.description().trim(),
                req.capacity(),
                enrolled,
                req.open(),
                blankToNull(req.imageUrl())));
        return toActivityAdmin(saved);
    }

    public void deleteActivity(String id) {
        if (!store.deleteActivity(id)) {
            throw new IllegalArgumentException("活动不存在");
        }
    }

    public List<PortalCourseAdminDto> listCourses() {
        return store.listCourses().stream().map(this::toCourseAdmin).toList();
    }

    public PortalCourseAdminDto getCourse(String id) {
        return store.getCourse(id)
                .map(this::toCourseAdmin)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在"));
    }

    public PortalCourseAdminDto saveCourse(PortalCourseSaveRequest req, boolean create) {
        String id = resolveId(req.id(), create, store::getCourse, store::newCourseId);
        int views = store.getCourseViews(id);
        if (views <= 0) {
            views = 1000;
        }
        List<PortalCourse.PortalLesson> lessons = new ArrayList<>();
        for (PortalLessonAdminDto l : req.lessons()) {
            String lessonId = l.id() == null || l.id().isBlank() ? store.newLessonId() : l.id().trim();
            lessons.add(new PortalCourse.PortalLesson(
                    lessonId,
                    l.title().trim(),
                    l.content().trim(),
                    l.durationMinutes(),
                    blankToNull(l.videoUrl())));
        }
        PortalCourse saved = store.saveCourse(new PortalCourse(
                id,
                req.icon().trim(),
                req.category().trim(),
                req.title().trim(),
                req.description().trim(),
                req.duration().trim(),
                views,
                req.rating(),
                lessons,
                blankToNull(req.imageUrl())));
        return toCourseAdmin(saved);
    }

    public void deleteCourse(String id) {
        if (!store.deleteCourse(id)) {
            throw new IllegalArgumentException("课程不存在");
        }
    }

    public List<PortalNewsAdminDto> listNews() {
        return store.listNews().stream().map(this::toNewsAdmin).toList();
    }

    public PortalNewsAdminDto getNews(String id) {
        return store.getNews(id)
                .map(this::toNewsAdmin)
                .orElseThrow(() -> new IllegalArgumentException("资讯不存在"));
    }

    public PortalNewsAdminDto saveNews(PortalNewsSaveRequest req, boolean create) {
        String id = resolveId(req.id(), create, store::getNews, store::newNewsId);
        int views = store.getNewsViews(id);
        if (views <= 0) {
            views = 100;
        }
        PortalNews saved = store.saveNews(new PortalNews(
                id,
                req.icon().trim(),
                req.title().trim(),
                req.summary().trim(),
                req.publishDate().trim(),
                req.source().trim(),
                req.body().trim(),
                views,
                blankToNull(req.imageUrl())));
        return toNewsAdmin(saved);
    }

    public void deleteNews(String id) {
        if (!store.deleteNews(id)) {
            throw new IllegalArgumentException("资讯不存在");
        }
    }

    public void reseedDemoContent() {
        store.reseedDefaults();
    }

    private PortalActivityAdminDto toActivityAdmin(PortalActivity a) {
        return new PortalActivityAdminDto(
                a.id(),
                a.icon(),
                a.tag(),
                a.title(),
                a.timeLabel(),
                a.location(),
                a.description(),
                a.capacity(),
                a.enrolledCount(),
                a.open(),
                a.imageUrl());
    }

    private PortalCourseAdminDto toCourseAdmin(PortalCourse c) {
        List<PortalLessonAdminDto> lessons = c.lessons().stream()
                .map(l -> new PortalLessonAdminDto(
                        l.id(), l.title(), l.content(), l.durationMinutes(), l.videoUrl()))
                .toList();
        return new PortalCourseAdminDto(
                c.id(),
                c.icon(),
                c.category(),
                c.title(),
                c.description(),
                c.duration(),
                store.getCourseViews(c.id()),
                c.rating(),
                lessons,
                c.imageUrl());
    }

    private PortalNewsAdminDto toNewsAdmin(PortalNews n) {
        return new PortalNewsAdminDto(
                n.id(),
                n.icon(),
                n.title(),
                n.summary(),
                n.publishDate(),
                n.source(),
                n.body(),
                store.getNewsViews(n.id()),
                n.imageUrl());
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) return null;
        return s.trim();
    }

    @FunctionalInterface
    private interface IdFactory {
        String newId();
    }

    @FunctionalInterface
    private interface ExistsCheck {
        java.util.Optional<?> get(String id);
    }

    private String resolveId(String reqId, boolean create, ExistsCheck exists, IdFactory factory) {
        if (create) {
            return factory.newId();
        }
        if (reqId == null || reqId.isBlank()) {
            throw new IllegalArgumentException("缺少内容 ID");
        }
        if (exists.get(reqId.trim()).isEmpty()) {
            throw new IllegalArgumentException("内容不存在");
        }
        return reqId.trim();
    }

    public static String todayDate() {
        return LocalDate.now().toString();
    }
}
