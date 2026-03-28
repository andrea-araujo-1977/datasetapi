package dev.atilioaraujo.music.datasetapi.domain;

import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("album")
public record Album(
        @Id
        @Column("id_album")
        Integer idAlbum,
        @Column("nm_album")
        String name,
        @Column("dt_release")
        LocalDate releaseDate,
        @Column("id_artist")
        Integer artistId
) {
}

