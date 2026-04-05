package dev.atilioaraujo.music.datasetapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ArtistController {

    @GetMapping("/artist")
    public String artist() {
        return "artist";
    }
}

