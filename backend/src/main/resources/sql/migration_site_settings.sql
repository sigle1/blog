-- 站点全局设置（浏览器标签图标等），单条 id=1
CREATE TABLE IF NOT EXISTS `site_settings` (
  `id` TINYINT NOT NULL DEFAULT 1,
  `favicon_url` VARCHAR(512) NULL COMMENT '标签页图标，如 /uploads/xxx.png 或外链',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
