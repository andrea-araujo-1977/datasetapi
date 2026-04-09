package dev.atilioaraujo.music.datasetapi.controller.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LegacyDataRefreshRequest(
        @JsonProperty("amount") Integer amount
) {
}

