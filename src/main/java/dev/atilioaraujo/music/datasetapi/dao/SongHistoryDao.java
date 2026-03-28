package dev.atilioaraujo.music.datasetapi.dao;

import dev.atilioaraujo.music.datasetapi.domain.SongHistory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class SongHistoryDao {

    private static final RowMapper<SongHistory> SONG_HISTORY_ROW_MAPPER = SongHistoryDao::mapSongHistory;

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

    private static SongHistory mapSongHistory(ResultSet rs, int rowNum) throws SQLException {
        Timestamp playedAt = rs.getTimestamp("dt_song_played");

        return new SongHistory(
                rs.getInt("id_song_history"),
                rs.getInt("id_song"),
                playedAt != null ? playedAt.toLocalDateTime() : null
        );
    }
}

