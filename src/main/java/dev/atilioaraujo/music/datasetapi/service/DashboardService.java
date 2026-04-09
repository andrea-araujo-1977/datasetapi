package dev.atilioaraujo.music.datasetapi.service;

import dev.atilioaraujo.music.datasetapi.dao.AlbumDao;
import dev.atilioaraujo.music.datasetapi.dao.ArtistDao;
import dev.atilioaraujo.music.datasetapi.dao.SongDao;
import dev.atilioaraujo.music.datasetapi.dao.SongHistoryDao;
import dev.atilioaraujo.music.datasetapi.domain.DashboardData;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final ArtistDao artistDao;
    private final AlbumDao albumDao;
    private final SongDao songDao;
    private final SongHistoryDao songHistoryDao;

    public DashboardService(ArtistDao artistDao, AlbumDao albumDao, SongDao songDao, SongHistoryDao songHistoryDao) {
        this.artistDao = artistDao;
        this.albumDao = albumDao;
        this.songDao = songDao;
        this.songHistoryDao = songHistoryDao;
    }

    public DashboardData getDashboardData() {
        Integer totalArtists = artistDao.getTotalCount();
        Integer totalAlbums = albumDao.getTotalCount();
        Integer totalSongs = songDao.getTotalCount();
        Integer totalSongHistory = songHistoryDao.getTotalCount();

        var oldestPlayedDate = songHistoryDao.getOldestPlayedDate();
        var lastFivePlayedSongs = songHistoryDao.getLastFivePlayedSongs();

        return new DashboardData(
                totalArtists,
                totalAlbums,
                totalSongs,
                totalSongHistory,
                oldestPlayedDate,
                lastFivePlayedSongs
        );
    }
}

