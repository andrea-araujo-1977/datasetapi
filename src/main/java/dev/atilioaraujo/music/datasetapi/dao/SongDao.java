package dev.atilioaraujo.music.datasetapi.dao;

import dev.atilioaraujo.music.datasetapi.domain.Song;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class SongDao {

    private static final RowMapper<Song> SONG_ROW_MAPPER = SongDao::mapSong;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SongDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Song> findByNameIgnoreCase(String name) {
        String sql = """
                SELECT id_song, nm_song_source, nm_song_streaming, id_track_number, id_album
                FROM song
                WHERE LOWER(nm_song_source) = LOWER(:name) OR LOWER(nm_song_streaming) = LOWER(:name)
                ORDER BY id_song
                """;

        return jdbcTemplate.query(sql, Map.of("name", name), SONG_ROW_MAPPER);
    }

    public Song insert(Song song) {
        String sql = """
                INSERT INTO song (nm_song_source, nm_song_streaming, id_track_number, id_album)
                VALUES (:nameSource, :nameStreaming, :trackNumber, :albumId)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nameSource", song.nameSource())
                .addValue("nameStreaming", song.nameStreaming())
                .addValue("trackNumber", song.trackNumber())
                .addValue("albumId", song.albumId());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id_song"});

        Number generatedId = keyHolder.getKey();
        Integer id = generatedId != null ? generatedId.intValue() : null;
        return new Song(id, song.nameStreaming(), song.nameStreaming(), song.trackNumber(), song.albumId());
    }

    public boolean update(Song song) {
        if (song.idSong() == null) {
            throw new IllegalArgumentException("song.idSong must be informed for update");
        }

        String sql = """
                UPDATE song
                SET nm_song_source = :nameSource,
                    nm_song_streaming = :nameStreaming,
                    id_track_number = :trackNumber,
                    id_album = :albumId
                WHERE id_song = :idSong
                """;

        int rows = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("idSong", song.idSong())
                .addValue("nameSource", song.nameSource())
                .addValue("nameStreaming", song.nameStreaming())
                .addValue("trackNumber", song.trackNumber())
                .addValue("albumId", song.albumId()));

        return rows > 0;
    }

    public Integer getTotalCount() {
        String sql = "SELECT COUNT(*) as total FROM song";
        Integer count = jdbcTemplate.queryForObject(sql, Map.of(), Integer.class);
        return count != null ? count : 0;
    }

    private static Song mapSong(ResultSet rs, int rowNum) throws SQLException {
        return new Song(
                rs.getInt("id_song"),
                rs.getString("nm_song_source"),
                rs.getString("nm_song_streaming"),
                rs.getInt("id_track_number"),
                rs.getInt("id_album")
        );
    }
}
