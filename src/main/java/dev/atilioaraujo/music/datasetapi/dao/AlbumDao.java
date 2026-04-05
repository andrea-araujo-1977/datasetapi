package dev.atilioaraujo.music.datasetapi.dao;

import dev.atilioaraujo.music.datasetapi.domain.Album;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public class AlbumDao {

    private static final RowMapper<Album> ALBUM_ROW_MAPPER = AlbumDao::mapAlbum;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AlbumDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Album> findByNameIgnoreCase(String name) {
        String sql = """
                SELECT id_album, nm_album, dt_release, id_artist
                FROM album
                WHERE LOWER(nm_album) = LOWER(:name)
                ORDER BY id_album
                """;

        return jdbcTemplate.query(sql, Map.of("name", name), ALBUM_ROW_MAPPER);
    }

    public Album insert(Album album) {
        String sql = """
                INSERT INTO album (nm_album, dt_release, id_artist)
                VALUES (:name, :releaseDate, :artistId)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", album.name())
                .addValue("releaseDate", album.releaseDate())
                .addValue("artistId", album.artistId());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id_album"});

        Number generatedId = keyHolder.getKey();
        Integer id = generatedId != null ? generatedId.intValue() : null;
        return new Album(id, album.name(), album.releaseDate(), album.artistId());
    }

    public boolean update(Album album) {
        if (album.idAlbum() == null) {
            throw new IllegalArgumentException("album.idAlbum must be informed for update");
        }

        String sql = """
                UPDATE album
                SET nm_album = :name,
                    dt_release = :releaseDate,
                    id_artist = :artistId
                WHERE id_album = :idAlbum
                """;

        int rows = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("idAlbum", album.idAlbum())
                .addValue("name", album.name())
                .addValue("releaseDate", album.releaseDate())
                .addValue("artistId", album.artistId()));

        return rows > 0;
    }

    public Integer getTotalCount() {
        String sql = "SELECT COUNT(*) as total FROM album";
        Integer count = jdbcTemplate.queryForObject(sql, Map.of(), Integer.class);
        return count != null ? count : 0;
    }

    private static Album mapAlbum(ResultSet rs, int rowNum) throws SQLException {
        Date releaseDateValue = rs.getDate("dt_release");
        LocalDate releaseDate = releaseDateValue != null ? releaseDateValue.toLocalDate() : null;

        return new Album(
                rs.getInt("id_album"),
                rs.getString("nm_album"),
                releaseDate,
                rs.getInt("id_artist")
        );
    }
}
