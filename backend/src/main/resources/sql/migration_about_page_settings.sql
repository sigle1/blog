-- 关于页自我介绍（单条 id=1）；在 phpMyAdmin 中请先选中博客库，或取消下行注释：
-- USE `blog`;

CREATE TABLE IF NOT EXISTS `about_page_settings` (
  `id` TINYINT NOT NULL DEFAULT 1,
  `intro` TEXT NULL COMMENT '关于页自我介绍，纯文本，支持换行',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
