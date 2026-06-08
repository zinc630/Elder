package com.example.elder.service.agency;

import com.example.elder.domain.DispatchTask;
import com.example.elder.dto.AgencyDashboardStatsDto;
import com.example.elder.service.dispatch.DispatchDbService;
import com.example.elder.service.elder.ElderDbService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.stereotype.Service;

@Service
public class AgencyDashboardService {

    private final DispatchDbService dispatchDbService;
    private final AgencyDbService agencyDbService;
    private final ElderDbService elderDbService;

    public AgencyDashboardService(
            DispatchDbService dispatchDbService,
            AgencyDbService agencyDbService,
            ElderDbService elderDbService
    ) {
        this.dispatchDbService = dispatchDbService;
        this.agencyDbService = agencyDbService;
        this.elderDbService = elderDbService;
    }

    public AgencyDashboardStatsDto getStats() {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        Instant dayStart = today.atStartOfDay(zone).toInstant();
        Instant dayEnd = dayStart.plusSeconds(86400);

        long distinctElders = dispatchDbService.countDistinctEldersServed();
        long profileElders = elderDbService.listElders().size();
        long totalElders = Math.max(distinctElders, profileElders);

        long agencyCount = agencyDbService.countAgencies();
        if (agencyCount < 1) {
            agencyCount = 1;
        }

        long todayOrders = dispatchDbService.countCreatedBetween(dayStart, dayEnd);
        long pendingOrders = dispatchDbService.countPending();
        long completedTotal = dispatchDbService.countByStatus(DispatchTask.Status.COMPLETED);
        long activeOrders = dispatchDbService.countActiveOrders();
        int completionRatePercent = activeOrders == 0
                ? 0
                : (int) Math.min(100, Math.round((completedTotal * 100.0) / activeOrders));

        long totalWorkers = agencyDbService.countWorkers();
        long onlineWorkers = agencyDbService.countOnlineWorkers();

        return new AgencyDashboardStatsDto(
                totalElders,
                agencyCount,
                todayOrders,
                pendingOrders,
                completedTotal,
                completionRatePercent,
                onlineWorkers,
                totalWorkers
        );
    }
}
