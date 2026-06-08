package com.example.elder.service.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthDbService {

    private static final List<String> VALID_ROLES = List.of("ELDER", "CHILD", "AGENCY", "ADMIN");
    private final JdbcTemplate jdbcTemplate;

    public AuthDbService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long countByRole(String roleCode) {
        Long n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE role_code = ?",
                Long.class,
                normalizeRole(roleCode)
        );
        return n == null ? 0L : n;
    }

    public Optional<Map<String, Object>> findUserByRoleAndName(String roleCode, String userName) {
        var rows = jdbcTemplate.queryForList(
                """
                SELECT user_id, role_code, user_name, password, address
                FROM sys_user
                WHERE role_code = ? AND user_name = ?
                LIMIT 1
                """,
                normalizeRole(roleCode), userName.trim());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(rows.get(0));
    }

    public Optional<Map<String, Object>> findUserBySystemId(String userId) {
        var rows = jdbcTemplate.queryForList(
                """
                SELECT user_id, role_code, user_name, address
                FROM sys_user
                WHERE user_id = ?
                LIMIT 1
                """,
                userId.trim().toUpperCase(Locale.ROOT));
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(rows.get(0));
    }

    public Optional<String> findSystemIdByRoleAndName(String roleCode, String userName) {
        var rows = jdbcTemplate.queryForList(
                "SELECT user_id FROM sys_user WHERE role_code = ? AND user_name = ? LIMIT 1",
                normalizeRole(roleCode), userName.trim());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.ofNullable((String) rows.get(0).get("user_id"));
    }

    public List<String> listLinkedEldersForChild(String childUserId) {
        return jdbcTemplate.query(
                """
                SELECT elder_user_id
                FROM elder_child_binding
                WHERE child_user_id = ?
                ORDER BY is_primary DESC, id ASC
                """,
                (rs, rowNum) -> rs.getString("elder_user_id"),
                childUserId.trim().toUpperCase(Locale.ROOT));
    }

    @Transactional
    public void bindEldersForChild(String childUserId, List<String> elderIds) {
        String childId = childUserId.trim().toUpperCase(Locale.ROOT);
        ensureUserRole(childId, "CHILD");
        if (elderIds == null || elderIds.isEmpty()) throw new IllegalArgumentException("请至少绑定一个老人账号");
        boolean first = true;
        for (String elderIdRaw : elderIds) {
            String elderId = elderIdRaw.trim().toUpperCase(Locale.ROOT);
            if (elderId.isEmpty()) continue;
            ensureUserRole(elderId, "ELDER");
            jdbcTemplate.update(
                    """
                    INSERT INTO elder_child_binding(elder_user_id, child_user_id, is_primary)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE is_primary = VALUES(is_primary)
                    """,
                    elderId, childId, first ? 1 : 0
            );
            first = false;
        }
    }

    @Transactional
    public void bindChildForElder(String elderUserId, String childUserId) {
        String elderId = elderUserId.trim().toUpperCase(Locale.ROOT);
        String childId = childUserId.trim().toUpperCase(Locale.ROOT);
        ensureUserRole(elderId, "ELDER");
        ensureUserRole(childId, "CHILD");
        jdbcTemplate.update(
                """
                INSERT INTO elder_child_binding(elder_user_id, child_user_id, is_primary)
                VALUES (?, ?, 1)
                ON DUPLICATE KEY UPDATE is_primary = 1
                """,
                elderId, childId
        );
    }

    @Transactional
    public String register(AuthRegisterCommand cmd) {
        String roleCode = normalizeRole(cmd.roleCode());
        String userId = cmd.userId().trim().toUpperCase(Locale.ROOT);
        String userName = cmd.userName().trim();
        String password = cmd.password();

        Integer dupRoleName = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sys_user WHERE role_code = ? AND user_name = ?",
                Integer.class, roleCode, userName);
        if (dupRoleName != null && dupRoleName > 0) throw new IllegalArgumentException("用户已存在");

        Integer dupUserId = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sys_user WHERE user_id = ?",
                Integer.class, userId);
        if (dupUserId != null && dupUserId > 0) throw new IllegalArgumentException("系统账号ID已被占用");

        jdbcTemplate.update(
                """
                INSERT INTO sys_user(user_id, role_code, user_name, password, gender, age, address)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                userId, roleCode, userName, password,
                blankToNull(cmd.gender()), cmd.age(), blankToNull(cmd.address()));

        switch (roleCode) {
            case "ELDER" -> {
                jdbcTemplate.update(
                        """
                        INSERT INTO elder_profile(elder_user_id, key_health_notes)
                        VALUES (?, ?)
                        ON DUPLICATE KEY UPDATE key_health_notes = VALUES(key_health_notes)
                        """,
                        userId, blankToNull(cmd.keyHealthNotes()));
                if (cmd.linkedChildId() != null && !cmd.linkedChildId().isBlank()) {
                    String childId = cmd.linkedChildId().trim().toUpperCase(Locale.ROOT);
                    ensureUserRole(childId, "CHILD");
                    bindElderChild(userId, childId);
                }
            }
            case "CHILD" -> {
                jdbcTemplate.update(
                        """
                        INSERT INTO child_profile(child_user_id, relation_desc)
                        VALUES (?, ?)
                        ON DUPLICATE KEY UPDATE relation_desc = VALUES(relation_desc)
                        """,
                        userId, blankToNull(cmd.relationDesc()));
                for (String elderId : splitCsvIds(cmd.linkedElderId())) {
                    ensureUserRole(elderId, "ELDER");
                    bindElderChild(elderId, userId);
                }
            }
            case "AGENCY" -> jdbcTemplate.update(
                    """
                    INSERT INTO agency_profile(agency_user_id, agency_name)
                    VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE agency_name = VALUES(agency_name)
                    """,
                    userId, blankToNull(cmd.address()));
            case "ADMIN" -> jdbcTemplate.update(
                    """
                    INSERT INTO admin_profile(admin_user_id, org_name)
                    VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE org_name = VALUES(org_name)
                    """,
                    userId, blankToNull(cmd.address()));
            default -> throw new IllegalArgumentException("不支持的角色");
        }
        return userId;
    }

    private void bindElderChild(String elderId, String childId) {
        jdbcTemplate.update(
                """
                INSERT INTO elder_child_binding(elder_user_id, child_user_id, is_primary)
                VALUES (?, ?, 0)
                ON DUPLICATE KEY UPDATE elder_user_id = VALUES(elder_user_id)
                """,
                elderId, childId);
    }

    private void ensureUserRole(String userId, String roleCode) {
        var found = findUserBySystemId(userId)
                .orElseThrow(() -> new IllegalArgumentException("未找到关联账号：" + userId));
        String foundRole = Objects.toString(found.get("role_code"), "");
        if (!roleCode.equals(foundRole)) {
            throw new IllegalArgumentException("关联账号角色不匹配：" + userId);
        }
    }

    private String normalizeRole(String roleCode) {
        String r = roleCode == null ? "" : roleCode.trim().toUpperCase(Locale.ROOT);
        if (!VALID_ROLES.contains(r)) throw new IllegalArgumentException("无效角色: " + roleCode);
        return r;
    }

    private String blankToNull(String s) {
        if (s == null || s.isBlank()) return null;
        return s.trim();
    }

    private List<String> splitCsvIds(String s) {
        if (s == null || s.isBlank()) return List.of();
        String[] arr = s.split("[,，;；\\s]+");
        List<String> out = new ArrayList<>();
        for (String v : arr) {
            String t = v == null ? "" : v.trim().toUpperCase(Locale.ROOT);
            if (!t.isEmpty() && !out.contains(t)) out.add(t);
        }
        return out;
    }

    public record AuthRegisterCommand(
            String userId,
            String roleCode,
            String userName,
            String password,
            String gender,
            Integer age,
            String address,
            String linkedElderId,
            String linkedChildId,
            String keyHealthNotes,
            String relationDesc
    ) {
    }

    @Transactional
    public String allocateNextSystemUserId(String roleCode) {
        String role = normalizeRole(roleCode);
        Map<String, String> prefixMap = new HashMap<>();
        prefixMap.put("ELDER", "E");
        prefixMap.put("CHILD", "C");
        prefixMap.put("AGENCY", "G");
        prefixMap.put("ADMIN", "A");
        String prefix = prefixMap.get(role);

        // 以数据库现有最大 user_id 为基准分配（删除/重启/多端并发都能对齐）
        Long maxExisting = jdbcTemplate.queryForObject(
                """
                SELECT COALESCE(MAX(CAST(SUBSTRING(user_id, 2) AS UNSIGNED)), 100000)
                FROM sys_user
                WHERE role_code = ?
                """,
                Long.class,
                role
        );
        long base = maxExisting == null ? 100000L : maxExisting;

        // 锁定该角色序列行，避免并发重复分配
        var seqRows = jdbcTemplate.queryForList(
                "SELECT seq_no FROM sys_id_sequence WHERE role_code = ? FOR UPDATE",
                role
        );
        long currentSeq = seqRows.isEmpty() ? base : ((Number) seqRows.get(0).get("seq_no")).longValue();
        long nextSeq = Math.max(currentSeq, base) + 1L;

        int updated = jdbcTemplate.update(
                "UPDATE sys_id_sequence SET seq_no = ? WHERE role_code = ?",
                nextSeq,
                role
        );
        if (updated == 0) {
            jdbcTemplate.update(
                    "INSERT INTO sys_id_sequence(role_code, seq_no) VALUES(?, ?)",
                    role,
                    nextSeq
            );
        }
        return prefix + nextSeq;
    }

    /**
     * 仅预览下一个系统账号ID（不会写库自增）。用于注册页展示，避免刷新页面导致序号“空涨”。
     */
    public String previewNextSystemUserId(String roleCode) {
        String role = normalizeRole(roleCode);
        Map<String, String> prefixMap = new HashMap<>();
        prefixMap.put("ELDER", "E");
        prefixMap.put("CHILD", "C");
        prefixMap.put("AGENCY", "G");
        prefixMap.put("ADMIN", "A");
        String prefix = prefixMap.get(role);

        Long maxExisting = jdbcTemplate.queryForObject(
                """
                SELECT COALESCE(MAX(CAST(SUBSTRING(user_id, 2) AS UNSIGNED)), 100000)
                FROM sys_user
                WHERE role_code = ?
                """,
                Long.class,
                role
        );
        long base = maxExisting == null ? 100000L : maxExisting;

        Long seqNo = jdbcTemplate.queryForObject(
                "SELECT seq_no FROM sys_id_sequence WHERE role_code = ?",
                Long.class,
                role
        );
        long currentSeq = seqNo == null ? base : seqNo;
        long nextSeq = Math.max(currentSeq, base) + 1L;
        return prefix + nextSeq;
    }
}

