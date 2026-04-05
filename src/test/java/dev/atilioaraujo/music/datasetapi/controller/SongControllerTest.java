package dev.atilioaraujo.music.datasetapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SongControllerTest {

    private SongController controller;

    @BeforeEach
    void setUp() {
        controller = new SongController();
    }

    @Test
    void shouldLoadSongPage() {
        assertEquals("song", controller.song());
    }

    @Test
    void shouldLoadSongHistoryPage() {
        assertEquals("song-history", controller.songsHistory());
    }
}

