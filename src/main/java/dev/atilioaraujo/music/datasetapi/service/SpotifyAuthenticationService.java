package dev.atilioaraujo.music.datasetapi.service;

import dev.atilioaraujo.music.datasetapi.configuration.SpotifyProperties;
import java.time.Clock;
import java.time.Instant;

import dev.atilioaraujo.music.datasetapi.domain.spotify.SpotifyToken;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;

@Service
public class SpotifyAuthenticationService {

    private final SpotifyApi spotifyApi;
    private final SpotifyProperties spotifyProperties;
    private final Clock clock = Clock.systemUTC();

    private volatile SpotifyToken cachedToken;

    public SpotifyAuthenticationService(SpotifyApi spotifyApi, SpotifyProperties spotifyProperties) {
        this.spotifyApi = spotifyApi;
        this.spotifyProperties = spotifyProperties;
    }

    public synchronized SpotifyToken authenticateClientCredentials() {
        if (isCachedTokenStillValid()) {
            return cachedToken;
        }

        validateCredentials();

        try {
            ClientCredentials clientCredentials = spotifyApi.clientCredentials().build().execute();
            Instant expiresAt = clock.instant().plusSeconds(clientCredentials.getExpiresIn());

            SpotifyToken token = new SpotifyToken(
                    clientCredentials.getAccessToken(),
                    clientCredentials.getTokenType(),
                    clientCredentials.getExpiresIn(),
                    expiresAt
            );

            spotifyApi.setAccessToken(token.accessToken());
            this.cachedToken = token;
            return token;
        } catch (Exception ex) {
            throw new IllegalStateException("Spotify client-credentials authentication failed", ex);
        }
    }

    public String getAccessToken() {
        return authenticateClientCredentials().accessToken();
    }

    private boolean isCachedTokenStillValid() {
        if (cachedToken == null) {
            return false;
        }

        long skewSeconds = Math.max(0, spotifyProperties.getTokenRefreshSkewSeconds());
        Instant effectiveExpiry = cachedToken.expiresAt().minusSeconds(skewSeconds);
        return clock.instant().isBefore(effectiveExpiry);
    }

    private void validateCredentials() {
        if (!StringUtils.hasText(spotifyProperties.getClientId()) || !StringUtils.hasText(spotifyProperties.getClientSecret())) {
            throw new IllegalStateException(
                    "Spotify credentials are missing. Configure SPOTIFY_CLIENT_ID and SPOTIFY_CLIENT_SECRET environment variables."
            );
        }
    }

}


