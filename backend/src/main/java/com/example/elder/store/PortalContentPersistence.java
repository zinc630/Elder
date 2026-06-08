package com.example.elder.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PortalContentPersistence {
    private static final Logger log = LoggerFactory.getLogger(PortalContentPersistence.class);

    private final ObjectMapper objectMapper;
    private final Path dataFile;

    public PortalContentPersistence(
            ObjectMapper objectMapper, @Value("${elder.portal-data.file:./data/portal-content.json}") String file) {
        this.objectMapper = objectMapper;
        this.dataFile = Path.of(file).toAbsolutePath().normalize();
    }

    public PortalContentSnapshot loadIfPresent() {
        if (!Files.isRegularFile(dataFile)) {
            return null;
        }
        try {
            byte[] bytes = Files.readAllBytes(dataFile);
            return objectMapper.readValue(bytes, PortalContentSnapshot.class);
        } catch (IOException e) {
            log.warn("Failed to load portal content from {}: {}", dataFile, e.getMessage());
            return null;
        }
    }

    public void save(PortalContentSnapshot snapshot) {
        try {
            Files.createDirectories(dataFile.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFile.toFile(), snapshot);
        } catch (IOException e) {
            throw new IllegalStateException("无法保存门户内容: " + e.getMessage(), e);
        }
    }
}
