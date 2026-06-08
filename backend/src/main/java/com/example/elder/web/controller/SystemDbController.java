package com.example.elder.web.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemDbController {

    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;

    public SystemDbController(ObjectProvider<JdbcTemplate> jdbcTemplateProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
    }

    @GetMapping("/db-status")
    public Map<String, Object> dbStatus() {
        Map<String, Object> result = new LinkedHashMap<>();
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            result.put("enabled", false);
            result.put("ok", false);
            result.put("message", "未配置数据库连接。请在 backend 目录创建 local-mysql.yml 并填写 spring.datasource。");
            return result;
        }

        Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        result.put("enabled", true);
        result.put("ok", Integer.valueOf(1).equals(one));
        result.put("message", "MySQL 连接正常");
        try {
            Integer portalRows = jdbcTemplate.queryForObject(
                    """
                    SELECT (
                      (SELECT COUNT(*) FROM portal_activity) +
                      (SELECT COUNT(*) FROM portal_course) +
                      (SELECT COUNT(*) FROM portal_news)
                    )
                    """,
                    Integer.class);
            result.put("portalContentRows", portalRows == null ? 0 : portalRows);
        } catch (Exception ex) {
            result.put("portalContentRows", -1);
            result.put("portalHint", "门户表未就绪，请重启后端以执行 schema.sql");
        }
        return result;
    }
}

