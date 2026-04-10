package dev.atilioaraujo.music.datasetapi.dao;

import dev.atilioaraujo.music.datasetapi.domain.Artist;
import dev.atilioaraujo.music.datasetapi.dto.ArtistView;
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
    private static final RowMapper<ArtistView> ARTIST_VIEW_ROW_MAPPER = ArtistDao::mapArtistView;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ArtistDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Artist> findByNameIgnoreCase(String name) {
        String sql = """
                SELECT id_artist, nm_artist, ds_genre, ds_image_url
                FROM artist
                WHERE LOWER(nm_artist) = LOWER(:name)
                ORDER BY id_artist
                """;

        return jdbcTemplate.query(sql, Map.of("name", name), ARTIST_ROW_MAPPER);
    }

    public List<Artist> findArtistsMissingMetadata(int amount) {
        String sql = """
                SELECT id_artist, nm_artist, ds_genre, ds_image_url
                FROM artist
                WHERE ds_genre IS NULL
                   OR TRIM(ds_genre) = ''
                   OR ds_image_url IS NULL
                   OR TRIM(ds_image_url) = ''
                ORDER BY id_artist
                LIMIT :amount
                """;

        return jdbcTemplate.query(sql, new MapSqlParameterSource().addValue("amount", amount), ARTIST_ROW_MAPPER);
    }

    public Artist insert(Artist artist) {
        String sql = """
                INSERT INTO artist (nm_artist, ds_genre, ds_image_url)
                VALUES (:name, :genre, :imageUrl)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", artist.name())
                .addValue("genre", artist.genre())
                .addValue("imageUrl", artist.imageUrl());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id_artist"});

        Number generatedId = keyHolder.getKey();
        Integer id = generatedId != null ? generatedId.intValue() : null;
        return new Artist(id, artist.name(), artist.genre(), artist.imageUrl());
    }

    public boolean update(Artist artist) {
        if (artist.idArtist() == null) {
            throw new IllegalArgumentException("artist.idArtist must be informed for update");
        }

        String sql = """
                UPDATE artist
                SET nm_artist = :name,
                    ds_genre = :genre,
                    ds_image_url = :imageUrl
                WHERE id_artist = :idArtist
                """;

        int rows = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("idArtist", artist.idArtist())
                .addValue("name", artist.name())
                .addValue("genre", artist.genre())
                .addValue("imageUrl", artist.imageUrl()));

        return rows > 0;
    }

    public Integer getTotalCount() {
        String sql = "SELECT COUNT(*) as total FROM artist";
        Integer count = jdbcTemplate.queryForObject(sql, Map.of(), Integer.class);
        return count != null ? count : 0;
    }

    public List<ArtistView> findTopArtistViews(int page, int maxElements) {
        int normalizedPage = Math.max(page, 1);
        int normalizedMaxElements = Math.max(maxElements, 1);
        int offset = (normalizedPage - 1) * normalizedMaxElements;

        String sql = """
                SELECT a.nm_artist AS artist_name,
                       COUNT(sh.id_song_history) AS total_plays,
                       a.ds_image_url AS artist_image_url
                FROM artist a
                LEFT JOIN album al ON al.id_artist = a.id_artist
                LEFT JOIN song s ON s.id_album = al.id_album
                LEFT JOIN song_history sh ON sh.id_song = s.id_song
                GROUP BY a.id_artist, a.nm_artist, a.ds_image_url
                ORDER BY total_plays DESC, a.nm_artist
                LIMIT :limit OFFSET :offset
                """;

        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("limit", normalizedMaxElements)
                        .addValue("offset", offset),
                ARTIST_VIEW_ROW_MAPPER
        );
    }

    public Integer getPendingMetadataCount() {
        String sql = """
                SELECT COUNT(*) as total
                FROM artist
                WHERE ds_genre IS NULL
                   OR TRIM(ds_genre) = ''
                   OR ds_image_url IS NULL
                   OR TRIM(ds_image_url) = ''
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Map.of(), Integer.class);
        return count != null ? count : 0;
    }

    private static Artist mapArtist(ResultSet rs, int rowNum) throws SQLException {
        return new Artist(
                rs.getInt("id_artist"),
                rs.getString("nm_artist"),
                rs.getString("ds_genre"),
                rs.getString("ds_image_url")
        );
    }

    private static ArtistView mapArtistView(ResultSet rs, int rowNum) throws SQLException {
        return new ArtistView(
                rs.getString("artist_name"),
                (int) rs.getLong("total_plays"),
                rs.getString("artist_image_url")
        );
    }
}
