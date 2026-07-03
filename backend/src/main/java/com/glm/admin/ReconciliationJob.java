package com.glm.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Daily reconciliation job at 03:30 MYT (UTC+8) = 19:30 UTC.
 * Runs the previous day's payments through the reconciliation diff.
 */
@Component
public class ReconciliationJob {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationJob.class);

    private final ReconciliationService reconciliationService;

    public ReconciliationJob(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @Scheduled(cron = "0 30 19 * * *") // 19:30 UTC = 03:30 MYT
    public void run() {
        log.info("[RECON-JOB] Starting scheduled reconciliation");
        try {
            var rows = reconciliationService.runScheduled();
            log.info("[RECON-JOB] Completed. rows={}", rows.size());
        } catch (Exception e) {
            log.error("[RECON-JOB] Failed: {}", e.getMessage(), e);
        }
    }
}
