package dev.atilioaraujo.music.datasetapi.service;

import dev.atilioaraujo.music.datasetapi.configuration.SpotifyProperties;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.SpotifyApi;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SpotifyAuthenticationServiceTest {

    @Test
    void shouldFailFastWhenSpotifyCredentialsAreMissing() {
        SpotifyProperties spotifyProperties = new SpotifyProperties();
        spotifyProperties.setClientId(" ");
        spotifyProperties.setClientSecret("");

        SpotifyApi spotifyApi = new SpotifyApi.Builder().build();
        SpotifyAuthenticationService service = new SpotifyAuthenticationService(spotifyApi, spotifyProperties);

        assertThrows(IllegalStateException.class, service::authenticateClientCredentials);
    }
}

