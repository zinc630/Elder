package com.example.elder.service.agency;

import com.example.elder.domain.portal.PortalActivity;
import com.example.elder.dto.AgencyActivityDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 机构活动与门户 {@link portal_activity} 的字段映射（单一数据源）。
 */
final class AgencyPortalActivityMapper {

    private static final String META_PREFIX = "AGENCY_META:";
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final DateTimeFormatter DISPLAY =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
    private static final Pattern META_LINE = Pattern.compile("^" + META_PREFIX + "(\\{.+})$", Pattern.MULTILINE);

    private AgencyPortalActivityMapper() {}

    record Meta(Instant start, Instant end, String status) {}

    static AgencyActivityDto toAgencyDto(PortalActivity a) {
        Meta meta = parseMeta(a.description()).orElse(null);
        Instant start = meta != null ? meta.start() : Instant.now();
        Instant end = meta != null ? meta.end() : start.plusSeconds(7200);
        String status = meta != null ? meta.status() : deriveStatus(start, end, a.open());
        String desc = stripMeta(a.description());
        return new AgencyActivityDto(
                a.id(),
                a.title(),
                start,
                end,
                a.location(),
                a.capacity(),
                a.enrolledCount(),
                status,
                desc
        );
    }

    static PortalActivity toPortalActivity(
            String id,
            String title,
            Instant start,
            Instant end,
            String location,
            int capacity,
            int enrolled,
            String status,
            String description,
            String tag,
            String icon,
            boolean open
    ) {
        String category = tag == null || tag.isBlank() ? "文娱活动" : tag.trim();
        String iconVal = icon == null || icon.isBlank() ? "🎨" : icon.trim();
        String body = embedMeta(description, start, end, status);
        return new PortalActivity(
                id,
                iconVal,
                category,
                title.trim(),
                formatTimeLabel(start, end),
                location == null ? "" : location.trim(),
                body,
                capacity,
                enrolled,
                open,
                null
        );
    }

    static String formatTimeLabel(Instant start, Instant end) {
        return DISPLAY.format(start) + " ~ " + DISPLAY.format(end);
    }

    static String deriveStatus(Instant start, Instant end, boolean open) {
        if (!open) return "ended";
        Instant now = Instant.now();
        if (now.isBefore(start)) return "upcoming";
        if (now.isAfter(end)) return "ended";
        return "ongoing";
    }

    static Optional<Meta> parseMeta(String description) {
        if (description == null || description.isBlank()) {
            return Optional.empty();
        }
        Matcher m = META_LINE.matcher(description.trim());
        if (!m.find()) {
            return Optional.empty();
        }
        try {
            MetaJson json = JSON.readValue(m.group(1), MetaJson.class);
            if (json.start == null || json.end == null || json.status == null) {
                return Optional.empty();
            }
            return Optional.of(new Meta(Instant.parse(json.start), Instant.parse(json.end), json.status));
        } catch (JsonProcessingException | java.time.DateTimeException e) {
            return Optional.empty();
        }
    }

    static String embedMeta(String userDescription, Instant start, Instant end, String status) {
        String metaLine = META_PREFIX + writeMeta(start, end, status);
        String user = userDescription == null ? "" : userDescription.trim();
        if (user.isEmpty()) {
            return metaLine;
        }
        if (user.startsWith(META_PREFIX)) {
            user = stripMeta(user);
        }
        return metaLine + "\n\n" + user;
    }

    static String stripMeta(String description) {
        if (description == null) return "";
        String trimmed = description.trim();
        if (trimmed.startsWith(META_PREFIX)) {
            int split = trimmed.indexOf("\n\n");
            return split < 0 ? "" : trimmed.substring(split + 2).trim();
        }
        Matcher m = META_LINE.matcher(trimmed);
        if (m.find()) {
            return trimmed.replace(m.group(0), "").trim();
        }
        return trimmed;
    }

    private static String writeMeta(Instant start, Instant end, String status) {
        try {
            MetaJson json = new MetaJson();
            json.start = start.toString();
            json.end = end.toString();
            json.status = status;
            return JSON.writeValueAsString(json);
        } catch (JsonProcessingException e) {
            return "{\"start\":\"" + start + "\",\"end\":\"" + end + "\",\"status\":\"" + status + "\"}";
        }
    }

    private static class MetaJson {
        public String start;
        public String end;
        public String status;
    }
}
