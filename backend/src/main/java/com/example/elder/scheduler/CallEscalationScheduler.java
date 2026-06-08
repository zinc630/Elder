package com.example.elder.scheduler;

import com.example.elder.service.escalation.CallEscalationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CallEscalationScheduler {
    private final CallEscalationService service;

    public CallEscalationScheduler(CallEscalationService service) {
        this.service = service;
    }

    @Scheduled(fixedDelay = 5000)
    public void tick() {
        service.startOrAdvanceCalls();
    }
}

