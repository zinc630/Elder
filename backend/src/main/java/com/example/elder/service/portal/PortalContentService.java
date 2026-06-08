package com.example.elder.service.portal;

import com.example.elder.domain.portal.PortalActivity;
import com.example.elder.domain.portal.PortalCourse;
import com.example.elder.domain.portal.PortalLifeService;
import com.example.elder.domain.portal.PortalNews;
import com.example.elder.domain.portal.PortalRegistration;
import com.example.elder.dto.portal.PortalActionResultDto;
import com.example.elder.dto.portal.PortalActivityDto;
import com.example.elder.dto.portal.PortalCourseDetailDto;
import com.example.elder.dto.portal.PortalCourseDto;
import com.example.elder.dto.portal.PortalEnrollRequest;
import com.example.elder.dto.portal.PortalLifeServiceDto;
import com.example.elder.dto.portal.PortalNewsDetailDto;
import com.example.elder.dto.portal.PortalNewsDto;
import com.example.elder.store.PortalContentStore;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class PortalContentService {
    private final PortalContentStore store;

    public PortalContentService(PortalContentStore store) {
        this.store = store;
    }

    public List<PortalActivityDto> listActivities(String actorKey) {
        return store.listActivities().stream().map(a -> toActivityDto(a, actorKey)).toList();
    }

    public PortalActivityDto getActivity(String id, String actorKey) {
        PortalActivity a = store.getActivity(id)
                .orElseThrow(() -> new IllegalArgumentException("活动不存在"));
        return toActivityDto(a, actorKey);
    }

    public PortalActionResultDto enrollActivity(String id, String actorKey, PortalEnrollRequest req) {
        PortalActivity a = store.getActivity(id)
                .orElseThrow(() -> new IllegalArgumentException("活动不存在"));
        if (!a.open()) {
            return new PortalActionResultDto(false, "活动已停止报名", null);
        }
        if (a.enrolledCount() >= a.capacity()) {
            return new PortalActionResultDto(false, "名额已满，请选择其他活动", null);
        }
        if (store.hasRegistration(actorKey, PortalRegistration.PortalRegistrationType.ACTIVITY, id)) {
            return new PortalActionResultDto(false, "您已报名该活动，请勿重复提交", null);
        }
        String regId = store.newRegistrationId();
        store.addRegistration(new PortalRegistration(
                regId,
                PortalRegistration.PortalRegistrationType.ACTIVITY,
                id,
                actorKey,
                req.contactName().trim(),
                req.contactPhone().trim(),
                req.note(),
                "CONFIRMED",
                Instant.now()));
        return new PortalActionResultDto(true, "报名成功！请按时参加「" + a.title() + "」", regId);
    }

    public List<PortalLifeServiceDto> listLifeServices(String actorKey) {
        return store.listLifeServices().stream().map(s -> toLifeDto(s, actorKey)).toList();
    }

    public PortalLifeServiceDto getLifeService(String id, String actorKey) {
        PortalLifeService s = store.getLifeService(id)
                .orElseThrow(() -> new IllegalArgumentException("服务不存在"));
        return toLifeDto(s, actorKey);
    }

    public PortalActionResultDto bookLifeService(String id, String actorKey, PortalEnrollRequest req) {
        PortalLifeService s = store.getLifeService(id)
                .orElseThrow(() -> new IllegalArgumentException("服务不存在"));
        if (store.hasRegistration(actorKey, PortalRegistration.PortalRegistrationType.LIFE, id)) {
            return new PortalActionResultDto(false, "您已有该服务的待处理预约", null);
        }
        String note = req.note();
        if (req.preferredTime() != null && !req.preferredTime().isBlank()) {
            note = (note == null ? "" : note + "；") + "期望时间：" + req.preferredTime().trim();
        }
        String regId = store.newRegistrationId();
        store.addRegistration(new PortalRegistration(
                regId,
                PortalRegistration.PortalRegistrationType.LIFE,
                id,
                actorKey,
                req.contactName().trim(),
                req.contactPhone().trim(),
                note,
                "PENDING",
                Instant.now()));
        return new PortalActionResultDto(
                true,
                "预约已提交！「" + s.title() + "」将在约 " + s.slaHours() + " 小时内安排专人联系您",
                regId);
    }

    public List<PortalCourseDto> listCourses(String actorKey) {
        return store.listCourses().stream().map(c -> toCourseDto(c, actorKey)).toList();
    }

    public PortalCourseDetailDto getCourseDetail(String id, String actorKey) {
        PortalCourse c = store.getCourse(id)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在"));
        store.incrementCourseViews(id);
        Set<String> done = store.completedLessons(actorKey, id);
        boolean enrolled = store.hasRegistration(actorKey, PortalRegistration.PortalRegistrationType.COURSE, id)
                || !done.isEmpty();
        int progress = progressPercent(c, done);
        List<PortalCourseDetailDto.PortalLessonDto> lessons = c.lessons().stream()
                .map(l -> new PortalCourseDetailDto.PortalLessonDto(
                        l.id(),
                        l.title(),
                        l.content(),
                        l.durationMinutes(),
                        l.videoUrl(),
                        done.contains(l.id())))
                .toList();
        return new PortalCourseDetailDto(
                c.id(),
                c.icon(),
                c.category(),
                c.title(),
                c.description(),
                c.duration(),
                formatViews(store.getCourseViews(id)),
                c.rating(),
                progress,
                enrolled,
                c.imageUrl(),
                lessons,
                List.copyOf(done));
    }

    public PortalActionResultDto enrollCourse(String id, String actorKey, PortalEnrollRequest req) {
        PortalCourse c = store.getCourse(id)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在"));
        if (store.hasRegistration(actorKey, PortalRegistration.PortalRegistrationType.COURSE, id)) {
            return new PortalActionResultDto(false, "您已加入该课程", null);
        }
        String regId = store.newRegistrationId();
        store.addRegistration(new PortalRegistration(
                regId,
                PortalRegistration.PortalRegistrationType.COURSE,
                id,
                actorKey,
                req.contactName().trim(),
                req.contactPhone().trim(),
                req.note(),
                "ENROLLED",
                Instant.now()));
        return new PortalActionResultDto(true, "已加入课程「" + c.title() + "」，可开始学习", regId);
    }

    public PortalActionResultDto completeLesson(String courseId, String actorKey, String lessonId) {
        PortalCourse c = store.getCourse(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在"));
        boolean lessonExists = c.lessons().stream().anyMatch(l -> l.id().equals(lessonId));
        if (!lessonExists) {
            return new PortalActionResultDto(false, "课时不存在", null);
        }
        if (!store.hasRegistration(actorKey, PortalRegistration.PortalRegistrationType.COURSE, courseId)) {
            store.addRegistration(new PortalRegistration(
                    store.newRegistrationId(),
                    PortalRegistration.PortalRegistrationType.COURSE,
                    courseId,
                    actorKey,
                    "自学",
                    "-",
                    null,
                    "ENROLLED",
                    Instant.now()));
        }
        store.completeLesson(actorKey, courseId, lessonId);
        Set<String> done = store.completedLessons(actorKey, courseId);
        int pct = progressPercent(c, done);
        String msg = pct >= 100 ? "恭喜完成全部课时！" : "课时已完成，当前进度 " + pct + "%";
        return new PortalActionResultDto(true, msg, lessonId);
    }

    public List<PortalNewsDto> listNews() {
        return store.listNews().stream().map(this::toNewsDto).toList();
    }

    public PortalNewsDetailDto getNewsDetail(String id) {
        PortalNews n = store.getNews(id)
                .orElseThrow(() -> new IllegalArgumentException("资讯不存在"));
        store.incrementNewsViews(id);
        return new PortalNewsDetailDto(
                n.id(),
                n.icon(),
                n.title(),
                n.summary(),
                n.publishDate(),
                n.source(),
                n.body(),
                formatViews(store.getNewsViews(id)),
                n.imageUrl());
    }

    private PortalActivityDto toActivityDto(PortalActivity a, String actorKey) {
        int remaining = Math.max(0, a.capacity() - a.enrolledCount());
        return new PortalActivityDto(
                a.id(),
                a.icon(),
                a.tag(),
                a.title(),
                a.timeLabel(),
                a.location(),
                a.description(),
                a.capacity(),
                a.enrolledCount(),
                remaining,
                a.open() && remaining > 0,
                store.hasRegistration(actorKey, PortalRegistration.PortalRegistrationType.ACTIVITY, a.id()),
                a.imageUrl());
    }

    private PortalLifeServiceDto toLifeDto(PortalLifeService s, String actorKey) {
        return new PortalLifeServiceDto(
                s.id(),
                s.icon(),
                s.title(),
                s.description(),
                s.features(),
                s.bgGradient(),
                s.priceHint(),
                s.slaHours(),
                store.hasRegistration(actorKey, PortalRegistration.PortalRegistrationType.LIFE, s.id()));
    }

    private PortalCourseDto toCourseDto(PortalCourse c, String actorKey) {
        Set<String> done = store.completedLessons(actorKey, c.id());
        boolean enrolled = store.hasRegistration(actorKey, PortalRegistration.PortalRegistrationType.COURSE, c.id())
                || !done.isEmpty();
        return new PortalCourseDto(
                c.id(),
                c.icon(),
                c.category(),
                c.title(),
                c.description(),
                c.duration(),
                formatViews(store.getCourseViews(c.id())),
                c.rating(),
                c.lessons().size(),
                progressPercent(c, done),
                enrolled,
                c.imageUrl());
    }

    private PortalNewsDto toNewsDto(PortalNews n) {
        return new PortalNewsDto(
                n.id(),
                n.icon(),
                n.title(),
                n.summary(),
                n.publishDate(),
                n.source(),
                formatViews(store.getNewsViews(n.id())),
                n.imageUrl());
    }

    private static int progressPercent(PortalCourse c, Set<String> done) {
        if (c.lessons().isEmpty()) return 0;
        return (int) Math.round(100.0 * done.size() / c.lessons().size());
    }

    private static String formatViews(int count) {
        if (count >= 10000) {
            double w = count / 10000.0;
            return (w % 1 == 0 ? String.valueOf((int) w) : String.format("%.1f", w)) + "万";
        }
        return String.valueOf(count);
    }
}
