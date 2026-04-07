package dev.atilioaraujo.music.datasetapi.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("song")
public record Song(
        @Id
        @Column("id_song")
        Integer idSong,
        @Column("nm_song_source")
        String nameSource,
        @Column("nm_song_streaming")
        String nameStreaming,
        @Column("id_track_number")
        Integer trackNumber,
        @Column("id_length_ms")
        Integer lengthMs,
        @Column("id_album")
        Integer albumId
) {
}

