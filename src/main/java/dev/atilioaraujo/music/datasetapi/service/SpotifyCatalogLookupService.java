package dev.atilioaraujo.music.datasetapi.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.stream.Collectors;

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

            Track selectedTrack = Arrays.stream(tracks)
                    .filter(track -> hasArtist(track.getArtists(), artistName))
                    .findFirst()
                    .orElse(tracks[0]);

            String resolvedArtist = resolveArtistName(selectedTrack.getArtists(), artistName);
            String albumName = selectedTrack.getAlbum() != null ? selectedTrack.getAlbum().getName() : null;
            Integer trackNumber = selectedTrack.getTrackNumber();
            Integer lengthMs = selectedTrack.getDurationMs();
            LocalDate albumReleaseDate = resolveReleaseDate(selectedTrack);
            String albumCoverImageUrl = resolveAlbumCoverImageUrl(selectedTrack);
            ArtistMetadata artistMetadata = resolveArtistMetadata(selectedTrack.getArtists(), artistName);

            if (!StringUtils.hasText(resolvedArtist) || !StringUtils.hasText(albumName) || !StringUtils.hasText(selectedTrack.getName())) {
                throw new IllegalStateException("Spotify returned incomplete track metadata");
            }

            return new SpotifyTrackInfo(
                    selectedTrack.getName(),
                    resolvedArtist,
                    artistMetadata.genre(),
                    artistMetadata.imageUrl(),
                    albumName,
                    albumReleaseDate,
                    albumCoverImageUrl,
                    trackNumber != null && trackNumber > 0 ? trackNumber : 1,
                    lengthMs
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to query Spotify track metadata", ex);
        }
    }

    public SpotifyArtistInfo findArtist(String artistName) {
        validateInput(artistName, "artist");
        spotifyAuthenticationService.authenticateClientCredentials();

        String query = "artist:" + artistName;

        try {
            var paging = spotifyApi.searchArtists(query).limit(10).build().execute();
            var artists = paging.getItems();

            if (artists == null || artists.length == 0) {
                throw new IllegalStateException("Artist not found on Spotify for artist='" + artistName + "'");
            }

            Artist selectedArtist = Arrays.stream(artists)
                    .filter(artist -> artist != null && artist.getName() != null && artist.getName().equalsIgnoreCase(artistName))
                    .findFirst()
                    .orElse(artists[0]);

            String resolvedName = selectedArtist != null ? selectedArtist.getName() : artistName;
            String genre = selectedArtist != null ? resolveGenre(selectedArtist) : null;
            String imageUrl = selectedArtist != null ? resolveArtistImageUrl(selectedArtist) : null;

            return new SpotifyArtistInfo(resolvedName, genre, imageUrl);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to query Spotify artist metadata", ex);
        }
    }

    private static void validateInput(String artistName, String songName) {
        if (!StringUtils.hasText(artistName) || !StringUtils.hasText(songName)) {
            throw new IllegalArgumentException("artistName and songName must be informed");
        }
    }

    private ArtistMetadata resolveArtistMetadata(ArtistSimplified[] artists, String targetArtistName) {
        String artistId = resolveArtistId(artists, targetArtistName);
        if (!StringUtils.hasText(artistId)) {
            return new ArtistMetadata(null, null);
        }

        try {
            Artist fullArtist = spotifyApi.getArtist(artistId).build().execute();
            if (fullArtist == null) {
                return new ArtistMetadata(null, null);
            }

            String genre = resolveGenre(fullArtist);
            String imageUrl = resolveArtistImageUrl(fullArtist);
            return new ArtistMetadata(genre, imageUrl);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to query Spotify artist metadata", ex);
        }
    }

    private static String resolveGenre(Artist fullArtist) {
        if (fullArtist.getGenres() == null || fullArtist.getGenres().length == 0) {
            return null;
        }

        return Arrays.stream(fullArtist.getGenres())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("/"));
    }

    private static String resolveArtistImageUrl(Artist fullArtist) {
        if (fullArtist.getImages() == null || fullArtist.getImages().length == 0) {
            return null;
        }

        return Arrays.stream(fullArtist.getImages())
                .filter(image -> image != null && StringUtils.hasText(image.getUrl()))
                .map(image -> image.getUrl())
                .findFirst()
                .orElse(null);
    }

    private static boolean hasArtist(ArtistSimplified[] artists, String targetArtistName) {
        if (artists == null || artists.length == 0) {
            return false;
        }

        return Arrays.stream(artists)
                .anyMatch(artist -> artist != null && artist.getName() != null && artist.getName().equalsIgnoreCase(targetArtistName));
    }

    private static String resolveArtistName(ArtistSimplified[] artists, String targetArtistName) {
        if (artists == null || artists.length == 0) {
            return targetArtistName;
        }

        return Arrays.stream(artists)
                .filter(artist -> artist != null && artist.getName() != null)
                .filter(artist -> artist.getName().equalsIgnoreCase(targetArtistName))
                .map(ArtistSimplified::getName)
                .findFirst()
                .orElseGet(() -> {
                    ArtistSimplified first = artists[0];
                    return first != null ? first.getName() : targetArtistName;
                });
    }

    private static String resolveArtistId(ArtistSimplified[] artists, String targetArtistName) {
        if (artists == null || artists.length == 0) {
            return null;
        }

        return Arrays.stream(artists)
                .filter(artist -> artist != null && StringUtils.hasText(artist.getId()))
                .filter(artist -> artist.getName() != null && artist.getName().equalsIgnoreCase(targetArtistName))
                .map(ArtistSimplified::getId)
                .findFirst()
                .orElseGet(() -> {
                    ArtistSimplified first = artists[0];
                    return first != null ? first.getId() : null;
                });
    }

    private static LocalDate resolveReleaseDate(Track selectedTrack) {
        if (selectedTrack.getAlbum() == null || !StringUtils.hasText(selectedTrack.getAlbum().getReleaseDate())) {
            return null;
        }

        String releaseDate = selectedTrack.getAlbum().getReleaseDate();
        var precision = selectedTrack.getAlbum().getReleaseDatePrecision();

        try {
            if (precision != null && "YEAR".equalsIgnoreCase(precision.name())) {
                return Year.parse(releaseDate).atDay(1);
            }
            if (precision != null && "MONTH".equalsIgnoreCase(precision.name())) {
                return YearMonth.parse(releaseDate).atDay(1);
            }
            return LocalDate.parse(releaseDate);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String resolveAlbumCoverImageUrl(Track selectedTrack) {
        if (selectedTrack.getAlbum() == null || selectedTrack.getAlbum().getImages() == null || selectedTrack.getAlbum().getImages().length == 0) {
            return null;
        }

        return Arrays.stream(selectedTrack.getAlbum().getImages())
                .filter(image -> image != null && StringUtils.hasText(image.getUrl()))
                .map(image -> image.getUrl())
                .findFirst()
                .orElse(null);
    }

    public record SpotifyTrackInfo(
            String songName,
            String artistName,
            String artistGenre,
            String artistImageUrl,
            String albumName,
            LocalDate albumReleaseDate,
            String albumCoverImageUrl,
            Integer trackNumber,
            Integer lengthMs
    ) {
    }

    public record SpotifyArtistInfo(
            String artistName,
            String artistGenre,
            String artistImageUrl
    ) {
    }

    private record ArtistMetadata(String genre, String imageUrl) {
    }
}

