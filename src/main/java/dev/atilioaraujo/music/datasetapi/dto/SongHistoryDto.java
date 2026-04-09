package dev.atilioaraujo.music.datasetapi.dto;

import java.time.LocalDateTime;

public record SongHistoryDto(
        Integer artistId,
        String artistName,
        Integer albumId,
        String albumName,
        Integer songId,
        String songNameSource,
        String songNameStreaming,
        String artistGenre,
        String albumCoverImageUrl,
        Integer songLengthMs,
        LocalDateTime playedAt
) {
}

