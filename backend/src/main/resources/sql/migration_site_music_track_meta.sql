-- 音乐曲目：封面与歌词（执行前请备份；若列已存在可跳过）
ALTER TABLE `site_music_track`
  ADD COLUMN `cover_url` VARCHAR(512) NULL COMMENT '封面图路径' AFTER `file_path`,
  ADD COLUMN `lrc_path` VARCHAR(512) NULL COMMENT 'LRC 歌词路径' AFTER `cover_url`;
