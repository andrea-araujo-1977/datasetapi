package dev.atilioaraujo.music.datasetapi.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("artist")
public record Artist(
        @Id
        @Column("id_artist")
        Integer idArtist,
        @Column("nm_artist")
        String name
) {
}

