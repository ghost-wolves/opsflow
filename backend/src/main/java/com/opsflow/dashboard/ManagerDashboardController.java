package com.opsflow.dashboard;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class ManagerDashboardController {

    private final ManagerDashboardService managerDashboardService;

    public ManagerDashboardController(ManagerDashboardService managerDashboardService) {
        this.managerDashboardService = managerDashboardService;
    }

    @GetMapping("/manager")
    @PreAuthorize("hasRole('MANAGER')")
    public ManagerDashboardResponse managerDashboard() {
        return managerDashboardService.getDashboard();
    }
}
