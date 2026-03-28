package dev.atilioaraujo.music.datasetapi.controller;

import dev.atilioaraujo.music.datasetapi.controller.payload.SongHistoryRegistrationRequest;
import dev.atilioaraujo.music.datasetapi.exception.InvalidSongHistoryPayloadException;
import dev.atilioaraujo.music.datasetapi.service.SongHistoryRegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/song/history")
public class SongHistoryController {

    private final SongHistoryRegistrationService songHistoryRegistrationService;

    public SongHistoryController(SongHistoryRegistrationService songHistoryRegistrationService) {
        this.songHistoryRegistrationService = songHistoryRegistrationService;
    }

    @PostMapping
    public ResponseEntity<Void> addSongHistory(@RequestBody(required = false) SongHistoryRegistrationRequest payload) {
        if (Objects.isNull(payload)) {
            throw new InvalidSongHistoryPayloadException("Payload cannot be null");
        }

        songHistoryRegistrationService.register(payload);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}



