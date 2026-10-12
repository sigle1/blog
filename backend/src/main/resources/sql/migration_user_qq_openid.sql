-- QQ 登录：绑定 openid（执行前请备份）
ALTER TABLE `user`
  ADD COLUMN `qq_openid` VARCHAR(64) NULL COMMENT 'QQ互联 openid' AFTER `role`,
  ADD UNIQUE KEY `uk_user_qq_openid` (`qq_openid`);
