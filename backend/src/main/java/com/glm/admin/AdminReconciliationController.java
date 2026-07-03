package com.glm.admin;

import com.glm.admin.dto.ReconciliationRow;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reconciliation")
public class AdminReconciliationController {

    private final ReconciliationService service;

    public AdminReconciliationController(ReconciliationService service) {
        this.service = service;
    }

    /**
     * On-demand reconciliation for a specific date.
     * Example: GET /admin/reconciliation?date=2025-07-03
     */
    @GetMapping
    public List<ReconciliationRow> reconcile(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.runForDate(date);
    }
}
