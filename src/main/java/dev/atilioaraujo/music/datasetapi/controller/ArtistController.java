package dev.atilioaraujo.music.datasetapi.controller;

import dev.atilioaraujo.music.datasetapi.dao.ArtistDao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ArtistController {

    private final ArtistDao artistDao;

    public ArtistController(ArtistDao artistDao) {
        this.artistDao = artistDao;
    }

    @GetMapping("/artist")
    public String artist(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model
    ) {
        int currentPage = Math.max(page, 1);
        int pageSize = Math.max(size, 1);

        var artistViews = artistDao.findTopArtistViews(currentPage, pageSize);
        model.addAttribute("artistViews", artistViews);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalArtists", artistDao.getTotalCount());
        return "artist";
    }
}

