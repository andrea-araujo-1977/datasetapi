package dev.atilioaraujo.music.datasetapi.dto;

import dev.atilioaraujo.music.datasetapi.domain.Album;
import dev.atilioaraujo.music.datasetapi.domain.Artist;
import dev.atilioaraujo.music.datasetapi.domain.Song;

public record SongCatalogData(
        Artist artist,
        Album album,
        Song song
) {
}

