package com.example.demo.service.impl;

import com.example.demo.entity.Note;
import com.example.demo.mapper.NoteMapper;
import com.example.demo.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteServiceImpl implements NoteService {
    @Autowired
    private NoteMapper noteMapper;

    @Override
    public List<Note> getAllNotes() {
        return noteMapper.selectAll();
    }

    @Override
    public Note getNoteById(Long id) {
        return noteMapper.selectById(id);
    }

    @Override
    public void addNote(Note note) {
        noteMapper.insert(note);
    }

    @Override
    public void updateNote(Note note) {
        noteMapper.update(note);
    }

    @Override
    public void deleteNoteById(Long id) {
        noteMapper.deleteById(id);
    }
}
