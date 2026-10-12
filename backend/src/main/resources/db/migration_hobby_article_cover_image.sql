-- 修复 Unknown column 'cover_image' in 'field list'
-- 若表是早期版本，可能没有 cover_image / updated_at，在 blog 库执行（已存在会报错可忽略对应行）。

ALTER TABLE hobby_article ADD COLUMN cover_image VARCHAR(255) NULL COMMENT '封面图 URL' AFTER content;

-- Mapper 的 insert/update 使用了 updated_at；若你表里没有该列再执行：
-- ALTER TABLE hobby_article ADD COLUMN updated_at DATETIME NULL DEFAULT NULL AFTER created_at;
