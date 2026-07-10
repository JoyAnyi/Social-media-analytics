package com.company.socialanalytics.dashboard;

import com.company.socialanalytics.security.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    DashboardSummary summary(CurrentUser currentUser) {
        boolean admin = currentUser != null && currentUser.authorities().contains("ROLE_ADMIN");
        return dashboardService.summary(admin);
    }
}
