package com.example.demo.service.impl;

import com.example.demo.dto.MusicTrackResponse;
import com.example.demo.dto.MusicTrackUpdateRequest;
import com.example.demo.entity.MusicTrack;
import com.example.demo.mapper.MusicTrackMapper;
import com.example.demo.service.MusicTrackService;
import com.example.demo.support.MusicTrackSchemaSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MusicTrackServiceImpl implements MusicTrackService {

    private static final Logger log = LoggerFactory.getLogger(MusicTrackServiceImpl.class);

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    private static final String MUSIC_SUB = "music";
    private static final long MAX_AUDIO_BYTES = 50L * 1024 * 1024;
    private static final long MAX_COVER_BYTES = 5L * 1024 * 1024;
    private static final long MAX_LRC_BYTES = 512 * 1024;

    private static final Set<String> ALLOWED_AUDIO_EXT = Set.of(
            ".mp3", ".m4a", ".aac", ".ogg", ".wav", ".flac", ".opus", ".webm");
    private static final Set<String> ALLOWED_COVER_EXT = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif");
    private static final Set<String> ALLOWED_LRC_EXT = Set.of(".lrc", ".txt");

    @Autowired
    private MusicTrackMapper musicTrackMapper;

    @Autowired
    private MusicTrackSchemaSupport schemaSupport;

    @Override
    public List<MusicTrackResponse> listPublic() {
        if (!schemaSupport.isTableReady()) {
            return List.of();
        }
        try {
            return musicTrackMapper.selectAllOrdered().stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("读取音乐列表失败", e);
            return List.of();
        }
    }

    @Override
    public MusicTrackResponse upload(MultipartFile file, String title, String artist,
                                     MultipartFile cover, MultipartFile lrc) {
        if (!schemaSupport.isTableReady()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "音乐表未创建，请先执行 migration_site_music_track.sql");
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "音频文件不能为空");
        }
        if (file.getSize() > MAX_AUDIO_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "音频不能超过 50MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文件名无效");
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        if (!ALLOWED_AUDIO_EXT.contains(ext)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不支持的音频格式");
        }

        String ct = file.getContentType();
        if (ct != null && !ct.toLowerCase(Locale.ROOT).startsWith("audio/")
                && !"application/octet-stream".equalsIgnoreCase(ct)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请上传音频文件");
        }

        File musicDir = ensureSubDir(MUSIC_SUB);
        String storedName = UUID.randomUUID() + ext;
        File dest = new File(musicDir, storedName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "文件保存失败");
        }

        String filePath = "/uploads/" + MUSIC_SUB + "/" + storedName;
        String coverUrl = schemaSupport.hasMetaColumns() ? saveCover(cover) : null;
        String lrcPath = schemaSupport.hasMetaColumns() ? saveLrc(lrc) : null;

        String useTitle = (title != null && !title.isBlank()) ? title.trim() : stripExt(originalFilename);
        String useArtist = artist != null ? artist.trim() : "";

        MusicTrack track = new MusicTrack();
        track.setTitle(useTitle);
        track.setArtist(useArtist);
        track.setFilePath(filePath);
        track.setCoverUrl(coverUrl);
        track.setLrcPath(lrcPath);
        track.setSortOrder(musicTrackMapper.selectNextSortOrder());
        track.setCreatedAt(LocalDateTime.now());

        if (schemaSupport.hasMetaColumns()) {
            musicTrackMapper.insert(track);
        } else {
            musicTrackMapper.insertLegacy(track);
        }
        return toResponse(track);
    }

    @Override
    public MusicTrackResponse update(Long id, MusicTrackUpdateRequest body) {
        MusicTrack existing = musicTrackMapper.selectById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "曲目不存在");
        }
        if (body.getTitle() != null && !body.getTitle().isBlank()) {
            existing.setTitle(body.getTitle().trim());
        }
        if (body.getArtist() != null) {
            existing.setArtist(body.getArtist().trim());
        }
        if (body.getCoverUrl() != null) {
            existing.setCoverUrl(body.getCoverUrl().isBlank() ? null : body.getCoverUrl().trim());
        }
        if (body.getLrcPath() != null) {
            existing.setLrcPath(body.getLrcPath().isBlank() ? null : body.getLrcPath().trim());
        }
        musicTrackMapper.update(existing);
        return toResponse(existing);
    }

    @Override
    public void reorder(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            if (id == null) continue;
            if (musicTrackMapper.selectById(id) == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的曲目 ID: " + id);
            }
            musicTrackMapper.updateSortOrder(id, i);
        }
    }

    @Override
    public void delete(Long id) {
        MusicTrack existing = musicTrackMapper.selectById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "曲目不存在");
        }
        deleteStoredFile(existing.getFilePath());
        deleteStoredFile(existing.getCoverUrl());
        deleteStoredFile(existing.getLrcPath());
        musicTrackMapper.delete(id);
    }

    private String saveCover(MultipartFile cover) {
        if (cover == null || cover.isEmpty()) {
            return null;
        }
        if (cover.getSize() > MAX_COVER_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "封面不能超过 5MB");
        }
        String name = cover.getOriginalFilename();
        if (name == null || !name.contains(".")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "封面文件名无效");
        }
        String ext = name.substring(name.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        if (!ALLOWED_COVER_EXT.contains(ext)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "封面仅支持 JPG/PNG/WebP/GIF");
        }
        File dir = ensureSubDir(MUSIC_SUB + "/covers");
        String stored = UUID.randomUUID() + ext;
        transferTo(cover, new File(dir, stored));
        return "/uploads/" + MUSIC_SUB + "/covers/" + stored;
    }

    private String saveLrc(MultipartFile lrc) {
        if (lrc == null || lrc.isEmpty()) {
            return null;
        }
        if (lrc.getSize() > MAX_LRC_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "歌词文件过大");
        }
        String name = lrc.getOriginalFilename();
        if (name == null || !name.contains(".")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "歌词文件名无效");
        }
        String ext = name.substring(name.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        if (!ALLOWED_LRC_EXT.contains(ext)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "歌词仅支持 .lrc / .txt");
        }
        File dir = ensureSubDir(MUSIC_SUB + "/lrc");
        String stored = UUID.randomUUID() + ext;
        transferTo(lrc, new File(dir, stored));
        return "/uploads/" + MUSIC_SUB + "/lrc/" + stored;
    }

    private File ensureSubDir(String sub) {
        File dir = new File(new File(uploadDir).getAbsoluteFile(), sub.replace('/', File.separatorChar));
        if (!dir.exists() && !dir.mkdirs()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "无法创建上传目录");
        }
        return dir;
    }

    private void transferTo(MultipartFile file, File dest) {
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "文件保存失败");
        }
    }

    private void deleteStoredFile(String rel) {
        if (rel == null || !rel.startsWith("/uploads/")) {
            return;
        }
        String sub = rel.substring("/uploads/".length()).replace('/', File.separatorChar);
        File f = new File(new File(uploadDir).getAbsoluteFile(), sub);
        if (f.isFile()) {
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }
    }

    private static String stripExt(String name) {
        int i = name.lastIndexOf('.');
        return i > 0 ? name.substring(0, i) : name;
    }

    private MusicTrackResponse toResponse(MusicTrack t) {
        MusicTrackResponse r = new MusicTrackResponse();
        r.setId(t.getId());
        r.setTitle(t.getTitle());
        r.setArtist(t.getArtist());
        r.setSrc(t.getFilePath());
        r.setCover(t.getCoverUrl());
        r.setLrc(t.getLrcPath());
        return r;
    }
}
