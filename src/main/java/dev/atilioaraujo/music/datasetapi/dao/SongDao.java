package dev.atilioaraujo.music.datasetapi.dao;

import dev.atilioaraujo.music.datasetapi.domain.Album;
import dev.atilioaraujo.music.datasetapi.domain.Artist;
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

    public record SongCatalogData(
            Artist artist,
            Album album,
            Song song
    ) {
    }

    private static final RowMapper<Song> SONG_ROW_MAPPER = SongDao::mapSong;
    private static final RowMapper<SongCatalogData> SONG_CATALOG_DATA_ROW_MAPPER = SongDao::mapSongCatalogData;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SongDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Song> findByNameIgnoreCase(String name) {
        String sql = """
                SELECT id_song, nm_song_source, nm_song_streaming, id_track_number, id_length_ms, id_album
                FROM song
                WHERE LOWER(nm_song_source) = LOWER(:name) OR LOWER(nm_song_streaming) = LOWER(:name)
                ORDER BY id_song
                """;

        return jdbcTemplate.query(sql, Map.of("name", name), SONG_ROW_MAPPER);
    }

    public List<SongCatalogData> findSongsWithLengthZero(int amount) {
        String sql = """
                SELECT a.id_artist,
                       a.nm_artist,
                       a.ds_genre,
                       a.ds_image_url,
                       al.id_album,
                       al.nm_album,
                       al.dt_release,
                       al.ds_cover_image_url,
                       al.id_artist AS album_id_artist,
                       s.id_song,
                       s.nm_song_source,
                       s.nm_song_streaming,
                       s.id_track_number,
                       s.id_length_ms,
                       s.id_album AS song_id_album
                FROM song s
                INNER JOIN album al ON al.id_album = s.id_album
                INNER JOIN artist a ON a.id_artist = al.id_artist
                WHERE s.id_length_ms = 0
                ORDER BY s.id_song
                LIMIT :amount
                """;

        return jdbcTemplate.query(sql, new MapSqlParameterSource().addValue("amount", amount), SONG_CATALOG_DATA_ROW_MAPPER);
    }

    public Song insert(Song song) {
        String sql = """
                INSERT INTO song (nm_song_source, nm_song_streaming, id_track_number, id_length_ms, id_album)
                VALUES (:nameSource, :nameStreaming, :trackNumber, :lengthMs, :albumId)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nameSource", song.nameSource())
                .addValue("nameStreaming", song.nameStreaming())
                .addValue("trackNumber", song.trackNumber())
                .addValue("lengthMs", song.lengthMs())
                .addValue("albumId", song.albumId());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id_song"});

        Number generatedId = keyHolder.getKey();
        Integer id = generatedId != null ? generatedId.intValue() : null;
        return new Song(id, song.nameStreaming(), song.nameStreaming(), song.trackNumber(), song.lengthMs(), song.albumId());
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
                    id_length_ms = :lengthMs,
                    id_album = :albumId
                WHERE id_song = :idSong
                """;

        int rows = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("idSong", song.idSong())
                .addValue("nameSource", song.nameSource())
                .addValue("nameStreaming", song.nameStreaming())
                .addValue("trackNumber", song.trackNumber())
                .addValue("lengthMs", song.lengthMs())
                .addValue("albumId", song.albumId()));

        return rows > 0;
    }

    public Integer getTotalCount() {
        String sql = "SELECT COUNT(*) as total FROM song";
        Integer count = jdbcTemplate.queryForObject(sql, Map.of(), Integer.class);
        return count != null ? count : 0;
    }

    public Integer getPendingLengthZeroCount() {
        String sql = "SELECT COUNT(*) as total FROM song WHERE id_length_ms = 0";
        Integer count = jdbcTemplate.queryForObject(sql, Map.of(), Integer.class);
        return count != null ? count : 0;
    }

    private static Song mapSong(ResultSet rs, int rowNum) throws SQLException {
        return new Song(
                rs.getInt("id_song"),
                rs.getString("nm_song_source"),
                rs.getString("nm_song_streaming"),
                rs.getInt("id_track_number"),
                (Integer) rs.getObject("id_length_ms"),
                rs.getInt("id_album")
        );
    }

    private static SongCatalogData mapSongCatalogData(ResultSet rs, int rowNum) throws SQLException {
        Artist artist = new Artist(
                rs.getInt("id_artist"),
                rs.getString("nm_artist"),
                rs.getString("ds_genre"),
                rs.getString("ds_image_url")
        );

        Album album = new Album(
                rs.getInt("id_album"),
                rs.getString("nm_album"),
                rs.getDate("dt_release") != null ? rs.getDate("dt_release").toLocalDate() : null,
                rs.getString("ds_cover_image_url"),
                rs.getInt("album_id_artist")
        );

        Song song = new Song(
                rs.getInt("id_song"),
                rs.getString("nm_song_source"),
                rs.getString("nm_song_streaming"),
                rs.getInt("id_track_number"),
                (Integer) rs.getObject("id_length_ms"),
                rs.getInt("song_id_album")
        );

        return new SongCatalogData(artist, album, song);
    }

}
