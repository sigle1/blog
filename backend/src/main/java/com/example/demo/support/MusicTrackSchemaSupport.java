package com.example.demo.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 检测 site_music_track 表是否已执行 migration_site_music_track.sql。
 */
@Component
public class MusicTrackSchemaSupport {

    private static final Logger log = LoggerFactory.getLogger(MusicTrackSchemaSupport.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private volatile Boolean tableReady;
    private volatile Boolean metaColumnsReady;

    public boolean isTableReady() {
        if (tableReady != null) {
            return tableReady;
        }
        synchronized (this) {
            if (tableReady != null) {
                return tableReady;
            }
            tableReady = detectTableReady();
            if (!tableReady) {
                log.warn(
                        "音乐表 site_music_track 不存在，前台将不显示后台歌单。"
                                + "请在数据库执行 sql/migration_site_music_track.sql 后重启应用。"
                );
            }
            return tableReady;
        }
    }

    public boolean hasMetaColumns() {
        if (!isTableReady()) {
            return false;
        }
        if (metaColumnsReady != null) {
            return metaColumnsReady;
        }
        synchronized (this) {
            if (metaColumnsReady != null) {
                return metaColumnsReady;
            }
            metaColumnsReady = detectMetaColumnsReady();
            if (!metaColumnsReady) {
                log.warn(
                        "site_music_track 缺少 cover_url/lrc_path 列，上传仍可用但无封面/歌词字段。"
                                + "请执行 sql/migration_site_music_track_meta.sql。"
                );
            }
            return metaColumnsReady;
        }
    }

    private boolean detectTableReady() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.TABLES "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'site_music_track'",
                    Integer.class
            );
            return count != null && count > 0;
        } catch (DataAccessException e) {
            return false;
        }
    }

    private boolean detectMetaColumnsReady() {
        try {
            Integer cover = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'site_music_track' AND COLUMN_NAME = 'cover_url'",
                    Integer.class
            );
            Integer lrc = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'site_music_track' AND COLUMN_NAME = 'lrc_path'",
                    Integer.class
            );
            return cover != null && cover > 0 && lrc != null && lrc > 0;
        } catch (DataAccessException e) {
            return false;
        }
    }
}
