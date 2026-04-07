package dev.atilioaraujo.music.datasetapi.configuration;

import dev.atilioaraujo.music.datasetapi.service.SpotifyAuthenticationService;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "app.startup.spotify-auth.enabled", havingValue = "true", matchIfMissing = true)
public class StartupActions implements CommandLineRunner {

    private final SpotifyAuthenticationService spotifyAuthenticationService;

    public StartupActions(SpotifyAuthenticationService spotifyAuthenticationService) {
        this.spotifyAuthenticationService = spotifyAuthenticationService;
    }

    @Override
    public void run(String @NonNull ... args) {
        spotifyAuthenticationService.authenticateClientCredentials();
    }
}
