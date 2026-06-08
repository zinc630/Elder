package com.example.elder.web.controller;

import com.example.elder.dto.PublicHomeSummaryDto;
import com.example.elder.service.alarm.AlarmDbService;
import com.example.elder.service.dispatch.DispatchDbService;
import com.example.elder.service.elder.ElderDbService;
import com.example.elder.web.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/v1/public/home")
public class PublicHomeController {
    private static final int MODULE_COUNT = 6;
    private static final int ROLE_COUNT = 4;

    private final ElderDbService elderDbService;
    private final DispatchDbService dispatchDbService;
    private final AlarmDbService alarmDbService;
    private final String deepSeekApiKey;

    public PublicHomeController(
            ElderDbService elderDbService,
            DispatchDbService dispatchDbService,
            AlarmDbService alarmDbService,
            @Value("${deepseek.api-key:}") String deepSeekApiKey) {
        this.elderDbService = elderDbService;
        this.dispatchDbService = dispatchDbService;
        this.alarmDbService = alarmDbService;
        this.deepSeekApiKey = deepSeekApiKey == null ? "" : deepSeekApiKey.trim();
    }

    @GetMapping("/summary")
    public ApiResponse<PublicHomeSummaryDto> summary() {
        long elders = elderDbService.listElders().size();
        ZoneId zone = ZoneId.systemDefault();
        Instant dayStart = LocalDate.now(zone).atStartOfDay(zone).toInstant();
        Instant dayEnd = dayStart.plusSeconds(86400);
        long today = alarmDbService.countBetween(dayStart, dayEnd);
        LocalDate todayLd = LocalDate.now(zone);
        long monthly = dispatchDbService.countCreatedInMonth(todayLd.getYear(), todayLd.getMonthValue(), zone);
        long devices = elders <= 0 ? 0 : elders * 2L;
        boolean deepSeekOk = !deepSeekApiKey.isEmpty();
        String notice = elders > 0
                ? "演示环境已接入 " + elders + " 位长者档案；今日触发告警 " + today + " 条。"
                : "演示环境：请使用注册时分配的 E/C/G/A 前缀账号完成绑定与登录。";
        return ApiResponse.ok(new PublicHomeSummaryDto(
                ROLE_COUNT,
                MODULE_COUNT,
                elders,
                today,
                monthly,
                devices,
                deepSeekOk,
                notice
        ));
    }
}
