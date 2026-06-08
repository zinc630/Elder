package com.example.elder.service.portal;

import com.example.elder.domain.portal.PortalActivity;
import com.example.elder.domain.portal.PortalCourse;
import com.example.elder.domain.portal.PortalNews;
import com.example.elder.domain.portal.PortalRegistration;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnBean(JdbcTemplate.class)
public class PortalContentDbService {

    private final JdbcTemplate jdbc;

    public PortalContentDbService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean hasAnyContent() {
        Integer n = jdbc.queryForObject(
                """
                SELECT (
                  (SELECT COUNT(*) FROM portal_activity) +
                  (SELECT COUNT(*) FROM portal_course) +
                  (SELECT COUNT(*) FROM portal_news)
                )
                """,
                Integer.class);
        return n != null && n > 0;
    }

    public List<PortalActivity> findAllActivities() {
        return jdbc.query(
                """
                SELECT id, icon, tag, title, time_label, location, description,
                       capacity, enrolled_count, open_flag, image_url
                FROM portal_activity
                ORDER BY sort_order ASC, id ASC
                """,
                activityMapper());
    }

    public Optional<PortalActivity> findActivity(String id) {
        var rows = jdbc.query(
                """
                SELECT id, icon, tag, title, time_label, location, description,
                       capacity, enrolled_count, open_flag, image_url
                FROM portal_activity WHERE id = ?
                """,
                activityMapper(),
                id);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public void upsertActivity(PortalActivity a) {
        jdbc.update(
                """
                INSERT INTO portal_activity (
                  id, icon, tag, title, time_label, location, description,
                  capacity, enrolled_count, open_flag, image_url, sort_order
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                ON DUPLICATE KEY UPDATE
                  icon = VALUES(icon), tag = VALUES(tag), title = VALUES(title),
                  time_label = VALUES(time_label), location = VALUES(location),
                  description = VALUES(description), capacity = VALUES(capacity),
                  enrolled_count = VALUES(enrolled_count), open_flag = VALUES(open_flag),
                  image_url = VALUES(image_url)
                """,
                a.id(),
                a.icon(),
                a.tag(),
                a.title(),
                a.timeLabel(),
                a.location(),
                a.description(),
                a.capacity(),
                a.enrolledCount(),
                a.open() ? 1 : 0,
                a.imageUrl());
    }

    public void deleteActivity(String id) {
        jdbc.update("DELETE FROM portal_activity WHERE id = ?", id);
    }

    public void incrementActivityEnrolled(String id) {
        jdbc.update(
                """
                UPDATE portal_activity
                SET enrolled_count = LEAST(enrolled_count + 1, capacity)
                WHERE id = ?
                """,
                id);
    }

    public List<PortalNews> findAllNews() {
        return jdbc.query(
                """
                SELECT id, icon, title, summary, publish_date, source, body, view_count, image_url
                FROM portal_news
                ORDER BY sort_order ASC, publish_date DESC, id ASC
                """,
                newsMapper());
    }

    public Optional<PortalNews> findNews(String id) {
        var rows = jdbc.query(
                """
                SELECT id, icon, title, summary, publish_date, source, body, view_count, image_url
                FROM portal_news WHERE id = ?
                """,
                newsMapper(),
                id);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public void upsertNews(PortalNews n) {
        jdbc.update(
                """
                INSERT INTO portal_news (
                  id, icon, title, summary, publish_date, source, body, view_count, image_url, sort_order
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                ON DUPLICATE KEY UPDATE
                  icon = VALUES(icon), title = VALUES(title), summary = VALUES(summary),
                  publish_date = VALUES(publish_date), source = VALUES(source), body = VALUES(body),
                  view_count = VALUES(view_count), image_url = VALUES(image_url)
                """,
                n.id(),
                n.icon(),
                n.title(),
                n.summary(),
                n.publishDate(),
                n.source(),
                n.body(),
                n.viewCount(),
                n.imageUrl());
    }

    public void deleteNews(String id) {
        jdbc.update("DELETE FROM portal_news WHERE id = ?", id);
    }

    public int getNewsViews(String id) {
        Integer v = jdbc.queryForObject("SELECT view_count FROM portal_news WHERE id = ?", Integer.class, id);
        return v == null ? 0 : v;
    }

    public void incrementNewsViews(String id) {
        jdbc.update("UPDATE portal_news SET view_count = view_count + 1 WHERE id = ?", id);
    }

    public List<PortalCourse> findAllCourses() {
        List<PortalCourse> bases = jdbc.query(
                """
                SELECT id, icon, category, title, description, duration_label, view_count, rating, image_url
                FROM portal_course
                ORDER BY sort_order ASC, id ASC
                """,
                courseMapper());
        if (bases.isEmpty()) return bases;
        Map<String, List<PortalCourse.PortalLesson>> lessonsByCourse = loadLessonsGrouped();
        List<PortalCourse> result = new ArrayList<>();
        for (PortalCourse c : bases) {
            result.add(new PortalCourse(
                    c.id(),
                    c.icon(),
                    c.category(),
                    c.title(),
                    c.description(),
                    c.duration(),
                    c.viewCount(),
                    c.rating(),
                    lessonsByCourse.getOrDefault(c.id(), List.of()),
                    c.imageUrl()));
        }
        return result;
    }

    public Optional<PortalCourse> findCourse(String id) {
        var rows = jdbc.query(
                """
                SELECT id, icon, category, title, description, duration_label, view_count, rating, image_url
                FROM portal_course WHERE id = ?
                """,
                courseMapper(),
                id);
        if (rows.isEmpty()) return Optional.empty();
        PortalCourse base = rows.get(0);
        return Optional.of(new PortalCourse(
                base.id(),
                base.icon(),
                base.category(),
                base.title(),
                base.description(),
                base.duration(),
                base.viewCount(),
                base.rating(),
                findLessons(id),
                base.imageUrl()));
    }

    @Transactional
    public void upsertCourse(PortalCourse c) {
        jdbc.update(
                """
                INSERT INTO portal_course (
                  id, icon, category, title, description, duration_label, view_count, rating, image_url, sort_order
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                ON DUPLICATE KEY UPDATE
                  icon = VALUES(icon), category = VALUES(category), title = VALUES(title),
                  description = VALUES(description), duration_label = VALUES(duration_label),
                  view_count = VALUES(view_count), rating = VALUES(rating), image_url = VALUES(image_url)
                """,
                c.id(),
                c.icon(),
                c.category(),
                c.title(),
                c.description(),
                c.duration(),
                c.viewCount(),
                c.rating(),
                c.imageUrl());
        jdbc.update("DELETE FROM portal_course_lesson WHERE course_id = ?", c.id());
        int order = 0;
        for (PortalCourse.PortalLesson l : c.lessons()) {
            jdbc.update(
                    """
                    INSERT INTO portal_course_lesson (
                      id, course_id, title, content, duration_minutes, video_url, sort_order
                    ) VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    l.id(),
                    c.id(),
                    l.title(),
                    l.content(),
                    l.durationMinutes(),
                    l.videoUrl(),
                    order++);
        }
    }

    public void deleteCourse(String id) {
        jdbc.update("DELETE FROM portal_course WHERE id = ?", id);
    }

    public int getCourseViews(String id) {
        Integer v = jdbc.queryForObject("SELECT view_count FROM portal_course WHERE id = ?", Integer.class, id);
        return v == null ? 0 : v;
    }

    public void incrementCourseViews(String id) {
        jdbc.update("UPDATE portal_course SET view_count = view_count + 1 WHERE id = ?", id);
    }

    public void addRegistration(PortalRegistration reg) {
        jdbc.update(
                """
                INSERT INTO portal_registration (
                  id, reg_type, target_id, user_id, contact_name, contact_phone, note, status, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                reg.id(),
                reg.type().name(),
                reg.targetId(),
                reg.userId(),
                reg.contactName(),
                reg.contactPhone(),
                reg.note(),
                reg.status(),
                Timestamp.from(reg.createdAt()));
    }

    public boolean hasRegistration(String actorKey, PortalRegistration.PortalRegistrationType type, String targetId) {
        Integer n = jdbc.queryForObject(
                """
                SELECT COUNT(*) FROM portal_registration
                WHERE user_id = ? AND reg_type = ? AND target_id = ? AND status <> 'CANCELLED'
                """,
                Integer.class,
                actorKey,
                type.name(),
                targetId);
        return n != null && n > 0;
    }

    public Set<String> completedLessons(String actorKey, String courseId) {
        List<String> ids = jdbc.query(
                """
                SELECT lesson_id FROM portal_lesson_progress
                WHERE actor_key = ? AND course_id = ?
                """,
                (rs, rowNum) -> rs.getString("lesson_id"),
                actorKey,
                courseId);
        return new LinkedHashSet<>(ids);
    }

    public void completeLesson(String actorKey, String courseId, String lessonId) {
        jdbc.update(
                """
                INSERT IGNORE INTO portal_lesson_progress (actor_key, course_id, lesson_id)
                VALUES (?, ?, ?)
                """,
                actorKey,
                courseId,
                lessonId);
    }

    @Transactional
    public void replaceAll(
            List<PortalActivity> activities, List<PortalCourse> courses, List<PortalNews> newsList) {
        for (PortalActivity a : activities) {
            upsertActivity(a);
        }
        for (PortalNews n : newsList) {
            upsertNews(n);
        }
        for (PortalCourse c : courses) {
            upsertCourse(c);
        }
    }

    private Map<String, List<PortalCourse.PortalLesson>> loadLessonsGrouped() {
        record LessonRow(String courseId, PortalCourse.PortalLesson lesson) {}
        List<LessonRow> rows = jdbc.query(
                """
                SELECT id, course_id, title, content, duration_minutes, video_url
                FROM portal_course_lesson
                ORDER BY course_id ASC, sort_order ASC, id ASC
                """,
                (rs, rowNum) -> new LessonRow(
                        rs.getString("course_id"),
                        new PortalCourse.PortalLesson(
                                rs.getString("id"),
                                rs.getString("title"),
                                rs.getString("content"),
                                rs.getInt("duration_minutes"),
                                rs.getString("video_url"))));
        Map<String, List<PortalCourse.PortalLesson>> map = new LinkedHashMap<>();
        for (LessonRow row : rows) {
            map.computeIfAbsent(row.courseId(), k -> new ArrayList<>()).add(row.lesson());
        }
        return map;
    }

    private List<PortalCourse.PortalLesson> findLessons(String courseId) {
        return jdbc.query(
                """
                SELECT id, course_id, title, content, duration_minutes, video_url
                FROM portal_course_lesson
                WHERE course_id = ?
                ORDER BY sort_order ASC, id ASC
                """,
                lessonMapper(),
                courseId);
    }

    private static RowMapper<PortalActivity> activityMapper() {
        return (rs, rowNum) -> new PortalActivity(
                rs.getString("id"),
                rs.getString("icon"),
                rs.getString("tag"),
                rs.getString("title"),
                rs.getString("time_label"),
                rs.getString("location"),
                rs.getString("description"),
                rs.getInt("capacity"),
                rs.getInt("enrolled_count"),
                rs.getInt("open_flag") == 1,
                rs.getString("image_url"));
    }

    private static RowMapper<PortalNews> newsMapper() {
        return (rs, rowNum) -> new PortalNews(
                rs.getString("id"),
                rs.getString("icon"),
                rs.getString("title"),
                rs.getString("summary"),
                rs.getString("publish_date"),
                rs.getString("source"),
                rs.getString("body"),
                rs.getInt("view_count"),
                rs.getString("image_url"));
    }

    private static RowMapper<PortalCourse> courseMapper() {
        return (rs, rowNum) -> new PortalCourse(
                rs.getString("id"),
                rs.getString("icon"),
                rs.getString("category"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("duration_label"),
                rs.getInt("view_count"),
                rs.getDouble("rating"),
                List.of(),
                rs.getString("image_url"));
    }

    private static RowMapper<PortalCourse.PortalLesson> lessonMapper() {
        return (rs, rowNum) -> new PortalCourse.PortalLesson(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getInt("duration_minutes"),
                rs.getString("video_url"));
    }
}
