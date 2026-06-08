package com.example.elder.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({VitalsThresholdProperties.class, CallProperties.class, PortalUploadProperties.class})
public class AppConfig {
}

