package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping({"/upload", "/admin/upload"})
public class UploadController {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    private static final long MAX_IMAGE_BYTES = 8 * 1024 * 1024;
    private static final long MAX_VIDEO_BYTES = 120 * 1024 * 1024;

    private static final Set<String> IMAGE_EXT = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp", ".svg");

    private static final Set<String> VIDEO_EXT = Set.of(".mp4", ".webm", ".mov");

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();

        if (file == null || file.isEmpty()) {
            response.put("error", "文件不能为空");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String contentType = file.getContentType();
        String ct = contentType != null ? contentType.toLowerCase(Locale.ROOT) : "";
        boolean isImage = ct.startsWith("image/");
        boolean isVideo = ct.startsWith("video/");

        if (!isImage && !isVideo) {
            response.put("error", "仅支持上传图片或视频（mp4 / webm / mov）");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            response.put("error", "文件名无效");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase(Locale.ROOT);

        if (isImage) {
            if (!IMAGE_EXT.contains(ext)) {
                response.put("error", "不支持的图片格式");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            if (file.getSize() > MAX_IMAGE_BYTES) {
                response.put("error", "图片大小不能超过 8MB");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } else {
            if (!VIDEO_EXT.contains(ext)) {
                response.put("error", "不支持的视频格式，请使用 mp4、webm 或 mov");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            if (file.getSize() > MAX_VIDEO_BYTES) {
                response.put("error", "视频大小不能超过 120MB");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }

        try {
            File uploadDirFile = new File(uploadDir).getAbsoluteFile();
            if (!uploadDirFile.exists() && !uploadDirFile.mkdirs()) {
                response.put("error", "无法创建上传目录");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            String fileName = UUID.randomUUID() + ext;
            File dest = new File(uploadDirFile, fileName);
            file.transferTo(dest);

            String filePath = "/uploads/" + fileName;
            response.put("filePath", filePath);
            response.put("url", filePath);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("error", "文件上传失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
