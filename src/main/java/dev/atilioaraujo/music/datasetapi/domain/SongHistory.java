package dev.atilioaraujo.music.datasetapi.domain;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("song_history")
public record SongHistory(
        @Id
        @Column("id_song_history")
        Integer idSongHistory,
        @Column("id_song")
        Integer songId,
        @Column("dt_song_played")
        LocalDateTime playedAt
) {
}

