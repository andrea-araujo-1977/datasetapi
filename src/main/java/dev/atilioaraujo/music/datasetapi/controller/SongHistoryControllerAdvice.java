package dev.atilioaraujo.music.datasetapi.controller;

import dev.atilioaraujo.music.datasetapi.exception.InvalidSongHistoryPayloadException;
import dev.atilioaraujo.music.datasetapi.exception.SongHistoryAlreadyRegisteredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class SongHistoryControllerAdvice {

    @ExceptionHandler(SongHistoryAlreadyRegisteredException.class)
    public ResponseEntity<Void> handleSongHistoryAlreadyRegistered() {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ExceptionHandler(InvalidSongHistoryPayloadException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPayload(InvalidSongHistoryPayloadException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(ex.getMessage()));
    }
}


