package com.example.demo.controller;

import com.example.demo.dto.MusicTrackResponse;
import com.example.demo.dto.MusicTrackUpdateRequest;
import com.example.demo.service.MusicTrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping
public class MusicTrackController {

    @Autowired
    private MusicTrackService musicTrackService;

    /** 游客端与前台播放器 */
    @GetMapping("/music/tracks")
    public List<MusicTrackResponse> listPublic() {
        return musicTrackService.listPublic();
    }

    @GetMapping("/admin/music/tracks")
    public List<MusicTrackResponse> listAdmin() {
        return musicTrackService.listPublic();
    }

    @PostMapping(value = "/admin/music/tracks", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MusicTrackResponse upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "artist", required = false) String artist,
            @RequestParam(value = "cover", required = false) MultipartFile cover,
            @RequestParam(value = "lrc", required = false) MultipartFile lrc) {
        return musicTrackService.upload(file, title, artist, cover, lrc);
    }

    @PutMapping("/admin/music/tracks/{id}")
    public MusicTrackResponse update(
            @PathVariable("id") Long id,
            @RequestBody MusicTrackUpdateRequest body) {
        return musicTrackService.update(id, body);
    }

    @PutMapping("/admin/music/tracks/order")
    public void reorder(@RequestBody List<Long> ids) {
        musicTrackService.reorder(ids);
    }

    @DeleteMapping("/admin/music/tracks/{id}")
    public void delete(@PathVariable("id") Long id) {
        musicTrackService.delete(id);
    }
}
