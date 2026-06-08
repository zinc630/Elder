package com.example.elder.service.notification;

import com.example.elder.domain.AlarmEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendChildSmsAndVibration(AlarmEvent event) {
        // Skeleton: integrate SMS provider / push / vibration in production.
        log.warn("Send SMS+Vibration to child. alarmEventId={}, elderId={}, type={}",
                event.getId(), event.getElderId(), event.getType());
    }

    /** 服务机构端通过告警列表 API 拉取；此处记录联动日志便于排查 */
    public void notifyAgency(AlarmEvent event) {
        log.warn("Notify agency for alarm. alarmEventId={}, elderId={}, type={}",
                event.getId(), event.getElderId(), event.getType());
    }
}

