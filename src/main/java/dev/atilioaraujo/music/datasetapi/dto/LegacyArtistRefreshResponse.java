package dev.atilioaraujo.music.datasetapi.dto;

public record LegacyArtistRefreshResponse(
        Integer totalUpdatedArtists,
        Integer pendingArtists
) {
}

