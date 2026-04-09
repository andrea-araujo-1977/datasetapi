package dev.atilioaraujo.music.datasetapi.controller;

import dev.atilioaraujo.music.datasetapi.exception.InvalidLegacyDataRefreshRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = LegacyDataController.class)
public class LegacyDataControllerAdvice {

    @ExceptionHandler(InvalidLegacyDataRefreshRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(InvalidLegacyDataRefreshRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(ex.getMessage()));
    }
}


