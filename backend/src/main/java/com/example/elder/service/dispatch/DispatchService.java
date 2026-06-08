package com.example.elder.service.dispatch;

import com.example.elder.domain.DispatchTask;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DispatchService {

    private final DispatchDbService dispatchDbService;

    public DispatchService(DispatchDbService dispatchDbService) {
        this.dispatchDbService = dispatchDbService;
    }

    public DispatchTask createTask(String elderId, String alarmEventId, String serviceType, Instant appointmentTime, String notes) {
        return createBookingTask(elderId, alarmEventId, serviceType, appointmentTime, notes, "AGENCY", "", "");
    }

    public DispatchTask createBookingTask(
            String elderId,
            String alarmEventId,
            String serviceType,
            Instant appointmentTime,
            String notes,
            String bookedByRole,
            String bookedByUserId,
            String bookedByName
    ) {
        Instant now = Instant.now();
        DispatchTask task = new DispatchTask(
                elderId,
                alarmEventId == null ? "" : alarmEventId,
                serviceType,
                appointmentTime,
                notes == null ? "" : notes,
                now,
                bookedByRole,
                bookedByUserId,
                bookedByName
        );
        return dispatchDbService.insert(task);
    }

    public DispatchTask getTask(String taskId) {
        return dispatchDbService.findById(taskId).orElse(null);
    }

    public List<DispatchTask> listTasks(int limit) {
        return dispatchDbService.listRecent(limit);
    }

    public List<DispatchTask> listTasksByStatus(DispatchTask.Status status) {
        return dispatchDbService.listByStatus(status);
    }

    public List<DispatchTask> listTasksForElder(String elderId, int limit) {
        return dispatchDbService.listByElder(elderId, limit);
    }

    public DispatchTask updateStatus(String taskId, DispatchTask.Status status) {
        DispatchTask t = getTask(taskId);
        if (t == null) {
            throw new IllegalArgumentException("task_not_found");
        }
        Instant now = Instant.now();
        dispatchDbService.updateStatus(taskId, status, now);
        t.setStatus(status);
        return t;
    }

    public DispatchTask assignWorker(String taskId, String workerId) {
        DispatchTask t = getTask(taskId);
        if (t == null) {
            throw new IllegalArgumentException("task_not_found");
        }
        Instant now = Instant.now();
        t.assignWorker(workerId);
        t.setStatus(DispatchTask.Status.IN_PROGRESS);
        dispatchDbService.assignWorker(taskId, workerId, DispatchTask.Status.IN_PROGRESS, now);
        return t;
    }
}
