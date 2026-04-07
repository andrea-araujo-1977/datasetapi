package dev.atilioaraujo.music.datasetapi.dao;

import dev.atilioaraujo.music.datasetapi.domain.Artist;
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
public class ArtistDao {

    private static final RowMapper<Artist> ARTIST_ROW_MAPPER = ArtistDao::mapArtist;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ArtistDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Artist> findByNameIgnoreCase(String name) {
        String sql = """
                SELECT id_artist, nm_artist, ds_genre
                FROM artist
                WHERE LOWER(nm_artist) = LOWER(:name)
                ORDER BY id_artist
                """;

        return jdbcTemplate.query(sql, Map.of("name", name), ARTIST_ROW_MAPPER);
    }

    public Artist insert(Artist artist) {
        String sql = """
                INSERT INTO artist (nm_artist, ds_genre)
                VALUES (:name, :genre)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", artist.name())
                .addValue("genre", artist.genre());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id_artist"});

        Number generatedId = keyHolder.getKey();
        Integer id = generatedId != null ? generatedId.intValue() : null;
        return new Artist(id, artist.name(), artist.genre());
    }

    public boolean update(Artist artist) {
        if (artist.idArtist() == null) {
            throw new IllegalArgumentException("artist.idArtist must be informed for update");
        }

        String sql = """
                UPDATE artist
                SET nm_artist = :name,
                    ds_genre = :genre
                WHERE id_artist = :idArtist
                """;

        int rows = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("idArtist", artist.idArtist())
                .addValue("name", artist.name())
                .addValue("genre", artist.genre()));

        return rows > 0;
    }

    public Integer getTotalCount() {
        String sql = "SELECT COUNT(*) as total FROM artist";
        Integer count = jdbcTemplate.queryForObject(sql, Map.of(), Integer.class);
        return count != null ? count : 0;
    }

    private static Artist mapArtist(ResultSet rs, int rowNum) throws SQLException {
        return new Artist(
                rs.getInt("id_artist"),
                rs.getString("nm_artist"),
                rs.getString("ds_genre")
        );
    }
}
