package dev.atilioaraujo.music.datasetapi.exception;

public class InvalidSongHistoryPayloadException extends RuntimeException {

    public InvalidSongHistoryPayloadException(String message) {
        super(message);
    }
}