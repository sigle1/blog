-- 首页侧栏：昵称、签名、头像（与 home_hero_settings 单条 id=1 同行存储）
-- 执行一次即可；若列已存在会报错，可忽略或手动跳过。

ALTER TABLE `home_hero_settings`
  ADD COLUMN `author_display_name` VARCHAR(128) NULL DEFAULT NULL COMMENT '侧栏显示昵称' AFTER `hero_subtitles`,
  ADD COLUMN `author_motto` VARCHAR(512) NULL DEFAULT NULL COMMENT '侧栏签名/格言' AFTER `author_display_name`,
  ADD COLUMN `author_avatar` VARCHAR(512) NULL DEFAULT NULL COMMENT '侧栏头像 URL（/uploads/...）' AFTER `author_motto`;
