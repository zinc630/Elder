package com.example.elder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "elder.portal-upload")
public class PortalUploadProperties {
    /** 本地上传根目录 */
    private String dir = "./data/portal-uploads";

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }
}
