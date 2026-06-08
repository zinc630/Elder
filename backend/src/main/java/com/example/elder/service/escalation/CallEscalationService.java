package com.example.elder.service.escalation;

import com.example.elder.config.CallProperties;
import com.example.elder.domain.AlarmEvent;
import com.example.elder.domain.CallAttempt;
import com.example.elder.service.alarm.AlarmDbService;
import com.example.elder.service.notification.NotificationService;
import com.example.elder.store.InMemoryDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class CallEscalationService {
    private static final Logger log = LoggerFactory.getLogger(CallEscalationService.class);

    private final InMemoryDataStore store;
    private final AlarmDbService alarmDbService;
    private final CallProperties callProperties;
    private final NotificationService notificationService;

    public CallEscalationService(
            InMemoryDataStore store,
            AlarmDbService alarmDbService,
            CallProperties callProperties,
            NotificationService notificationService
    ) {
        this.store = store;
        this.alarmDbService = alarmDbService;
        this.callProperties = callProperties;
        this.notificationService = notificationService;
    }

    public void startOrAdvanceCalls() {
        List<AlarmEvent> alarms = alarmDbService.listByStatuses(
                List.of(AlarmEvent.Status.CHILD_NOTIFIED, AlarmEvent.Status.CALL_PENDING),
                100
        );
        Instant now = Instant.now();

        for (AlarmEvent alarm : alarms) {
            // If already confirmed (race), just skip.
            if (alarm.getStatus() == AlarmEvent.Status.CONFIRMED) continue;

            if (alarm.getStatus() == AlarmEvent.Status.CHILD_NOTIFIED) {
                startPrimaryCallIfNeeded(alarm, now);
                continue;
            }

            // CALL_PENDING path:
            Optional<CallAttempt> latestOpt = store.latestCallAttempt(alarm.getId());
            if (latestOpt.isEmpty()) {
                // No attempts yet -> start primary
                startPrimaryCallIfNeeded(alarm, now);
                continue;
            }

            CallAttempt latest = latestOpt.get();

            // If already answered -> confirm
            if (latest.getResult() == CallAttempt.Result.ANSWERED) {
                alarm.confirm("guardian_call_answered");
                alarmDbService.confirm(alarm.getId(), "guardian_call_answered");
                log.warn("Alarm confirmed by call. alarmEventId={}", alarm.getId());
                continue;
            }

            // If call result is missing, and ring timeout elapsed, treat as NO_ANSWER.
            if (latest.getResult() == null && now.isAfter(latest.getCreatedAt().plusSeconds(callProperties.getRingTimeoutSec()))) {
                store.updateCallAttemptResult(latest.getId(), CallAttempt.Result.NO_ANSWER, "ring_timeout");
                latest.setResult(CallAttempt.Result.NO_ANSWER, "ring_timeout");
            }

            if (latest.getResult() == null) {
                continue; // wait for result
            }

            boolean answered = latest.getResult() == CallAttempt.Result.ANSWERED;
            if (answered) continue;

            // Decide next attempt
            advanceAfterUnanswered(alarm, now);
        }
    }

    private void startPrimaryCallIfNeeded(AlarmEvent alarm, Instant now) {
        // If there was already a primary attempt, don't create another on every tick.
        List<CallAttempt> attempts = store.listCallAttempts(alarm.getId());
        boolean hasPrimaryAttempt = attempts.stream().anyMatch(a -> a.getStage() == CallAttempt.Stage.PRIMARY);
        if (hasPrimaryAttempt) {
            persistStatus(alarm, AlarmEvent.Status.CALL_PENDING);
            return;
        }

        persistStatus(alarm, AlarmEvent.Status.CALL_PENDING);
        CallAttempt attempt = new CallAttempt(alarm.getId(), CallAttempt.Stage.PRIMARY, 1, now);
        store.addCallAttempt(attempt);
        log.warn("Start primary call attempt alarmEventId={}, attemptNo=1 (simulated)", alarm.getId());
        // Skeleton: integrate telephony provider here.
    }

    private void advanceAfterUnanswered(AlarmEvent alarm, Instant now) {
        List<CallAttempt> attempts = store.listCallAttempts(alarm.getId());
        // Sort by createdAt for stable counting
        attempts.sort(Comparator.comparing(CallAttempt::getCreatedAt));

        long primaryUnanswered = attempts.stream()
                .filter(a -> a.getStage() == CallAttempt.Stage.PRIMARY)
                .filter(a -> a.getResult() != null && a.getResult() != CallAttempt.Result.ANSWERED)
                .count();

        long backupUnanswered = attempts.stream()
                .filter(a -> a.getStage() == CallAttempt.Stage.BACKUP)
                .filter(a -> a.getResult() != null && a.getResult() != CallAttempt.Result.ANSWERED)
                .count();

        // If primary unanswered exceeded allowed, switch to backup.
        if (primaryUnanswered >= callProperties.getPrimaryMaxAttempts()) {
            if (backupUnanswered >= callProperties.getBackupMaxAttempts()) {
                persistStatus(alarm, AlarmEvent.Status.CLOSED);
                log.warn("Call escalation closed: backup attempts exhausted. alarmEventId={}", alarm.getId());
                return;
            }

            int nextBackupAttemptNo = (int) backupUnanswered + 1;
            CallAttempt attempt = new CallAttempt(alarm.getId(), CallAttempt.Stage.BACKUP, nextBackupAttemptNo, now);
            store.addCallAttempt(attempt);
            log.warn("Start backup call attempt alarmEventId={}, attemptNo={}", alarm.getId(), nextBackupAttemptNo);
            return;
        }

        // Continue primary attempts.
        int nextPrimaryAttemptNo = (int) primaryUnanswered + 1;
        if (nextPrimaryAttemptNo > callProperties.getPrimaryMaxAttempts()) {
            // Defensive: if count already at max
            return;
        }

        CallAttempt attempt = new CallAttempt(alarm.getId(), CallAttempt.Stage.PRIMARY, nextPrimaryAttemptNo, now);
        store.addCallAttempt(attempt);
        log.warn("Start primary call attempt alarmEventId={}, attemptNo={}", alarm.getId(), nextPrimaryAttemptNo);
    }

    private void persistStatus(AlarmEvent alarm, AlarmEvent.Status status) {
        alarm.setStatus(status);
        alarmDbService.updateStatus(alarm.getId(), status);
    }
}

