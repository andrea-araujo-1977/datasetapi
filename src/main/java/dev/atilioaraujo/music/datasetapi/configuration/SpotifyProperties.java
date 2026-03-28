package dev.atilioaraujo.music.datasetapi.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spotify")
public class SpotifyProperties {

    private String clientId;
    private String clientSecret;
    private long tokenRefreshSkewSeconds = 30;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public long getTokenRefreshSkewSeconds() {
        return tokenRefreshSkewSeconds;
    }

    public void setTokenRefreshSkewSeconds(long tokenRefreshSkewSeconds) {
        this.tokenRefreshSkewSeconds = tokenRefreshSkewSeconds;
    }
}

