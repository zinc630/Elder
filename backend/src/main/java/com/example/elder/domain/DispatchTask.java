package com.example.elder.domain;

import java.time.Instant;
import java.util.UUID;

public class DispatchTask {
    public enum Status {
        NEW,
        ASSIGNED,
        ARRIVING,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    private final String id;
    private final String elderId;
    private final String alarmEventId;
    private final String serviceType; // e.g. nursing, accompany, housekeeping
    private final Instant appointmentTime;
    private final String notes;

    private String assignedWorkerId; // nullable
    private Status status = Status.NEW;
    private Instant createdAt;
    private Instant updatedAt;
    private String bookedByRole;
    private String bookedByUserId;
    private String bookedByName;

    public DispatchTask(String elderId, String alarmEventId, String serviceType, Instant appointmentTime, String notes, Instant createdAt) {
        this(UUID.randomUUID().toString(), elderId, alarmEventId, serviceType, appointmentTime, notes, null, Status.NEW, createdAt, createdAt, "", "", "");
    }

    public DispatchTask(
            String elderId,
            String alarmEventId,
            String serviceType,
            Instant appointmentTime,
            String notes,
            Instant createdAt,
            String bookedByRole,
            String bookedByUserId,
            String bookedByName
    ) {
        this(UUID.randomUUID().toString(), elderId, alarmEventId, serviceType, appointmentTime, notes, null, Status.NEW, createdAt, createdAt,
                bookedByRole, bookedByUserId, bookedByName);
    }

    public DispatchTask(String id,
                         String elderId,
                         String alarmEventId,
                         String serviceType,
                         Instant appointmentTime,
                         String notes,
                         String assignedWorkerId,
                         Status status,
                         Instant createdAt,
                         Instant updatedAt) {
        this(id, elderId, alarmEventId, serviceType, appointmentTime, notes, assignedWorkerId, status, createdAt, updatedAt, "", "", "");
    }

    public DispatchTask(String id,
                         String elderId,
                         String alarmEventId,
                         String serviceType,
                         Instant appointmentTime,
                         String notes,
                         String assignedWorkerId,
                         Status status,
                         Instant createdAt,
                         Instant updatedAt,
                         String bookedByRole,
                         String bookedByUserId,
                         String bookedByName) {
        this.id = id;
        this.elderId = elderId;
        this.alarmEventId = alarmEventId == null ? "" : alarmEventId;
        this.serviceType = serviceType;
        this.appointmentTime = appointmentTime;
        this.notes = notes == null ? "" : notes;
        this.assignedWorkerId = assignedWorkerId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.bookedByRole = bookedByRole == null ? "" : bookedByRole;
        this.bookedByUserId = bookedByUserId == null ? "" : bookedByUserId;
        this.bookedByName = bookedByName == null ? "" : bookedByName;
    }

    public String getId() {
        return id;
    }

    public String getElderId() {
        return elderId;
    }

    public String getAlarmEventId() {
        return alarmEventId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public Instant getAppointmentTime() {
        return appointmentTime;
    }

    public String getNotes() {
        return notes;
    }

    public String getAssignedWorkerId() {
        return assignedWorkerId;
    }

    public void assignWorker(String workerId) {
        this.assignedWorkerId = workerId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getBookedByRole() {
        return bookedByRole;
    }

    public String getBookedByUserId() {
        return bookedByUserId;
    }

    public String getBookedByName() {
        return bookedByName;
    }
}

