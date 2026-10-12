package com.example.demo.dto;

import lombok.Data;

@Data
public class MusicTrackUpdateRequest {
    private String title;
    private String artist;
    private String coverUrl;
    private String lrcPath;
}
