package dev.atilioaraujo.music.datasetapi.controller;

import dev.atilioaraujo.music.datasetapi.exception.SongHistoryAlreadyRegisteredException;
import dev.atilioaraujo.music.datasetapi.service.SongHistoryRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SongHistoryControllerTest {

    @Mock
    private SongHistoryRegistrationService songHistoryRegistrationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SongHistoryController controller = new SongHistoryController(songHistoryRegistrationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new SongHistoryControllerAdvice())
                .build();
    }

    @Test
    void shouldReturnCreatedWhenRegistrationSucceeds() throws Exception {
        String payload = """
                {
                  "musica": "MACHINE HEAD",
                  "artista": "BUSH",
                  "timestamp": 1774394786,
                  "data_hora": "2026-03-24T23:26:26.000Z"
                }
                """;

        mockMvc.perform(post("/api/song/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        verify(songHistoryRegistrationService, times(1)).register(any());
    }

    @Test
    void shouldReturnCreatedWhenSongAlreadyRegisteredExceptionIsThrown() throws Exception {
        String payload = """
                {
                  "musica": "MACHINE HEAD",
                  "artista": "BUSH",
                  "timestamp": 1774394786,
                  "data_hora": "2026-03-24T23:26:26.000Z"
                }
                """;

        doThrow(new SongHistoryAlreadyRegisteredException("already registered"))
                .when(songHistoryRegistrationService)
                .register(any());

        mockMvc.perform(post("/api/song/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReturnBadRequestWhenPayloadIsEmpty() throws Exception {
        mockMvc.perform(post("/api/song/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());

        verify(songHistoryRegistrationService, never()).register(any());
    }

    @Test
    void shouldReturnBadRequestWhenPayloadIsNull() throws Exception {
        mockMvc.perform(post("/api/song/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Payload cannot be null"));

        verify(songHistoryRegistrationService, never()).register(any());
    }
}



