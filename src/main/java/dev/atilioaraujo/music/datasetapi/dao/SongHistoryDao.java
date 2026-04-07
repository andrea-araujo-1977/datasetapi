package dev.atilioaraujo.music.datasetapi.dao;

import dev.atilioaraujo.music.datasetapi.domain.SongHistory;
import dev.atilioaraujo.music.datasetapi.dto.SongHistoryDto;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class SongHistoryDao {

    private static final RowMapper<SongHistory> SONG_HISTORY_ROW_MAPPER = SongHistoryDao::mapSongHistory;
    private static final RowMapper<SongHistoryDto> SONG_HISTORY_DTO_ROW_MAPPER = SongHistoryDao::mapSongHistoryDto;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SongHistoryDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SongHistory insert(SongHistory songHistory) {
        String sql = """
                INSERT INTO song_history (id_song, dt_song_played)
                VALUES (:songId, :playedAt)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("songId", songHistory.songId())
                .addValue("playedAt", songHistory.playedAt());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id_song_history"});

        Number generatedId = keyHolder.getKey();
        Integer id = generatedId != null ? generatedId.intValue() : null;
        return new SongHistory(id, songHistory.songId(), songHistory.playedAt());
    }

    public List<SongHistory> findByPlayedDateAndSongNameIgnoreCase(LocalDateTime playedDateTime, String songName) {
        String sql = """
                SELECT sh.id_song_history, sh.id_song, sh.dt_song_played
                FROM song_history sh
                INNER JOIN song s ON s.id_song = sh.id_song
                WHERE sh.dt_song_played = :playedDateTime
                  AND (LOWER(s.nm_song_source) = LOWER(:songName) OR LOWER(s.nm_song_streaming) = LOWER(:songName))
                ORDER BY sh.dt_song_played
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("playedDateTime", Timestamp.valueOf(playedDateTime))
                .addValue("songName", songName);

        return jdbcTemplate.query(sql, params, SONG_HISTORY_ROW_MAPPER);
    }

    public LocalDateTime getOldestPlayedDate() {
        String sql = """
                SELECT MIN(dt_song_played) as oldest_date
                FROM song_history
                """;

        Timestamp result = jdbcTemplate.queryForObject(sql, Map.of(), Timestamp.class);
        return result != null ? result.toLocalDateTime() : null;
    }

    public List<SongHistoryDto> getLastFivePlayedSongs() {
        String sql = """
                SELECT sh.id_song_history,
                       sh.dt_song_played,
                       a.id_artist,
                       a.nm_artist,
                       al.id_album,
                       al.nm_album,
                       s.id_song,
                       s.nm_song_source,
                       s.nm_song_streaming
                FROM song_history sh
                INNER JOIN song s ON s.id_song = sh.id_song
                INNER JOIN album al ON al.id_album = s.id_album
                INNER JOIN artist a ON a.id_artist = al.id_artist
                ORDER BY sh.dt_song_played DESC
                LIMIT 5
                """;

        return jdbcTemplate.query(sql, Map.of(), SONG_HISTORY_DTO_ROW_MAPPER);
    }

    private static SongHistory mapSongHistory(ResultSet rs, int rowNum) throws SQLException {
        Timestamp playedAt = rs.getTimestamp("dt_song_played");

        return new SongHistory(
                rs.getInt("id_song_history"),
                rs.getInt("id_song"),
                playedAt != null ? playedAt.toLocalDateTime() : null
        );
    }

    private static SongHistoryDto mapSongHistoryDto(ResultSet rs, int rowNum) throws SQLException {
        Timestamp playedAt = rs.getTimestamp("dt_song_played");

        return new SongHistoryDto(
                rs.getInt("id_artist"),
                rs.getString("nm_artist"),
                rs.getInt("id_album"),
                rs.getString("nm_album"),
                rs.getInt("id_song"),
                rs.getString("nm_song_source"),
                rs.getString("nm_song_streaming"),
                playedAt != null ? playedAt.toLocalDateTime() : null
        );
    }
}
