package dev.atilioaraujo.music.datasetapi.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import se.michaelthelin.spotify.SpotifyApi;

import java.util.Arrays;

@Service
public class SpotifyCatalogLookupService {

    private final SpotifyApi spotifyApi;
    private final SpotifyAuthenticationService spotifyAuthenticationService;

    public SpotifyCatalogLookupService(SpotifyApi spotifyApi, SpotifyAuthenticationService spotifyAuthenticationService) {
        this.spotifyApi = spotifyApi;
        this.spotifyAuthenticationService = spotifyAuthenticationService;
    }

    public SpotifyTrackInfo findTrack(String artistName, String songName) {
        validateInput(artistName, songName);
        spotifyAuthenticationService.authenticateClientCredentials();

        String query = "track:" + songName + " artist:" + artistName;

        try {
            var paging = spotifyApi.searchTracks(query).limit(10).build().execute();
            var tracks = paging.getItems();

            if (tracks == null || tracks.length == 0) {
                throw new IllegalStateException("Track not found on Spotify for artist='" + artistName + "' and song='" + songName + "'");
            }

            var selectedTrack = Arrays.stream(tracks)
                    .filter(track -> hasArtist(track.getArtists(), artistName))
                    .findFirst()
                    .orElse(tracks[0]);

            String resolvedArtist = resolveArtistName(selectedTrack.getArtists(), artistName);
            String albumName = selectedTrack.getAlbum() != null ? selectedTrack.getAlbum().getName() : null;
            Integer trackNumber = selectedTrack.getTrackNumber();

            if (!StringUtils.hasText(resolvedArtist) || !StringUtils.hasText(albumName) || !StringUtils.hasText(selectedTrack.getName())) {
                throw new IllegalStateException("Spotify returned incomplete track metadata");
            }

            return new SpotifyTrackInfo(
                    selectedTrack.getName(),
                    resolvedArtist,
                    albumName,
                    trackNumber != null && trackNumber > 0 ? trackNumber : 1
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to query Spotify track metadata", ex);
        }
    }

    private static void validateInput(String artistName, String songName) {
        if (!StringUtils.hasText(artistName) || !StringUtils.hasText(songName)) {
            throw new IllegalArgumentException("artistName and songName must be informed");
        }
    }

    private static boolean hasArtist(Object[] artists, String targetArtistName) {
        if (artists == null || artists.length == 0) {
            return false;
        }

        return Arrays.stream(artists)
                .map(artist -> (se.michaelthelin.spotify.model_objects.specification.ArtistSimplified) artist)
                .anyMatch(artist -> artist != null && artist.getName() != null && artist.getName().equalsIgnoreCase(targetArtistName));
    }

    private static String resolveArtistName(Object[] artists, String targetArtistName) {
        if (artists == null || artists.length == 0) {
            return targetArtistName;
        }

        return Arrays.stream(artists)
                .map(artist -> (se.michaelthelin.spotify.model_objects.specification.ArtistSimplified) artist)
                .filter(artist -> artist != null && artist.getName() != null)
                .filter(artist -> artist.getName().equalsIgnoreCase(targetArtistName))
                .map(se.michaelthelin.spotify.model_objects.specification.ArtistSimplified::getName)
                .findFirst()
                .orElseGet(() -> {
                    var first = (se.michaelthelin.spotify.model_objects.specification.ArtistSimplified) artists[0];
                    return first != null ? first.getName() : targetArtistName;
                });
    }

    public record SpotifyTrackInfo(String songName, String artistName, String albumName, Integer trackNumber) {
    }
}

