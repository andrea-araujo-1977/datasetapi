package dev.atilioaraujo.music.datasetapi.controller;

import dev.atilioaraujo.music.datasetapi.service.DashboardService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final DashboardService dashboardService;
    private final String dashboardTimeZone;

    public HomeController(
            DashboardService dashboardService,
            @Value("${app.dashboard.time-zone:America/Sao_Paulo}") String dashboardTimeZone
    ) {
        this.dashboardService = dashboardService;
        this.dashboardTimeZone = dashboardTimeZone;
    }

    @GetMapping("/home")
    public String home(Model model) {
        var dashboardData = dashboardService.getDashboardData();
        model.addAttribute("dashboardData", dashboardData);
        model.addAttribute("dashboardTimeZone", dashboardTimeZone);
        return "home";
    }
}
