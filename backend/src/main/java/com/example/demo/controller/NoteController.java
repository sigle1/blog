package com.example.demo.controller;

import com.example.demo.entity.Note;
import com.example.demo.service.NoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping({"/notes", "/admin/notes"})
public class NoteController {
    @Autowired
    private NoteService noteService;

    @GetMapping
    public List<Note> getNotes() {
        return noteService.getAllNotes();
    }

    @PostMapping
    public ResponseEntity<Note> addNote(@RequestBody Note note) {
        try {
            if (note == null || note.getTitle() == null || note.getTitle().isEmpty() || 
                note.getContent() == null || note.getContent().isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            if (note.getCreatedAt() == null) {
                note.setCreatedAt(LocalDateTime.now());
            }
            noteService.addNote(note);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            log.error("Error adding note: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        Note note = noteService.getNoteById(id);
        if (note == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(note);
    }

    // 更新笔记
    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable Long id, @RequestBody Note noteDetails) {
        try {
            Note note = noteService.getNoteById(id);
            if (note == null) {
                return ResponseEntity.notFound().build();
            }
            if (noteDetails == null || noteDetails.getTitle() == null || noteDetails.getTitle().isEmpty() || 
                noteDetails.getContent() == null || noteDetails.getContent().isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            note.setTitle(noteDetails.getTitle());
            note.setContent(noteDetails.getContent());
            noteService.updateNote(note);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            log.error("Error updating note: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    // 删除笔记
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteNote(@PathVariable Long id) {
        try {
            Note note = noteService.getNoteById(id);
            if (note == null) {
                return ResponseEntity.notFound().build();
            }
            noteService.deleteNoteById(id);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            log.error("Error deleting note: {}", e.getMessage());
            return ResponseEntity.status(500).body(false);
        }
    }
}
