package com.example.demo.service;

import com.example.demo.dto.MusicTrackResponse;
import com.example.demo.dto.MusicTrackUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MusicTrackService {
    List<MusicTrackResponse> listPublic();

    MusicTrackResponse upload(MultipartFile file, String title, String artist,
                              MultipartFile cover, MultipartFile lrc);

    MusicTrackResponse update(Long id, MusicTrackUpdateRequest body);

    void reorder(List<Long> ids);

    void delete(Long id);
}
