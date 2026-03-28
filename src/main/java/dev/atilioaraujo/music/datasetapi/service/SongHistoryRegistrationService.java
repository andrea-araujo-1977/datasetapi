package dev.atilioaraujo.music.datasetapi.service;

import dev.atilioaraujo.music.datasetapi.controller.payload.SongHistoryRegistrationRequest;
import dev.atilioaraujo.music.datasetapi.dao.AlbumDao;
import dev.atilioaraujo.music.datasetapi.dao.ArtistDao;
import dev.atilioaraujo.music.datasetapi.dao.SongDao;
import dev.atilioaraujo.music.datasetapi.dao.SongHistoryDao;
import dev.atilioaraujo.music.datasetapi.domain.Album;
import dev.atilioaraujo.music.datasetapi.domain.Artist;
import dev.atilioaraujo.music.datasetapi.domain.Song;
import dev.atilioaraujo.music.datasetapi.domain.SongHistory;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import dev.atilioaraujo.music.datasetapi.exception.SongHistoryAlreadyRegisteredException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SongHistoryRegistrationService {

    private final SongHistoryDao songHistoryDao;
    private final SongDao songDao;
    private final AlbumDao albumDao;
    private final ArtistDao artistDao;
    private final SpotifyCatalogLookupService spotifyCatalogLookupService;

    public SongHistoryRegistrationService(
            SongHistoryDao songHistoryDao,
            SongDao songDao,
            AlbumDao albumDao,
            ArtistDao artistDao,
            SpotifyCatalogLookupService spotifyCatalogLookupService
    ) {
        this.songHistoryDao = songHistoryDao;
        this.songDao = songDao;
        this.albumDao = albumDao;
        this.artistDao = artistDao;
        this.spotifyCatalogLookupService = spotifyCatalogLookupService;
    }

    @Transactional
    public SongHistory register(SongHistoryRegistrationRequest request) {
        validateRequest(request);

        LocalDateTime playedAt = resolvePlayedAt(request);
        ensureSongNotAlreadyRegistered(request.musica(), playedAt);

        SpotifyCatalogLookupService.SpotifyTrackInfo spotifyTrackInfo =
                spotifyCatalogLookupService.findTrack(request.artista(), request.musica());

        Artist artist = resolveArtist(spotifyTrackInfo.artistName());
        Album album = resolveAlbum(spotifyTrackInfo.albumName(), artist.idArtist());
        Song song = resolveSong(request.musica(), spotifyTrackInfo.songName(), spotifyTrackInfo.trackNumber(), album.idAlbum());

        return songHistoryDao.insert(new SongHistory(null, song.idSong(), playedAt));
    }

    private void ensureSongNotAlreadyRegistered(String songName, LocalDateTime playedAt) {
        boolean alreadyRegistered = !songHistoryDao
                .findByPlayedDateAndSongNameIgnoreCase(playedAt, songName)
                .isEmpty();

        if (alreadyRegistered) {
            throw new SongHistoryAlreadyRegisteredException(
                    "Song '" + songName + "' already has history for date " + playedAt
            );
        }
    }

    private Artist resolveArtist(String artistName) {
        return artistDao.findByNameIgnoreCase(artistName)
                .stream()
                .findFirst()
                .orElseGet(() -> artistDao.insert(new Artist(null, artistName)));
    }

    private Album resolveAlbum(String albumName, Integer artistId) {
        return albumDao.findByNameIgnoreCase(albumName)
                .stream()
                .filter(album -> artistId.equals(album.artistId()))
                .findFirst()
                .orElseGet(() -> albumDao.insert(new Album(null, albumName, null, artistId)));
    }

    private Song resolveSong(String songNameSource, String songNameStreaming, Integer trackNumber, Integer albumId) {
        return songDao.findByNameIgnoreCase(songNameSource)
                .stream()
                .filter(song -> albumId.equals(song.albumId()))
                .findFirst()
                .orElseGet(() -> songDao.insert(new Song(null, songNameSource,  songNameStreaming, normalizeTrackNumber(trackNumber), albumId)));
    }

    private static int normalizeTrackNumber(Integer trackNumber) {
        return trackNumber != null && trackNumber > 0 ? trackNumber : 1;
    }

    private static void validateRequest(SongHistoryRegistrationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must be informed");
        }

        if (!StringUtils.hasText(request.musica()) || !StringUtils.hasText(request.artista())) {
            throw new IllegalArgumentException("musica and artista must be informed");
        }

        if (request.dataHora() == null && request.timestamp() == null) {
            throw new IllegalArgumentException("timestamp or data_hora must be informed");
        }
    }

    private static LocalDateTime resolvePlayedAt(SongHistoryRegistrationRequest request) {
        Instant source = request.dataHora() != null
                ? request.dataHora()
                : Instant.ofEpochSecond(request.timestamp());

        return LocalDateTime.ofInstant(source, ZoneOffset.UTC);
    }
}

