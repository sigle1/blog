package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MusicTrackResponse {
    private Long id;
    private String title;
    private String artist;
    /** 与前端播放器字段一致，对应存储的 file_path（如 /uploads/music/xxx.mp3） */
    private String src;
    private String cover;
    private String lrc;
}
