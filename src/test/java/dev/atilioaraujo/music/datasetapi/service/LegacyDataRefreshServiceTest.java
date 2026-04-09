package dev.atilioaraujo.music.datasetapi.service;

import dev.atilioaraujo.music.datasetapi.dao.AlbumDao;
import dev.atilioaraujo.music.datasetapi.dao.ArtistDao;
import dev.atilioaraujo.music.datasetapi.dao.SongDao;
import dev.atilioaraujo.music.datasetapi.dao.SongDao.SongCatalogData;
import dev.atilioaraujo.music.datasetapi.domain.Album;
import dev.atilioaraujo.music.datasetapi.domain.Artist;
import dev.atilioaraujo.music.datasetapi.domain.Song;
import dev.atilioaraujo.music.datasetapi.dto.LegacyArtistRefreshResponse;
import dev.atilioaraujo.music.datasetapi.dto.LegacyDataRefreshResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LegacyDataRefreshServiceTest {

    @Mock
    private SongDao songDao;

    @Mock
    private AlbumDao albumDao;

    @Mock
    private ArtistDao artistDao;

    @Mock
    private SpotifyCatalogLookupService spotifyCatalogLookupService;

    @InjectMocks
    private LegacyDataRefreshService service;

    @Test
    void shouldRefreshSongsAndReturnRemainingCount() {
        SongCatalogData catalogData = new SongCatalogData(
                new Artist(11, "Bush", null, null),
                new Album(12, "Sixteen Stone", null, null, 11),
                new Song(13, "MACHINE HEAD", null, 3, 0, 12)
        );

        when(songDao.findSongsWithLengthZero(2)).thenReturn(List.of(catalogData));
        when(spotifyCatalogLookupService.findTrack("Bush", "MACHINE HEAD"))
                .thenReturn(new SpotifyCatalogLookupService.SpotifyTrackInfo(
                        "Machine Head",
                        "Bush",
                        "grunge/alternative rock",
                        "https://example.com/artist.png",
                        "Sixteen Stone",
                        LocalDate.of(1994, 11, 1),
                        "https://example.com/cover.png",
                        3,
                        312000
                ));
        when(songDao.getPendingLengthZeroCount()).thenReturn(4);

        LegacyDataRefreshResponse response = service.refresh(2);

        assertEquals(1, response.totalUpdatedSongs());
        assertEquals(4, response.pendingSongs());
        verify(artistDao).update(any(Artist.class));
        verify(albumDao).update(any(Album.class));
        verify(songDao).update(any(Song.class));
    }

    @Test
    void shouldRejectNonPositiveAmount() {
        assertThrows(IllegalArgumentException.class, () -> service.refresh(0));
        verifyNoInteractions(songDao, albumDao, artistDao, spotifyCatalogLookupService);
    }

    @Test
    void shouldContinueProcessingWhenSpotifyTrackIsNotFound() {
        SongCatalogData firstSong = new SongCatalogData(
                new Artist(11, "Unknown Artist", null, null),
                new Album(12, "Unknown Album", null, null, 11),
                new Song(13, "NOT_FOUND_TRACK", null, 1, 0, 12)
        );

        SongCatalogData secondSong = new SongCatalogData(
                new Artist(21, "Bush", null, null),
                new Album(22, "Sixteen Stone", null, null, 21),
                new Song(23, "MACHINE HEAD", null, 3, 0, 22)
        );

        when(songDao.findSongsWithLengthZero(2)).thenReturn(List.of(firstSong, secondSong));

        when(spotifyCatalogLookupService.findTrack(eq("Unknown Artist"), eq("NOT_FOUND_TRACK")))
                .thenThrow(new IllegalStateException("Track not found"));

        when(spotifyCatalogLookupService.findTrack(eq("Bush"), eq("MACHINE HEAD")))
                .thenReturn(new SpotifyCatalogLookupService.SpotifyTrackInfo(
                        "Machine Head",
                        "Bush",
                        "grunge/alternative rock",
                        "https://example.com/artist.png",
                        "Sixteen Stone",
                        LocalDate.of(1994, 11, 1),
                        "https://example.com/cover.png",
                        3,
                        312000
                ));

        when(songDao.getPendingLengthZeroCount()).thenReturn(1);

        LegacyDataRefreshResponse response = service.refresh(2);

        assertEquals(1, response.totalUpdatedSongs());
        assertEquals(1, response.pendingSongs());
        verify(songDao, times(1)).findSongsWithLengthZero(2);
        verify(songDao, times(1)).update(any(Song.class));
    }

    @Test
    void shouldRefreshArtistsAndReturnRemainingCount() {
        when(artistDao.findArtistsMissingMetadata(2)).thenReturn(List.of(
                new Artist(11, "Bush", null, null),
                new Artist(21, "Pearl Jam", "grunge", "https://example.com/existing.png")
        ));

        when(spotifyCatalogLookupService.findArtist("Bush"))
                .thenReturn(new SpotifyCatalogLookupService.SpotifyArtistInfo(
                        "Bush",
                        "grunge/alternative rock",
                        "https://example.com/bush.png"
                ));
        when(spotifyCatalogLookupService.findArtist("Pearl Jam"))
                .thenReturn(new SpotifyCatalogLookupService.SpotifyArtistInfo(
                        "Pearl Jam",
                        "grunge",
                        "https://example.com/existing.png"
                ));
        when(artistDao.getPendingMetadataCount()).thenReturn(8);

        LegacyArtistRefreshResponse response = service.refreshArtists(2);

        assertEquals(1, response.totalUpdatedArtists());
        assertEquals(8, response.pendingArtists());
        verify(artistDao, times(1)).update(any(Artist.class));
    }
}


