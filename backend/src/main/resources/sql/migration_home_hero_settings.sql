-- 首页壁纸与标题（单条记录 id=1）
-- 若曾建过旧版空表，IF NOT EXISTS 不会加新列，INSERT 会报 Unknown column；先删再建可修复。
DROP TABLE IF EXISTS `home_hero_settings`;

CREATE TABLE `home_hero_settings` (
  `id` TINYINT NOT NULL DEFAULT 1,
  `hero_title` VARCHAR(128) NOT NULL DEFAULT '琉璃月',
  `intro_title` VARCHAR(256) NOT NULL DEFAULT '欢迎来到琉璃月',
  `banner_images` JSON NOT NULL,
  `hero_subtitles` JSON NOT NULL,
  `author_display_name` VARCHAR(128) NULL DEFAULT NULL COMMENT '侧栏昵称',
  `author_motto` VARCHAR(512) NULL DEFAULT NULL COMMENT '侧栏签名',
  `author_avatar` VARCHAR(512) NULL DEFAULT NULL COMMENT '侧栏头像 URL',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `home_hero_settings` (`id`, `hero_title`, `intro_title`, `banner_images`, `hero_subtitles`,
  `author_display_name`, `author_motto`)
VALUES (1, '琉璃月', '欢迎来到琉璃月', '[]', '[]', '黑虎阿昊', '不经一番寒彻骨，怎得梅花扑鼻香')
ON DUPLICATE KEY UPDATE `id` = `id`;
