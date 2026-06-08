package com.example.elder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vitals")
public class VitalsThresholdProperties {
    private int hrLow = 50;
    private int hrHigh = 120;

    private int sbpLow = 90;
    private int sbpHigh = 160;

    private int dbpLow = 60;
    private int dbpHigh = 100;

    public int getHrLow() {
        return hrLow;
    }

    public void setHrLow(int hrLow) {
        this.hrLow = hrLow;
    }

    public int getHrHigh() {
        return hrHigh;
    }

    public void setHrHigh(int hrHigh) {
        this.hrHigh = hrHigh;
    }

    public int getSbpLow() {
        return sbpLow;
    }

    public void setSbpLow(int sbpLow) {
        this.sbpLow = sbpLow;
    }

    public int getSbpHigh() {
        return sbpHigh;
    }

    public void setSbpHigh(int sbpHigh) {
        this.sbpHigh = sbpHigh;
    }

    public int getDbpLow() {
        return dbpLow;
    }

    public void setDbpLow(int dbpLow) {
        this.dbpLow = dbpLow;
    }

    public int getDbpHigh() {
        return dbpHigh;
    }

    public void setDbpHigh(int dbpHigh) {
        this.dbpHigh = dbpHigh;
    }
}

