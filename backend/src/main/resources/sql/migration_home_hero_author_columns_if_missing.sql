-- 若线上保存「首页壁纸」报 500，且日志/SQL 提示 Unknown column 'author_display_name' 等，
-- 在 blog 库执行本脚本（可重复执行：已存在的列会跳过）。

SET @db = DATABASE();

-- author_display_name
SET @sql = (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'home_hero_settings' AND COLUMN_NAME = 'author_display_name'
    ),
    'SELECT 1',
    'ALTER TABLE home_hero_settings ADD COLUMN author_display_name VARCHAR(128) NULL DEFAULT NULL COMMENT ''侧栏昵称'' AFTER hero_subtitles'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- author_motto
SET @sql = (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'home_hero_settings' AND COLUMN_NAME = 'author_motto'
    ),
    'SELECT 1',
    'ALTER TABLE home_hero_settings ADD COLUMN author_motto VARCHAR(512) NULL DEFAULT NULL COMMENT ''侧栏签名'' AFTER author_display_name'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- author_avatar
SET @sql = (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'home_hero_settings' AND COLUMN_NAME = 'author_avatar'
    ),
    'SELECT 1',
    'ALTER TABLE home_hero_settings ADD COLUMN author_avatar VARCHAR(512) NULL DEFAULT NULL COMMENT ''侧栏头像 URL'' AFTER author_motto'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
