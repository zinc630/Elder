package com.example.elder.service.portal;

import com.example.elder.config.PortalUploadProperties;
import com.example.elder.dto.portal.admin.PortalMediaUploadDto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PortalMediaStorageService {
    private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> VIDEO_EXT = Set.of("mp4", "webm", "mov");

    private final PortalUploadProperties properties;

    public PortalMediaStorageService(PortalUploadProperties properties) {
        this.properties = properties;
    }

    public PortalMediaUploadDto store(MultipartFile file, String kind) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择文件");
        }
        String normalized = kind == null ? "image" : kind.trim().toLowerCase();
        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = extension(original);
        if ("video".equals(normalized)) {
            if (!VIDEO_EXT.contains(ext)) {
                throw new IllegalArgumentException("视频仅支持 mp4 / webm / mov");
            }
            if (file.getSize() > 120L * 1024 * 1024) {
                throw new IllegalArgumentException("视频不能超过 120MB");
            }
        } else {
            if (!IMAGE_EXT.contains(ext)) {
                throw new IllegalArgumentException("图片仅支持 jpg / png / gif / webp");
            }
            if (file.getSize() > 8L * 1024 * 1024) {
                throw new IllegalArgumentException("图片不能超过 8MB");
            }
        }
        Path root = Path.of(properties.getDir()).toAbsolutePath().normalize();
        Files.createDirectories(root);
        String stored = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = root.resolve(stored);
        try (var in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        String url = "/api/v1/public/media/" + stored;
        return new PortalMediaUploadDto(url, normalized, original);
    }

    private static String extension(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0 || i == name.length() - 1) return "";
        return name.substring(i + 1).toLowerCase();
    }
}
