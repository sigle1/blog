-- 站点背景音乐（管理员上传）
CREATE TABLE IF NOT EXISTS `site_music_track` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `artist` VARCHAR(200) NOT NULL DEFAULT '',
  `file_path` VARCHAR(512) NOT NULL,
  `cover_url` VARCHAR(512) NULL COMMENT '封面图路径',
  `lrc_path` VARCHAR(512) NULL COMMENT 'LRC 歌词路径',
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sort` (`sort_order`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
