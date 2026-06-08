package com.example.elder.store;

import com.example.elder.domain.AgencyAnomalyReport;
import com.example.elder.domain.AgencyClockInRecord;
import com.example.elder.domain.AlarmEvent;
import com.example.elder.domain.CallAttempt;
import com.example.elder.domain.ElderGeofence;
import com.example.elder.domain.ElderLastLocation;
import com.example.elder.domain.ElderProfile;
import com.example.elder.domain.DispatchTask;
import com.example.elder.domain.ServiceWorker;
import com.example.elder.domain.Measurement;
import com.example.elder.domain.AlarmEvent.Status;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Demo-only in-memory store.
 * Replace with DB + message queue when moving from skeleton to production.
 */
@Component
public class InMemoryDataStore {
    private final Map<String, Deque<Measurement>> vitalsByElder = new ConcurrentHashMap<>();
    private final Map<String, AlarmEvent> alarmsById = new ConcurrentHashMap<>();
    private final Map<String, List<String>> alarmIdsByElder = new ConcurrentHashMap<>();
    private final Map<String, DispatchTask> tasksById = new ConcurrentHashMap<>();
    private final Map<String, List<CallAttempt>> callAttemptsByAlarm = new ConcurrentHashMap<>();
    private final Map<String, ElderProfile> elderProfilesById = new ConcurrentHashMap<>();
    private final Map<String, ServiceWorker> workersById = new ConcurrentHashMap<>();
    private final Map<String, AgencyAnomalyReport> agencyAnomaliesById = new ConcurrentHashMap<>();
    private final List<AgencyClockInRecord> agencyClockIns = new ArrayList<>();
    private final Map<String, ElderGeofence> geofencesByElder = new ConcurrentHashMap<>();
    private final Map<String, ElderLastLocation> lastLocationByElder = new ConcurrentHashMap<>();

    @PostConstruct
    public void seedElderProfiles() {
        // Demo-only sample data. Replace with DB in real system.
        elderProfilesById.put("elder-001", new ElderProfile(
                "elder-001",
                "张奶奶",
                72,
                "女",
                "上海市浦东新区XX路",
                "高血压、糖尿病（示例）"
        ));
        elderProfilesById.put("elder-002", new ElderProfile(
                "elder-002",
                "李爷爷",
                78,
                "男",
                "杭州市拱墅区XX街",
                "冠心病（示例）"
        ));

        // Demo workers for agency pages.
        ServiceWorker w1 = new ServiceWorker(
                "李磊芳",
                "高级护理员（在线）",
                "18800000001",
                ServiceWorker.OnlineStatus.ONLINE,
                "NURSING"
        );
        ServiceWorker w2 = new ServiceWorker(
                "王丽",
                "助餐员（在线）",
                "18800000002",
                ServiceWorker.OnlineStatus.ONLINE,
                "NURSING"
        );
        ServiceWorker w3 = new ServiceWorker(
                "赵强",
                "保洁员（离线）",
                "18800000003",
                ServiceWorker.OnlineStatus.OFFLINE,
                "HOUSEKEEPING"
        );
        addWorker(w1);
        addWorker(w2);
        addWorker(w3);

        ServiceWorker w4 = new ServiceWorker(
                "李秀芳",
                "高级护理员",
                "18800000004",
                ServiceWorker.OnlineStatus.ONLINE,
                "BATH"
        );
        addWorker(w4);

        Instant now = Instant.now();
        AgencyAnomalyReport a1 = new AgencyAnomalyReport(
                "赵强",
                "elder-001",
                "张奶奶",
                "设备离线",
                "腕表约 2 小时无数据上传",
                now.minusSeconds(3600),
                AgencyAnomalyReport.Status.PENDING
        );
        AgencyAnomalyReport a2 = new AgencyAnomalyReport(
                "王丽",
                "elder-002",
                "李爷爷",
                "体征异常",
                "血压连续偏高，已通知家属",
                now.minusSeconds(7200),
                AgencyAnomalyReport.Status.PROCESSED
        );
        agencyAnomaliesById.put(a1.getId(), a1);
        agencyAnomaliesById.put(a2.getId(), a2);

        // Demo map: Pudong / Hangzhou centers with electronic fence + last device position.
        geofencesByElder.put("elder-001", new ElderGeofence(
                "elder-001", 31.245944, 121.567706, 400, "养老院生活区（演示电子围栏）"));
        lastLocationByElder.put("elder-001", new ElderLastLocation(
                31.24620, 121.56810, 12.0, "device_simulated", now.minusSeconds(300)));

        geofencesByElder.put("elder-002", new ElderGeofence(
                "elder-002", 30.274084, 120.155070, 350, "院区电子围栏（演示）"));
        lastLocationByElder.put("elder-002", new ElderLastLocation(
                30.27390, 120.15490, 8.0, "device_simulated", now.minusSeconds(600)));

        agencyClockIns.add(new AgencyClockInRecord(
                "李秀芳",
                "张奶奶",
                "助餐",
                now.minusSeconds(86400),
                "上海市浦东新区XX路",
                "已完成打卡"
        ));
        agencyClockIns.add(new AgencyClockInRecord(
                "王丽",
                "李爷爷",
                "保洁",
                now.minusSeconds(172800),
                "杭州市拱墅区XX街",
                "已完成打卡"
        ));
    }

