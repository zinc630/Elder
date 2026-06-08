package com.example.elder.service.child;

import com.example.elder.domain.ChildProfile;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChildDbService {

    private final JdbcTemplate jdbcTemplate;

    public ChildDbService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<ChildProfile> getChildProfile(String childId) {
        var rows = jdbcTemplate.query(
                """
                SELECT u.user_id, u.user_name, u.age, u.gender, u.address, cp.relation_desc
                FROM sys_user u
                LEFT JOIN child_profile cp ON cp.child_user_id = u.user_id
                WHERE u.user_id = ? AND u.role_code = 'CHILD'
                LIMIT 1
                """,
                (rs, rowNum) -> new ChildProfile(
                        rs.getString("user_id"),
                        rs.getString("user_name"),
                        (Integer) rs.getObject("age"),
                        rs.getString("gender"),
                        rs.getString("address"),
                        rs.getString("relation_desc")
                ),
                childId.trim().toUpperCase()
        );
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(rows.get(0));
    }

    public ChildProfile upsertChildProfile(ChildProfile p) {
        String childId = p.getChildId().trim().toUpperCase();
        assertChildExists(childId);
        jdbcTemplate.update(
                """
                UPDATE sys_user
                SET user_name = ?, age = ?, gender = ?, address = ?
                WHERE user_id = ?
                """,
                p.getName(), p.getAge(), blankToNull(p.getGender()), blankToNull(p.getAddress()), childId
        );
        jdbcTemplate.update(
                """
                INSERT INTO child_profile(child_user_id, relation_desc)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE relation_desc = VALUES(relation_desc)
                """,
                childId, blankToNull(p.getRelationDesc())
        );
        return new ChildProfile(childId, p.getName(), p.getAge(), p.getGender(), p.getAddress(), p.getRelationDesc());
    }

    private void assertChildExists(String childId) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sys_user WHERE user_id = ? AND role_code = 'CHILD'",
                Integer.class,
                childId
        );
        if (exists == null || exists == 0) {
            throw new IllegalArgumentException("child_not_found: " + childId);
        }
    }

    private String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }
}
