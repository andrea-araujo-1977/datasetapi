package dev.atilioaraujo.music.datasetapi.service;

import dev.atilioaraujo.music.datasetapi.dao.AlbumDao;
import dev.atilioaraujo.music.datasetapi.dao.ArtistDao;
import dev.atilioaraujo.music.datasetapi.dao.SongDao;
import dev.atilioaraujo.music.datasetapi.dao.SongDao.SongCatalogData;
import dev.atilioaraujo.music.datasetapi.domain.Album;
import dev.atilioaraujo.music.datasetapi.domain.Artist;
import dev.atilioaraujo.music.datasetapi.domain.Song;
import dev.atilioaraujo.music.datasetapi.dto.LegacyDataRefreshResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

@Service
public class LegacyDataRefreshService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyDataRefreshService.class);

    private final SongDao songDao;
    private final AlbumDao albumDao;
    private final ArtistDao artistDao;
    private final SpotifyCatalogLookupService spotifyCatalogLookupService;

    public LegacyDataRefreshService(
            SongDao songDao,
            AlbumDao albumDao,
            ArtistDao artistDao,
            SpotifyCatalogLookupService spotifyCatalogLookupService
    ) {
        this.songDao = songDao;
        this.albumDao = albumDao;
        this.artistDao = artistDao;
        this.spotifyCatalogLookupService = spotifyCatalogLookupService;
    }

    public LegacyDataRefreshResponse refresh(Integer amount) {
        Assert.notNull(amount, "amount must be informed");
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }

        List<SongCatalogData> catalogItems = songDao.findSongsWithLengthZero(amount);
        int updatedSongs = 0;
        for (SongCatalogData catalogItem : catalogItems) {
            try {
                refreshCatalogItem(catalogItem);
                updatedSongs++;
            } catch (Exception ex) {
                LOGGER.warn(
                        "Could not refresh legacy metadata for artist='{}' song='{}'. Processing will continue.",
                        catalogItem.artist().name(),
                        catalogItem.song().nameSource(),
                        ex
                );
            }
        }

        Integer pendingSongs = songDao.getPendingLengthZeroCount();
        return new LegacyDataRefreshResponse(updatedSongs, pendingSongs);
    }

    private void refreshCatalogItem(SongCatalogData catalogItem) {
        var spotifyTrackInfo = spotifyCatalogLookupService.findTrack(
                catalogItem.artist().name(),
                catalogItem.song().nameSource()
        );

        updateArtist(catalogItem.artist(), spotifyTrackInfo.artistGenre());
        updateAlbum(catalogItem.album(), spotifyTrackInfo.albumReleaseDate(), spotifyTrackInfo.albumCoverImageUrl());
        updateSong(catalogItem.song(), spotifyTrackInfo.songName(), spotifyTrackInfo.lengthMs());
    }

    private void updateArtist(Artist currentArtist, String genre) {
        if (Objects.equals(currentArtist.genre(), genre)) {
            return;
        }

        artistDao.update(new Artist(currentArtist.idArtist(), currentArtist.name(), genre));
    }

    private void updateAlbum(Album currentAlbum, java.time.LocalDate releaseDate, String coverImageUrl) {
        if (Objects.equals(currentAlbum.releaseDate(), releaseDate)
                && Objects.equals(currentAlbum.coverImageUrl(), coverImageUrl)) {
            return;
        }

        albumDao.update(new Album(currentAlbum.idAlbum(), currentAlbum.name(), releaseDate, coverImageUrl, currentAlbum.artistId()));
    }

    private void updateSong(Song currentSong, String streamingName, Integer lengthMs) {
        if (Objects.equals(currentSong.nameStreaming(), streamingName)
                && Objects.equals(currentSong.lengthMs(), lengthMs)) {
            return;
        }

        songDao.update(new Song(
                currentSong.idSong(),
                currentSong.nameSource(),
                streamingName,
                currentSong.trackNumber(),
                lengthMs,
                currentSong.albumId()
        ));
    }
}


