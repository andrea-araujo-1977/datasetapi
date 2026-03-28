package dev.atilioaraujo.music.datasetapi.domain.spotify;

import java.time.Instant;

public record SpotifyToken(String accessToken, String tokenType, Integer expiresInSeconds, Instant expiresAt) {
}
