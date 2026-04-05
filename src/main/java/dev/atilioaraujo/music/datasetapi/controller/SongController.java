package dev.atilioaraujo.music.datasetapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SongController {

    @GetMapping("/song")
    public String song() {
        return "song";
    }

    @GetMapping("/song/history")
    public String songsHistory() {
        return "song-history";
    }
}

