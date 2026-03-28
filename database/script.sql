CREATE DATABASE "songs-dataset" /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE "songs-dataset";

CREATE TABLE "artist" (
  "id_artist" int NOT NULL AUTO_INCREMENT,
  "nm_artist" varchar(100) NOT NULL,
  PRIMARY KEY ("id_artist"),
  KEY "nm_artist_idx" ("nm_artist")
);

CREATE TABLE "album" (
     "id_album" int NOT NULL AUTO_INCREMENT,
     "nm_album" varchar(100) NOT NULL,
     "dt_release" date DEFAULT NULL,
     "id_artist" int NOT NULL,
     PRIMARY KEY ("id_album"),
     KEY "fk_artist_idx" ("id_artist"),
     CONSTRAINT "fk_artist" FOREIGN KEY ("id_artist") REFERENCES "artist" ("id_artist")
);

CREATE TABLE "song" (
    "id_song" int NOT NULL AUTO_INCREMENT,
    "nm_song_source" varchar(150) NOT NULL COMMENT 'Song''s name as collected',
    "id_track_number" int NOT NULL,
    "id_album" int NOT NULL,
    "nm_song_streaming" varchar(150) DEFAULT NULL COMMENT 'Song''s name recovered at streaming service',
    PRIMARY KEY ("id_song"),
    KEY "idx_nm_song" ("nm_song_source"),
    KEY "fk_album_idx" ("id_album"),
    CONSTRAINT "fk_album" FOREIGN KEY ("id_album") REFERENCES "album" ("id_album")
);


CREATE TABLE `songs-dataset`.`song_history` (
    `id_song_history` INT NOT NULL AUTO_INCREMENT,
    `id_song` INT NOT NULL,
    `dt_song_played` DATETIME NOT NULL,
    PRIMARY KEY (`id_song_history`),
    INDEX `idx_dt_song_played` (`dt_song_played` ASC) INVISIBLE,
    INDEX `fk_id_song_idx` (`id_song` ASC) VISIBLE,
    CONSTRAINT `fk_id_song`
        FOREIGN KEY (`id_song`)
            REFERENCES `songs-dataset`.`song` (`id_song`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION
);