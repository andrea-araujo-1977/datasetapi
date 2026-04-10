ALTER TABLE `artist`
    ADD COLUMN `ds_image_url` VARCHAR(500) NULL COMMENT 'Artist image URL from Spotify' AFTER `ds_genre`;

