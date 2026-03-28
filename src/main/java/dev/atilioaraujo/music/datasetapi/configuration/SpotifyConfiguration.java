package dev.atilioaraujo.music.datasetapi.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.michaelthelin.spotify.SpotifyApi;

@Configuration
@EnableConfigurationProperties(SpotifyProperties.class)
public class SpotifyConfiguration {

    @Bean
    public SpotifyApi spotifyApi(SpotifyProperties spotifyProperties) {
        return new SpotifyApi.Builder()
                .setClientId(spotifyProperties.getClientId())
                .setClientSecret(spotifyProperties.getClientSecret())
                .build();
    }
}

