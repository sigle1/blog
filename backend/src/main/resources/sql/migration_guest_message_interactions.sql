-- 留言互动：点赞、收藏、评论（执行前请备份；可重复执行）
-- 若某列已存在，对应 ALTER 会报错，可跳过该条继续执行后面的语句。

-- 1) 点赞 / 收藏列（缺哪条执行哪条）
-- ALTER TABLE `guest_messages` ADD COLUMN `likes` INT NOT NULL DEFAULT 0 AFTER `content`;
-- ALTER TABLE `guest_messages` ADD COLUMN `favorites` INT NOT NULL DEFAULT 0 AFTER `likes`;

-- 推荐一次性执行（仅首次）：
ALTER TABLE `guest_messages`
  ADD COLUMN `likes` INT NOT NULL DEFAULT 0 AFTER `content`,
  ADD COLUMN `favorites` INT NOT NULL DEFAULT 0 AFTER `likes`;

-- 2) 留言评论表
CREATE TABLE IF NOT EXISTS `message_comments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `message_id` BIGINT NOT NULL,
  `parent_id` BIGINT DEFAULT NULL,
  `nickname` VARCHAR(50) NOT NULL DEFAULT '游客',
  `content` TEXT NOT NULL,
  `likes` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `message_id_idx` (`message_id`),
  KEY `parent_id_idx` (`parent_id`),
  CONSTRAINT `fk_message_comments_message` FOREIGN KEY (`message_id`) REFERENCES `guest_messages` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_message_comments_parent` FOREIGN KEY (`parent_id`) REFERENCES `message_comments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