    public List<AlarmEvent> listAlarmsByStatus(Collection<Status> statuses) {
        List<AlarmEvent> res = new ArrayList<>();
        for (AlarmEvent e : alarmsById.values()) {
            if (statuses.contains(e.getStatus())) {
                res.add(e);
            }
        }
        return res;
    }

    public void appendVitals(Measurement measurement, int maxMeasurementsPerElder) {
        Deque<Measurement> q = vitalsByElder.computeIfAbsent(measurement.getElderId(), k -> new ArrayDeque<>());
        synchronized (q) {
            q.addLast(measurement);
            while (q.size() > maxMeasurementsPerElder) {
                q.removeFirst();
            }
        }
    }

    public List<Measurement> recentVitals(String elderId) {
        Deque<Measurement> q = vitalsByElder.get(elderId);
        if (q == null) return List.of();
        synchronized (q) {
            return new ArrayList<>(q);
        }
    }

    public AlarmEvent upsertAlarm(AlarmEvent event) {
        alarmsById.put(event.getId(), event);
        alarmIdsByElder.computeIfAbsent(event.getElderId(), k -> new ArrayList<>()).add(event.getId());
        return event;
    }

    public AlarmEvent getAlarm(String alarmEventId) {
        return alarmsById.get(alarmEventId);
    }

    public List<AlarmEvent> listAlarmsByElder(String elderId, int limit) {
        List<String> ids = alarmIdsByElder.getOrDefault(elderId, List.of());
        int start = Math.max(0, ids.size() - limit);
        List<AlarmEvent> res = new ArrayList<>();
        for (int i = start; i < ids.size(); i++) {
            AlarmEvent e = alarmsById.get(ids.get(i));
            if (e != null) res.add(e);
        }
        return res;
    }

    public void confirmAlarm(String alarmEventId, String confirmationSource) {
        AlarmEvent e = alarmsById.get(alarmEventId);
        if (e == null) throw new IllegalArgumentException("alarm_not_found");
        e.confirm(confirmationSource);
    }

    public CallAttempt addCallAttempt(CallAttempt attempt) {
        callAttemptsByAlarm.computeIfAbsent(attempt.getAlarmEventId(), k -> new ArrayList<>()).add(attempt);
        return attempt;
    }

    public List<CallAttempt> listCallAttempts(String alarmEventId) {
        return callAttemptsByAlarm.getOrDefault(alarmEventId, List.of());
    }

    public Optional<CallAttempt> findCallAttemptById(String callAttemptId) {
        for (List<CallAttempt> attempts : callAttemptsByAlarm.values()) {
            for (CallAttempt a : attempts) {
                if (a.getId().equals(callAttemptId)) return Optional.of(a);
            }
        }
        return Optional.empty();
    }

