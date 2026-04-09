package dev.atilioaraujo.music.datasetapi.dto;

public record ArtistView(
        String artistName,
        Integer totalPlays,
        String artistImageUrl
) {
}

