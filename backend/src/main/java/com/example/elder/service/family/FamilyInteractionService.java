package com.example.elder.service.family;

import com.example.elder.dto.FamilyAlbumPhotoDto;
import com.example.elder.dto.FamilyChatMessageDto;
import com.example.elder.dto.FamilySendMessageRequest;
import com.example.elder.dto.FamilyStartVideoCallRequest;
import com.example.elder.dto.FamilyVideoCallDto;
import com.example.elder.dto.portal.admin.PortalMediaUploadDto;
import com.example.elder.service.elder.ElderDbService;
import com.example.elder.service.portal.PortalMediaStorageService;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FamilyInteractionService {
    private static final Set<String> VIDEO_STATUSES = Set.of("ringing", "connected", "ended", "declined");
    private static final long VIDEO_TTL_SEC = 120;

    private final JdbcTemplate jdbcTemplate;
    private final ElderDbService elderDbService;
    private final PortalMediaStorageService mediaStorageService;

    public FamilyInteractionService(
            JdbcTemplate jdbcTemplate,
            ElderDbService elderDbService,
            PortalMediaStorageService mediaStorageService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.elderDbService = elderDbService;
        this.mediaStorageService = mediaStorageService;
    }

    public void ensureElderExists(String elderId) {
        String normalized = elderId.trim().toUpperCase();
        if (elderDbService.getElderProfile(normalized).isEmpty()) {
            throw new IllegalArgumentException("invalid_elder_id");
        }
    }

    public List<FamilyChatMessageDto> listMessages(String elderId, int limit) {
        ensureElderExists(elderId);
        String eid = elderId.trim().toUpperCase();
        seedDemoIfEmpty(eid);
        int lim = Math.min(Math.max(limit, 1), 200);
        return jdbcTemplate.query(
                """
                SELECT id, elder_user_id, sender_role, sender_user_id, sender_name, content, created_at
                FROM family_chat_message
                WHERE elder_user_id = ?
                ORDER BY created_at ASC
                LIMIT ?
                """,
                (rs, i) -> new FamilyChatMessageDto(
                        rs.getString("id"),
                        rs.getString("elder_user_id"),
                        rs.getString("sender_role"),
                        rs.getString("sender_user_id"),
                        rs.getString("sender_name"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toInstant()
                ),
                eid,
                lim
        );
    }

    public FamilyChatMessageDto sendMessage(String elderId, FamilySendMessageRequest req) {
        ensureElderExists(elderId);
        String eid = elderId.trim().toUpperCase();
        String role = req.senderRole().trim().toUpperCase();
        if (!role.equals("ELDER") && !role.equals("CHILD")) {
            throw new IllegalArgumentException("invalid_sender_role");
        }
        String content = req.content().trim();
        if (content.isEmpty()) {
            throw new IllegalArgumentException("empty_content");
        }
        if (content.length() > 2000) {
            throw new IllegalArgumentException("content_too_long");
        }
        String id = "FCM-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        Instant now = Instant.now();
        jdbcTemplate.update(
                """
                INSERT INTO family_chat_message(id, elder_user_id, sender_role, sender_user_id, sender_name, content, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                eid,
                role,
                req.senderUserId(),
                req.senderName().trim(),
                content,
                Timestamp.from(now)
        );
        return new FamilyChatMessageDto(id, eid, role, req.senderUserId(), req.senderName().trim(), content, now);
    }

    public List<FamilyAlbumPhotoDto> listAlbum(String elderId) {
        ensureElderExists(elderId);
        String eid = elderId.trim().toUpperCase();
        return jdbcTemplate.query(
                """
                SELECT id, elder_user_id, uploaded_by_role, uploaded_by_user_id, file_name, media_url, created_at
                FROM family_album_photo
                WHERE elder_user_id = ?
                ORDER BY created_at DESC
                LIMIT 100
                """,
                (rs, i) -> new FamilyAlbumPhotoDto(
                        rs.getString("id"),
                        rs.getString("elder_user_id"),
                        rs.getString("uploaded_by_role"),
                        rs.getString("uploaded_by_user_id"),
                        rs.getString("file_name"),
                        rs.getString("media_url"),
                        rs.getTimestamp("created_at").toInstant()
                ),
                eid
        );
    }

    public FamilyAlbumPhotoDto uploadAlbum(
            String elderId,
            String uploadedByRole,
            String uploadedByUserId,
            MultipartFile file
    ) throws IOException {
        ensureElderExists(elderId);
        String eid = elderId.trim().toUpperCase();
        String role = uploadedByRole.trim().toUpperCase();
        PortalMediaUploadDto stored = mediaStorageService.store(file, "image");
        String id = "FAP-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        Instant now = Instant.now();
        jdbcTemplate.update(
                """
                INSERT INTO family_album_photo(id, elder_user_id, uploaded_by_role, uploaded_by_user_id, file_name, media_url, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                eid,
                role,
                uploadedByUserId,
                stored.originalFilename(),
                stored.url(),
                Timestamp.from(now)
        );
        return new FamilyAlbumPhotoDto(id, eid, role, uploadedByUserId, stored.originalFilename(), stored.url(), now);
    }

    public void deleteAlbumPhoto(String elderId, String photoId) {
        ensureElderExists(elderId);
        jdbcTemplate.update(
                "DELETE FROM family_album_photo WHERE id = ? AND elder_user_id = ?",
                photoId,
                elderId.trim().toUpperCase()
        );
    }

    public FamilyVideoCallDto getActiveVideoCall(String elderId) {
        ensureElderExists(elderId);
        String eid = elderId.trim().toUpperCase();
        List<FamilyVideoCallDto> rows = jdbcTemplate.query(
                """
                SELECT call_id, elder_user_id, child_user_id, child_name,
                       initiator_role, elder_display_name, status, offer_sdp, answer_sdp, updated_at
                FROM family_video_call
                WHERE elder_user_id = ?
                ORDER BY updated_at DESC
                LIMIT 1
                """,
                (rs, i) -> mapVideoCall(rs),
                eid
        );
        if (rows.isEmpty()) {
            return null;
        }
        FamilyVideoCallDto call = rows.get(0);
        if (isVideoCallExpired(call)) {
            return null;
        }
        return call;
    }

    public FamilyVideoCallDto startVideoCall(String elderId, FamilyStartVideoCallRequest req) {
        ensureElderExists(elderId);
        String eid = elderId.trim().toUpperCase();
        String initiator = normalizeInitiator(req.initiatorRole());
        jdbcTemplate.update(
                """
                UPDATE family_video_call
                SET status = 'ended', updated_at = CURRENT_TIMESTAMP
                WHERE elder_user_id = ? AND status IN ('ringing', 'connected')
                """,
                eid
        );
        String callId = "VC-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        Instant now = Instant.now();
        jdbcTemplate.update(
                """
                INSERT INTO family_video_call(
                    call_id, elder_user_id, child_user_id, child_name,
                    initiator_role, elder_display_name, status, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, 'ringing', ?)
                """,
                callId,
                eid,
                req.childUserId(),
                req.callerDisplayName().trim(),
                initiator,
                req.elderDisplayName(),
                Timestamp.from(now)
        );
        return getVideoCallById(callId, eid);
    }

    public FamilyVideoCallDto updateVideoSignal(
            String elderId,
            String callId,
            String offerSdp,
            String answerSdp
    ) {
        ensureElderExists(elderId);
        String eid = elderId.trim().toUpperCase();
        if (offerSdp != null && !offerSdp.isBlank()) {
            jdbcTemplate.update(
                    "UPDATE family_video_call SET offer_sdp = ?, updated_at = CURRENT_TIMESTAMP WHERE call_id = ? AND elder_user_id = ?",
                    offerSdp,
                    callId,
                    eid
            );
        }
        if (answerSdp != null && !answerSdp.isBlank()) {
            jdbcTemplate.update(
                    "UPDATE family_video_call SET answer_sdp = ?, updated_at = CURRENT_TIMESTAMP WHERE call_id = ? AND elder_user_id = ?",
                    answerSdp,
                    callId,
                    eid
            );
        }
        return getVideoCallById(callId, eid);
    }

    public FamilyVideoCallDto patchVideoCall(String elderId, String callId, String status) {
        ensureElderExists(elderId);
        String eid = elderId.trim().toUpperCase();
        String st = status.trim().toLowerCase();
        if (!VIDEO_STATUSES.contains(st)) {
            throw new IllegalArgumentException("invalid_video_status");
        }
        int updated = jdbcTemplate.update(
                """
                UPDATE family_video_call
                SET status = ?, updated_at = CURRENT_TIMESTAMP
                WHERE call_id = ? AND elder_user_id = ?
                """,
                st,
                callId,
                eid
        );
        if (updated == 0) {
            throw new IllegalArgumentException("video_call_not_found");
        }
        return getVideoCallById(callId, eid);
    }

    public void seedDemoIfEmpty(String elderId) {
        String eid = elderId.trim().toUpperCase();
        Long chatCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM family_chat_message WHERE elder_user_id = ?",
                Long.class,
                eid
        );
        if (chatCount != null && chatCount > 0) {
            return;
        }
        sendMessage(
                eid,
                new FamilySendMessageRequest("CHILD", null, "子女", "爸爸，今天天气好，记得多喝水哦～")
        );
        sendMessage(
                eid,
                new FamilySendMessageRequest("ELDER", eid, "老人", "好的，孩子。晚上我再量一量血压。")
        );
    }

    private FamilyVideoCallDto getVideoCallById(String callId, String elderId) {
        return jdbcTemplate.query(
                """
                SELECT call_id, elder_user_id, child_user_id, child_name,
                       initiator_role, elder_display_name, status, offer_sdp, answer_sdp, updated_at
                FROM family_video_call
                WHERE call_id = ? AND elder_user_id = ?
                """,
                (rs, i) -> mapVideoCall(rs),
                callId,
                elderId
        ).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("video_call_not_found"));
    }

    private static FamilyVideoCallDto mapVideoCall(java.sql.ResultSet rs) throws java.sql.SQLException {
        String initiator = rs.getString("initiator_role");
        if (initiator == null || initiator.isBlank()) {
            initiator = "CHILD";
        }
        return new FamilyVideoCallDto(
                rs.getString("call_id"),
                rs.getString("elder_user_id"),
                rs.getString("child_user_id"),
                rs.getString("child_name"),
                initiator,
                rs.getString("elder_display_name"),
                rs.getString("status"),
                rs.getString("offer_sdp"),
                rs.getString("answer_sdp"),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    private static String normalizeInitiator(String role) {
        if (role == null || role.isBlank()) {
            return "CHILD";
        }
        String r = role.trim().toUpperCase();
        return "ELDER".equals(r) ? "ELDER" : "CHILD";
    }

    private static boolean isVideoCallExpired(FamilyVideoCallDto call) {
        if ("ringing".equals(call.status()) || "connected".equals(call.status())) {
            return false;
        }
        return call.updatedAt().isBefore(Instant.now().minusSeconds(VIDEO_TTL_SEC));
    }
}