    public Optional<CallAttempt> latestCallAttempt(String alarmEventId) {
        List<CallAttempt> attempts = callAttemptsByAlarm.getOrDefault(alarmEventId, List.of());
        return attempts.stream()
                .max((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
    }

    public void updateCallAttemptResult(String callAttemptId, CallAttempt.Result result, String resultDetail) {
        CallAttempt a = findCallAttemptById(callAttemptId)
                .orElseThrow(() -> new IllegalArgumentException("call_attempt_not_found"));
        a.setResult(result, resultDetail);
    }

    public DispatchTask addTask(DispatchTask task) {
        tasksById.put(task.getId(), task);
        return task;
    }

    public DispatchTask getTask(String taskId) {
        return tasksById.get(taskId);
    }

    public Optional<ElderProfile> getElderProfile(String elderId) {
        return Optional.ofNullable(elderProfilesById.get(elderId));
    }

    public ElderProfile upsertElderProfile(ElderProfile profile) {
        elderProfilesById.put(profile.getElderId(), profile);
        return profile;
    }

    public List<ElderProfile> listElderProfiles() {
        return new ArrayList<>(elderProfilesById.values());
    }

    public List<DispatchTask> listTasksByStatus(DispatchTask.Status status) {
        List<DispatchTask> res = new ArrayList<>();
        for (DispatchTask t : tasksById.values()) {
            if (t.getStatus() == status) res.add(t);
        }
        res.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return res;
    }

    public List<DispatchTask> listTasks(int limit) {
        List<DispatchTask> all = new ArrayList<>(tasksById.values());
        all.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        if (all.size() > limit) return all.subList(0, limit);
        return all;
    }

    public List<ServiceWorker> listWorkers(String keyword) {
        List<ServiceWorker> res = new ArrayList<>();
        String k = keyword == null ? "" : keyword.trim();
        for (ServiceWorker w : workersById.values()) {
            if (k.isEmpty()
                    || (w.getName() != null && w.getName().contains(k))
                    || (w.getPhone() != null && w.getPhone().contains(k))
                    || (w.getPosition() != null && w.getPosition().contains(k))) {
                res.add(w);
            }
        }
        res.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
        return res;
    }

    public ServiceWorker addWorker(ServiceWorker worker) {
        workersById.put(worker.getId(), worker);
        return worker;
    }

    public ServiceWorker getWorker(String workerId) {
        return workersById.get(workerId);
    }

    public ServiceWorker updateWorker(String workerId, ServiceWorker patch) {
        ServiceWorker existing = workersById.get(workerId);
        if (existing == null) return null;
        existing.setName(patch.getName());
        existing.setPosition(patch.getPosition());
        existing.setPhone(patch.getPhone());
        existing.setOnlineStatus(patch.getOnlineStatus());
        // keep serviceType from patch
        return existing;
    }

    public void deleteWorker(String workerId) {
        workersById.remove(workerId);
    }

    public List<AgencyAnomalyReport> listAgencyAnomalies() {
        List<AgencyAnomalyReport> res = new ArrayList<>(agencyAnomaliesById.values());
        res.sort((a, b) -> b.getReportedAt().compareTo(a.getReportedAt()));
        return res;
    }

    public AgencyAnomalyReport getAgencyAnomaly(String id) {
        return agencyAnomaliesById.get(id);
    }

    public void processAgencyAnomaly(String id) {
        AgencyAnomalyReport r = agencyAnomaliesById.get(id);
        if (r == null) throw new IllegalArgumentException("anomaly_not_found");
        r.markProcessed();
    }

    public List<AgencyClockInRecord> listAgencyClockIns() {
        List<AgencyClockInRecord> res = new ArrayList<>(agencyClockIns);
        res.sort((a, b) -> b.getClockAt().compareTo(a.getClockAt()));
        return res;
    }

    public List<AlarmEvent> listRecentAlarms(int limit) {
        List<AlarmEvent> all = new ArrayList<>(alarmsById.values());
        all.sort((a, b) -> b.getTriggeredAt().compareTo(a.getTriggeredAt()));
        if (all.size() > limit) return all.subList(0, limit);
        return all;
    }

    public long countAlarmsTriggeredBetween(Instant startInclusive, Instant endExclusive) {
        long c = 0;
        for (AlarmEvent e : alarmsById.values()) {
            Instant t = e.getTriggeredAt();
            if (!t.isBefore(startInclusive) && t.isBefore(endExclusive)) {
                c++;
            }
        }
        return c;
    }

    public long countTasksCreatedInMonth(int year, int month) {
        long c = 0;
        for (DispatchTask t : tasksById.values()) {
            ZonedDateTime z = t.getCreatedAt().atZone(ZoneId.systemDefault());
            if (z.getYear() == year && z.getMonthValue() == month) {
                c++;
            }
        }
        return c;
    }

    public Instant startOfTodayLocal() {
        return ZonedDateTime.now(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    public List<Integer> alarmCountsLastDays(int days) {
        List<Integer> res = new ArrayList<>();
        ZoneId z = ZoneId.systemDefault();
        for (int i = days - 1; i >= 0; i--) {
            ZonedDateTime day = ZonedDateTime.now(z).toLocalDate().minusDays(i).atStartOfDay(z);
            Instant start = day.toInstant();
            Instant end = day.plusDays(1).toInstant();
            res.add((int) countAlarmsTriggeredBetween(start, end));
        }
        return res;
    }

    public Optional<ElderGeofence> getGeofence(String elderId) {
        return Optional.ofNullable(geofencesByElder.get(elderId));
    }

    public void putGeofence(ElderGeofence fence) {
        geofencesByElder.put(fence.elderId(), fence);
    }

    public Optional<ElderLastLocation> getLastLocation(String elderId) {
        return Optional.ofNullable(lastLocationByElder.get(elderId));
    }

    public void putLastLocation(String elderId, ElderLastLocation location) {
        lastLocationByElder.put(elderId, location);
    }
}

