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
import dev.atilioaraujo.music.datasetapi.exception.SongHistoryAlreadyRegisteredException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SongHistoryRegistrationServiceTest {

    @Mock
    private SongHistoryDao songHistoryDao;

    @Mock
    private SongDao songDao;

    @Mock
    private AlbumDao albumDao;

    @Mock
    private ArtistDao artistDao;

    @Mock
    private SpotifyCatalogLookupService spotifyCatalogLookupService;

    @InjectMocks
    private SongHistoryRegistrationService service;

    @Test
    void shouldThrowWhenSongAlreadyRegisteredOnDate() {
        SongHistoryRegistrationRequest request = new SongHistoryRegistrationRequest(
                "MACHINE HEAD",
                "BUSH",
                1774394786L,
                Instant.parse("2026-03-24T23:26:26Z")
        );

        when(songHistoryDao.findByPlayedDateAndSongNameIgnoreCase(LocalDateTime.parse("2026-03-24T23:26:26"), "MACHINE HEAD"))
                .thenReturn(List.of(new SongHistory(1, 10, LocalDateTime.parse("2026-03-24T23:26:26"))));

        assertThrows(SongHistoryAlreadyRegisteredException.class, () -> service.register(request));
        verify(spotifyCatalogLookupService, never()).findTrack(anyString(), anyString());
    }

    @Test
    void shouldRegisterHistoryCreatingCatalogDataWhenNeeded() {
        SongHistoryRegistrationRequest request = new SongHistoryRegistrationRequest(
                "MACHINE HEAD",
                "BUSH",
                1774394786L,
                Instant.parse("2026-03-24T23:26:26Z")
        );

        when(songHistoryDao.findByPlayedDateAndSongNameIgnoreCase(LocalDateTime.parse("2026-03-24T23:26:26"), "MACHINE HEAD"))
                .thenReturn(List.of());

        when(spotifyCatalogLookupService.findTrack("BUSH", "MACHINE HEAD"))
                .thenReturn(new SpotifyCatalogLookupService.SpotifyTrackInfo(
                        "Machinehead",
                        "Bush",
                        "grunge/alternative rock",
                        "Sixteen Stone",
                        null,
                        "https://example.com/cover.png",
                        3,
                        312000
                ));

        when(artistDao.findByNameIgnoreCase("Bush")).thenReturn(List.of());
        when(artistDao.insert(any(Artist.class))).thenReturn(new Artist(11, "Bush", "grunge/alternative rock"));

        when(albumDao.findByNameIgnoreCase("Sixteen Stone")).thenReturn(List.of());
        when(albumDao.insert(any(Album.class))).thenReturn(new Album(12, "Sixteen Stone", null, "https://example.com/cover.png", 11));

        when(songDao.findByNameIgnoreCase("MACHINE HEAD")).thenReturn(List.of());
        when(songDao.insert(any(Song.class))).thenReturn(new Song(13, "MACHINE HEAD", "Machinehead", 3, 312000, 12));

        when(songHistoryDao.insert(any(SongHistory.class)))
                .thenReturn(new SongHistory(20, 13, LocalDateTime.parse("2026-03-24T23:26:26")));

        SongHistory created = service.register(request);

        assertEquals(20, created.idSongHistory());
        assertEquals(13, created.songId());
        verify(artistDao).insert(any(Artist.class));
        verify(albumDao).insert(any(Album.class));
        verify(songDao).insert(any(Song.class));
    }
}

