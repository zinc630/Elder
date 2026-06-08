package com.example.elder.store;

import com.example.elder.domain.portal.PortalActivity;
import com.example.elder.domain.portal.PortalCourse;
import com.example.elder.domain.portal.PortalLifeService;
import com.example.elder.domain.portal.PortalNews;
import com.example.elder.domain.portal.PortalRegistration;
import com.example.elder.service.portal.PortalContentDbService;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class PortalContentStore {
    private final PortalContentPersistence persistence;
    private final Optional<PortalContentDbService> db;
    private static final String V1 =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
    private static final String V2 =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4";
    private static final String V3 =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4";
    private static final String V4 =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4";
    private static final String V5 =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4";
    private static final String V6 =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4";

    private final Map<String, PortalActivity> activities = new ConcurrentHashMap<>();
    private final Map<String, PortalLifeService> lifeServices = new ConcurrentHashMap<>();
    private final Map<String, PortalCourse> courses = new ConcurrentHashMap<>();
    private final Map<String, PortalNews> news = new ConcurrentHashMap<>();
    private final Map<String, PortalRegistration> registrations = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> courseCompletedLessons = new ConcurrentHashMap<>();
    private final Map<String, Integer> newsViewCounts = new ConcurrentHashMap<>();
    private final Map<String, Integer> courseViewCounts = new ConcurrentHashMap<>();

    public PortalContentStore(
            PortalContentPersistence persistence, ObjectProvider<PortalContentDbService> dbProvider) {
        this.persistence = persistence;
        this.db = Optional.ofNullable(dbProvider.getIfAvailable());
    }

    @PostConstruct
    public void seed() {
        seedActivities();
        seedLifeServices();
        seedCourses();
        seedNews();
        if (db.isPresent()) {
            PortalContentDbService database = db.get();
            if (database.hasAnyContent()) {
                reloadFromDb();
            } else {
                PortalContentSnapshot json = persistence.loadIfPresent();
                if (json != null) {
                    applySnapshot(json);
                }
                database.replaceAll(listActivities(), listCourses(), listNews());
                reloadFromDb();
            }
        } else {
            PortalContentSnapshot saved = persistence.loadIfPresent();
            if (saved != null) {
                applySnapshot(saved);
            }
        }
    }

    private void reloadFromDb() {
        if (db.isEmpty()) return;
        PortalContentDbService database = db.get();
        activities.clear();
        database.findAllActivities().forEach(this::putActivity);
        news.clear();
        newsViewCounts.clear();
        for (PortalNews n : database.findAllNews()) {
            putNews(n);
            newsViewCounts.put(n.id(), n.viewCount());
        }
        courses.clear();
        courseViewCounts.clear();
        for (PortalCourse c : database.findAllCourses()) {
            putCourse(c);
            courseViewCounts.put(c.id(), c.viewCount());
        }
    }

    private void applySnapshot(PortalContentSnapshot snapshot) {
        if (snapshot.activities() != null && !snapshot.activities().isEmpty()) {
            activities.clear();
            snapshot.activities().forEach(this::putActivity);
        }
        if (snapshot.courses() != null && !snapshot.courses().isEmpty()) {
            courses.clear();
            snapshot.courses().forEach(this::putCourse);
        }
        if (snapshot.news() != null && !snapshot.news().isEmpty()) {
            news.clear();
            snapshot.news().forEach(this::putNews);
        }
    }

    public synchronized void persistEditableContent() {
        if (db.isPresent()) {
            db.get().replaceAll(listActivities(), listCourses(), listNews());
        } else {
            persistence.save(new PortalContentSnapshot(
                    listActivities(), listCourses(), listNews()));
        }
    }

    private void persistActivity(PortalActivity activity) {
        if (db.isPresent()) {
            db.get().upsertActivity(activity);
        } else {
            persistEditableContent();
        }
    }

    private void persistNews(PortalNews item) {
        if (db.isPresent()) {
            db.get().upsertNews(item);
        } else {
            persistEditableContent();
        }
    }

    private void persistCourse(PortalCourse course) {
        if (db.isPresent()) {
            db.get().upsertCourse(course);
        } else {
            persistEditableContent();
        }
    }

    private void seedActivities() {
        putActivity(new PortalActivity(
                "act-001", "🎭", "节目表演", "京剧专场《贵妃醉酒》",
                "2024.12.25 14:00", "多功能厅",
                "特邀市京剧团带来经典剧目表演，含茶歇与交流环节。建议提前 15 分钟入场。",
                80, 12, true, null));
        putActivity(new PortalActivity(
                "act-002", "🎇", "节日庆典", "元旦联欢晚会",
                "2024.12.24 18:00", "一楼大厅",
                "社工派发礼物，合唱团献唱，长者及家属均可参加。",
                120, 45, true, null));
        putActivity(new PortalActivity(
                "act-003", "🧘", "运动健身", "太极拳晨练班",
                "每日 07:00", "户外花园",
                "专业教练指导，适合初学者，请穿舒适运动鞋。",
                30, 18, true, null));
    }

    private void seedLifeServices() {
        putLife(new PortalLifeService(
                "life-001", "🛒", "代购跑腿",
                "日常生活用品代购，专人专送，支持超市清单拍照上传。",
                List.of("超市代购", "药品代取", "日用品配送"),
                "linear-gradient(135deg, #eff6ff, #dbeafe)",
                "按单计费 · 起步 ¥15", 4));
        putLife(new PortalLifeService(
                "life-002", "💳", "缴费代办",
                "水电煤话费一键代缴，支持账单核对与电子回执。",
                List.of("水电缴费", "话费充值", "物业代缴"),
                "linear-gradient(135deg, #f0fdf4, #dcfce7)",
                "免服务费（演示）", 2));
        putLife(new PortalLifeService(
                "life-003", "🧹", "家政保洁",
                "专业保洁团队上门，工具消杀达标，可预约定期服务。",
                List.of("日常打扫", "深度保洁", "衣物清洗"),
                "linear-gradient(135deg, #fef3c7, #fef9c3)",
                "¥88 起 / 次", 24));
        putLife(new PortalLifeService(
                "life-004", "🍱", "助餐配送",
                "营养餐食定时配送，支持低糖低盐等特殊饮食标注。",
                List.of("营养套餐", "特殊饮食", "节日加餐"),
                "linear-gradient(135deg, #fce7f3, #fbcfe8)",
                "套餐 ¥25–45", 1));
    }

    private void seedCourses() {
        putCourse(new PortalCourse(
                "course-001", "🛡️", "反诈科普", "老年人防诈骗指南",
                "识别常见诈骗手段，保护养老金与个人信息安全。",
                "25分钟", 23000, 4.9,
                List.of(
                        new PortalCourse.PortalLesson(
                                "l1", "诈骗类型概览", "电信诈骗、冒充公检法、投资理财骗局等分类说明。", 8, V1),
                        new PortalCourse.PortalLesson(
                                "l2", "识别与应对", "牢记「不轻信、不转账、先核实」三原则，遇疑立即联系家属或机构。", 10, V2),
                        new PortalCourse.PortalLesson(
                                "l3", "案例演练", "通过真实案例复盘，练习挂断与报警流程。", 7, V3)
                ),
                null));
        putCourse(new PortalCourse(
                "course-002", "❤️", "健康讲座", "高血压的日常管理",
                "专业医师讲解血压监测、用药与饮食注意事项。",
                "40分钟", 18000, 4.8,
                List.of(
                        new PortalCourse.PortalLesson(
                                "l1", "认识高血压", "病因、分级与长期风险，为何需要持续管理。", 12, V2),
                        new PortalCourse.PortalLesson(
                                "l2", "居家监测", "正确测量血压的时间与方法，记录表使用说明。", 14, V4),
                        new PortalCourse.PortalLesson(
                                "l3", "生活方式", "低盐饮食、适量运动与作息建议。", 14, V5)
                ),
                null));
        putCourse(new PortalCourse(
                "course-003", "📱", "手机培训", "智能手机基础操作",
                "微信视频通话、健康码、网上挂号等实用技能。",
                "30分钟", 35000, 4.9,
                List.of(
                        new PortalCourse.PortalLesson(
                                "l1", "基础设置", "字体放大、音量、Wi-Fi 连接与桌面整理。", 10, V3),
                        new PortalCourse.PortalLesson(
                                "l2", "微信常用功能", "发消息、语音通话、视频通话与相册分享。", 12, V5),
                        new PortalCourse.PortalLesson(
                                "l3", "安全注意事项", "权限管理、陌生链接识别与支付保护。", 8, V6)
                ),
                null));
    }

    private void seedNews() {
        putNews(new PortalNews(
                "news-001", "📋",
                "国务院办公厅关于发展银发经济的意见",
                "加快银发经济规模化、标准化、集群化发展，培育高精尖产品和高品质服务模式。",
                "2024-12-20", "国务院办公厅",
                """
                国务院办公厅印发意见，明确发展银发经济的总体要求与重点任务：
                
                一、扩大养老服务供给，推动社区居家、机构、旅居等业态协同发展；
                二、丰富老年用品与智慧健康产品供给，鼓励 AI 监测、康复辅具等创新应用；
                三、发展老年文娱、教育、旅游等消费场景，满足精神文化需求；
                四、加强人才培养与标准体系建设，提升行业专业化水平。
                
                各地可结合演示平台中的「文娱活动」「生活服务」「学习赋能」等模块，对接机构落地试点。
                """,
                1280,
                null));
        putNews(new PortalNews(
                "news-002", "📰",
                "2024智慧养老产业市场规模突破万亿",
                "AI 技术深度应用于健康监测、跌倒预警与服务派单，产业增速显著。",
                "2024-12-10", "新华网",
                """
                据行业研究报告，2024 年我国智慧养老相关市场规模首次突破万亿元。
                
                核心增长点包括：可穿戴与居家传感器、机构级数据中台、家属端协同 App，
                以及面向基层的适老化改造。银发智盾演示系统展示了多角色统一入口、
                告警联动派单与学习反诈等典型能力，可供院校与机构教学演示参考。
                """,
                3560,
                null));
        putNews(new PortalNews(
                "news-003", "🔬",
                "AI智能监测设备在养老机构的创新应用",
                "跌倒检测、体征监测、电子围栏等技术进入规模化试点阶段。",
                "2024-11-28", "科技日报",
                """
                多家养老机构试点部署 AI 视觉跌倒检测、毫米波雷达体征监测与 GPS 电子围栏。
                
                系统可将告警推送至家属端与管理端，并结合外呼升级、工单派单形成闭环。
                演示环境中，长者端上报位置与体征后，管理端大屏可查看趋势与待处理告警列表。
                """,
                2100,
                null));
    }

    public synchronized PortalActivity saveActivity(PortalActivity activity) {
        putActivity(activity);
        persistActivity(activity);
        return activity;
    }

    public synchronized boolean deleteActivity(String id) {
        boolean removed = activities.remove(id) != null;
        if (removed) {
            db.ifPresent(d -> d.deleteActivity(id));
            if (db.isEmpty()) persistEditableContent();
        }
        return removed;
    }

    public synchronized PortalNews saveNews(PortalNews item) {
        putNews(item);
        newsViewCounts.put(item.id(), item.viewCount());
        persistNews(item);
        return item;
    }

    public synchronized boolean deleteNews(String id) {
        boolean removed = news.remove(id) != null;
        if (removed) {
            newsViewCounts.remove(id);
            db.ifPresent(d -> d.deleteNews(id));
            if (db.isEmpty()) persistEditableContent();
        }
        return removed;
    }

    public synchronized PortalCourse saveCourse(PortalCourse course) {
        putCourse(course);
        courseViewCounts.put(course.id(), course.viewCount());
        persistCourse(course);
        return course;
    }

    public synchronized boolean deleteCourse(String id) {
        boolean removed = courses.remove(id) != null;
        if (removed) {
            courseViewCounts.remove(id);
            db.ifPresent(d -> d.deleteCourse(id));
            if (db.isEmpty()) persistEditableContent();
        }
        return removed;
    }

    public String newActivityId() {
        return "act-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public String newCourseId() {
        return "course-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public String newNewsId() {
        return "news-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public String newLessonId() {
        return "l-" + UUID.randomUUID().toString().substring(0, 6);
    }

    /** 恢复内置示例数据并写入数据库或 JSON */
    public synchronized void reseedDefaults() {
        activities.clear();
        courses.clear();
        news.clear();
        newsViewCounts.clear();
        courseViewCounts.clear();
        seedActivities();
        seedCourses();
        seedNews();
        if (db.isPresent()) {
            db.get().replaceAll(listActivities(), listCourses(), listNews());
            reloadFromDb();
        } else {
            persistEditableContent();
        }
    }

    private void putActivity(PortalActivity a) {
        activities.put(a.id(), a);
    }

    private void putLife(PortalLifeService s) {
        lifeServices.put(s.id(), s);
    }

    private void putCourse(PortalCourse c) {
        courses.put(c.id(), c);
        courseViewCounts.putIfAbsent(c.id(), c.viewCount());
    }

    private void putNews(PortalNews n) {
        news.put(n.id(), n);
        newsViewCounts.putIfAbsent(n.id(), n.viewCount());
    }

    public List<PortalActivity> listActivities() {
        return new ArrayList<>(activities.values());
    }

    public Optional<PortalActivity> getActivity(String id) {
        return Optional.ofNullable(activities.get(id));
    }

    public List<PortalLifeService> listLifeServices() {
        return new ArrayList<>(lifeServices.values());
    }

    public Optional<PortalLifeService> getLifeService(String id) {
        return Optional.ofNullable(lifeServices.get(id));
    }

    public List<PortalCourse> listCourses() {
        return new ArrayList<>(courses.values());
    }

    public Optional<PortalCourse> getCourse(String id) {
        return Optional.ofNullable(courses.get(id));
    }

    public List<PortalNews> listNews() {
        return new ArrayList<>(news.values());
    }

    public Optional<PortalNews> getNews(String id) {
        return Optional.ofNullable(news.get(id));
    }

    public int getNewsViews(String id) {
        if (db.isPresent()) {
            return db.get().getNewsViews(id);
        }
        return newsViewCounts.getOrDefault(id, 0);
    }

    public void incrementNewsViews(String id) {
        newsViewCounts.merge(id, 1, Integer::sum);
        db.ifPresent(d -> d.incrementNewsViews(id));
    }

    public int getCourseViews(String id) {
        if (db.isPresent()) {
            return db.get().getCourseViews(id);
        }
        return courseViewCounts.getOrDefault(id, 0);
    }

    public void incrementCourseViews(String id) {
        courseViewCounts.merge(id, 1, Integer::sum);
        db.ifPresent(d -> d.incrementCourseViews(id));
    }

    public synchronized void incrementActivityEnrolled(String activityId) {
        activities.computeIfPresent(activityId, (k, a) -> {
            if (a.enrolledCount() >= a.capacity()) {
                return a;
            }
            PortalActivity updated = new PortalActivity(
                    a.id(),
                    a.icon(),
                    a.tag(),
                    a.title(),
                    a.timeLabel(),
                    a.location(),
                    a.description(),
                    a.capacity(),
                    a.enrolledCount() + 1,
                    a.open(),
                    a.imageUrl());
            persistActivity(updated);
            return updated;
        });
    }

    public synchronized PortalRegistration addRegistration(PortalRegistration reg) {
        registrations.put(reg.id(), reg);
        db.ifPresent(d -> d.addRegistration(reg));
        if (reg.type() == PortalRegistration.PortalRegistrationType.ACTIVITY) {
            activities.computeIfPresent(reg.targetId(), (k, a) -> {
                PortalActivity updated = new PortalActivity(
                        a.id(),
                        a.icon(),
                        a.tag(),
                        a.title(),
                        a.timeLabel(),
                        a.location(),
                        a.description(),
                        a.capacity(),
                        a.enrolledCount() + 1,
                        a.open(),
                        a.imageUrl());
                persistActivity(updated);
                return updated;
            });
        }
        return reg;
    }

    public boolean hasRegistration(String actorKey, PortalRegistration.PortalRegistrationType type, String targetId) {
        if (db.isPresent() && db.get().hasRegistration(actorKey, type, targetId)) {
            return true;
        }
        return registrations.values().stream()
                .anyMatch(r -> actorKey.equals(r.userId())
                        && r.type() == type
                        && r.targetId().equals(targetId)
                        && !"CANCELLED".equals(r.status()));
    }

    public Set<String> completedLessons(String actorKey, String courseId) {
        if (db.isPresent()) {
            return db.get().completedLessons(actorKey, courseId);
        }
        return courseCompletedLessons.getOrDefault(actorKey + ":" + courseId, Set.of());
    }

    public void completeLesson(String actorKey, String courseId, String lessonId) {
        if (db.isPresent()) {
            db.get().completeLesson(actorKey, courseId, lessonId);
        }
        String key = actorKey + ":" + courseId;
        courseCompletedLessons.compute(key, (k, v) -> {
            Set<String> set = v == null ? new LinkedHashSet<>() : new LinkedHashSet<>(v);
            set.add(lessonId);
            return set;
        });
    }

    public String newRegistrationId() {
        return "reg-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
