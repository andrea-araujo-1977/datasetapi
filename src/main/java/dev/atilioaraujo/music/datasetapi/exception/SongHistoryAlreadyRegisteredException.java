package dev.atilioaraujo.music.datasetapi.exception;

public class SongHistoryAlreadyRegisteredException extends RuntimeException {

    public SongHistoryAlreadyRegisteredException(String message) {
        super(message);
    }
}

