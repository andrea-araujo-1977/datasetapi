package dev.atilioaraujo.music.datasetapi.controller;

import dev.atilioaraujo.music.datasetapi.controller.payload.LegacyDataRefreshRequest;
import dev.atilioaraujo.music.datasetapi.dto.LegacyDataRefreshResponse;
import dev.atilioaraujo.music.datasetapi.exception.InvalidLegacyDataRefreshRequestException;
import dev.atilioaraujo.music.datasetapi.service.LegacyDataRefreshService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/data")
public class LegacyDataController {

    private final LegacyDataRefreshService legacyDataRefreshService;

    public LegacyDataController(LegacyDataRefreshService legacyDataRefreshService) {
        this.legacyDataRefreshService = legacyDataRefreshService;
    }

    @PutMapping("/refresh")
    public ResponseEntity<LegacyDataRefreshResponse> refresh(@RequestBody(required = false) LegacyDataRefreshRequest request) {
        if (Objects.isNull(request) || Objects.isNull(request.amount())) {
            throw new InvalidLegacyDataRefreshRequestException("amount must be informed");
        }

        LegacyDataRefreshResponse response = legacyDataRefreshService.refresh(request.amount());
        return ResponseEntity.ok(response);
    }
}

