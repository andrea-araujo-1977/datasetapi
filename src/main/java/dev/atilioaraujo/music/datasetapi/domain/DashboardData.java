package dev.atilioaraujo.music.datasetapi.domain;

import dev.atilioaraujo.music.datasetapi.dto.SongHistoryDto;

import java.time.LocalDateTime;
import java.util.List;

public record DashboardData(
        Integer totalArtists,
        Integer totalAlbums,
        Integer totalSongs,
        Integer totalSongHistory,
        LocalDateTime oldestPlayedDate,
        List<SongHistoryDto> lastFivePlayedSongs
) {
}

