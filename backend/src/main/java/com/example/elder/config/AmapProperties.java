package com.example.elder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "amap")
public class AmapProperties {

    /** 高德 Web 服务 Key，见 https://console.amap.com/dev/key/app */
    private String webKey = "";
    private boolean enabled = true;
    private int searchRadiusMeters = 5000;
    private int maxResults = 8;

    public String getWebKey() {
        return webKey;
    }

    public void setWebKey(String webKey) {
        this.webKey = webKey == null ? "" : webKey.trim();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getSearchRadiusMeters() {
        return searchRadiusMeters;
    }

    public void setSearchRadiusMeters(int searchRadiusMeters) {
        this.searchRadiusMeters = searchRadiusMeters;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public boolean isConfigured() {
        return enabled && webKey != null && !webKey.isBlank();
    }
}
