package com.example.demo.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 检测留言互动相关表结构是否已执行 migration_guest_message_interactions.sql。
 */
@Component
public class GuestMessageSchemaSupport {

    private static final Logger log = LoggerFactory.getLogger(GuestMessageSchemaSupport.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private volatile Boolean interactionsReady;

    public boolean isInteractionsReady() {
        if (interactionsReady != null) {
            return interactionsReady;
        }
        synchronized (this) {
            if (interactionsReady != null) {
                return interactionsReady;
            }
            interactionsReady = detectInteractionsReady();
            if (!interactionsReady) {
                log.warn(
                        "留言互动表结构未就绪（缺少 likes/favorites 或 message_comments），"
                                + "已使用兼容模式。请在数据库执行 sql/migration_guest_message_interactions.sql 后重启应用。"
                );
            }
            return interactionsReady;
        }
    }

    private boolean detectInteractionsReady() {
        try {
            Integer likesCol = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'guest_messages' AND COLUMN_NAME = 'likes'",
                    Integer.class
            );
            Integer favoritesCol = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'guest_messages' AND COLUMN_NAME = 'favorites'",
                    Integer.class
            );
            Integer commentsTable = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.TABLES "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'message_comments'",
                    Integer.class
            );
            return likesCol != null && likesCol > 0
                    && favoritesCol != null && favoritesCol > 0
                    && commentsTable != null && commentsTable > 0;
        } catch (DataAccessException e) {
            return false;
        }
    }

    /** 部署迁移后若需重新检测，可调用（一般不必） */
    public void resetCache() {
        interactionsReady = null;
    }
}
