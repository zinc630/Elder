package com.example.elder.domain;

import java.time.Instant;
import java.util.UUID;

public class ServiceWorker {
    public enum OnlineStatus {
        ONLINE,
        OFFLINE
    }

    private final String id;
    private String name;
    private String position; // 岗位（示例展示用）
    private String phone;
    private OnlineStatus onlineStatus;
    private Instant createdAt;

    // Derived from position in simplified demo
    private String serviceType;

    public ServiceWorker(String name, String position, String phone, OnlineStatus onlineStatus, String serviceType) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.position = position;
        this.phone = phone;
        this.onlineStatus = onlineStatus;
        this.serviceType = serviceType;
        this.createdAt = Instant.now();
    }

    public ServiceWorker(String id, String name, String position, String phone, OnlineStatus onlineStatus, String serviceType, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.phone = phone;
        this.onlineStatus = onlineStatus;
        this.serviceType = serviceType;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPosition() {
        return position;
    }

    public String getPhone() {
        return phone;
    }

    public OnlineStatus getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(OnlineStatus onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getServiceType() {
        return serviceType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

