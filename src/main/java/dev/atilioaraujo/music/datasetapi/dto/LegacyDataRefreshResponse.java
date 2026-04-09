package dev.atilioaraujo.music.datasetapi.dto;

public record LegacyDataRefreshResponse(
        Integer totalUpdatedSongs,
        Integer pendingSongs
) {
}

