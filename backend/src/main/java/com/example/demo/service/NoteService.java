package com.example.demo.service;

import com.example.demo.entity.Note;
import java.util.List;

public interface NoteService {
    List<Note> getAllNotes();
    Note getNoteById(Long id);
    void addNote(Note note);
    void updateNote(Note note);
    void deleteNoteById(Long id);
}
