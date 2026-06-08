package com.example.elder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "call")
public class CallProperties {
    private int ringTimeoutSec = 30;
    private int primaryMaxAttempts = 4; // primary: unanswered count > 3 => after 4th attempt
    private int backupMaxAttempts = 2;
    private String primaryGuardianPhone = "0000000000";
    private String backupGuardianPhone = "0000000000";

    public int getRingTimeoutSec() {
        return ringTimeoutSec;
    }

    public void setRingTimeoutSec(int ringTimeoutSec) {
        this.ringTimeoutSec = ringTimeoutSec;
    }

    public int getPrimaryMaxAttempts() {
        return primaryMaxAttempts;
    }

    public void setPrimaryMaxAttempts(int primaryMaxAttempts) {
        this.primaryMaxAttempts = primaryMaxAttempts;
    }

    public int getBackupMaxAttempts() {
        return backupMaxAttempts;
    }

    public void setBackupMaxAttempts(int backupMaxAttempts) {
        this.backupMaxAttempts = backupMaxAttempts;
    }

    public String getPrimaryGuardianPhone() {
        return primaryGuardianPhone;
    }

    public void setPrimaryGuardianPhone(String primaryGuardianPhone) {
        this.primaryGuardianPhone = primaryGuardianPhone;
    }

    public String getBackupGuardianPhone() {
        return backupGuardianPhone;
    }

    public void setBackupGuardianPhone(String backupGuardianPhone) {
        this.backupGuardianPhone = backupGuardianPhone;
    }
}

