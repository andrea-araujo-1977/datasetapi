package dev.atilioaraujo.music.datasetapi.controller;

import dev.atilioaraujo.music.datasetapi.dto.LegacyDataRefreshResponse;
import dev.atilioaraujo.music.datasetapi.service.LegacyDataRefreshService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LegacyDataControllerTest {

    @Mock
    private LegacyDataRefreshService legacyDataRefreshService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LegacyDataController controller = new LegacyDataController(legacyDataRefreshService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new LegacyDataControllerAdvice())
                .build();
    }

    @Test
    void shouldRefreshLegacyData() throws Exception {
        when(legacyDataRefreshService.refresh(eq(3)))
                .thenReturn(new LegacyDataRefreshResponse(3, 7));

        mockMvc.perform(put("/api/data/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 3}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUpdatedSongs").value(3))
                .andExpect(jsonPath("$.pendingSongs").value(7));
    }

    @Test
    void shouldReturnBadRequestWhenAmountIsMissing() throws Exception {
        mockMvc.perform(put("/api/data/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("amount must be informed"));
    }
}

