package dev.atilioaraujo.music.datasetapi.controller.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record SongHistoryRegistrationRequest(
        @JsonProperty("musica") String musica,
        @JsonProperty("artista") String artista,
        @JsonProperty("timestamp") Long timestamp,
        @JsonProperty("data_hora") Instant dataHora
) {
}
