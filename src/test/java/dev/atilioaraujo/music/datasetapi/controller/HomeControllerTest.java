package dev.atilioaraujo.music.datasetapi.controller;

import dev.atilioaraujo.music.datasetapi.domain.DashboardData;
import dev.atilioaraujo.music.datasetapi.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    private static final String DASHBOARD_TIME_ZONE = "America/Sao_Paulo";

    @Mock
    private DashboardService dashboardService;

    @Mock
    private Model model;

    private HomeController controller;

    @BeforeEach
    void setUp() {
        controller = new HomeController(dashboardService, DASHBOARD_TIME_ZONE);
    }

    @Test
    void shouldLoadHomePageAndAddDashboardDataToModel() {
        // Arrange
        DashboardData dashboardData = new DashboardData(
                10,
                5,
                100,
                250,
                LocalDateTime.now(),
                List.of()
        );
        when(dashboardService.getDashboardData()).thenReturn(dashboardData);

        // Act
        String viewName = controller.home(model);

        // Assert
        assertEquals("home", viewName);
        verify(dashboardService, times(1)).getDashboardData();
        verify(model, times(1)).addAttribute("dashboardData", dashboardData);
        verify(model, times(1)).addAttribute("dashboardTimeZone", DASHBOARD_TIME_ZONE);
    }
}
