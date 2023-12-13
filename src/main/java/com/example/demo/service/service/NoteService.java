package com.example.demo.service.service;

import com.example.demo.service.dto.NoteDto;
import com.example.demo.service.dto.NoteWithUsernameDto;
import com.example.demo.service.exception.NoteNotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface NoteService {

    List<NoteDto> listAll();

    List<NoteWithUsernameDto> listAllUserNotes(Long userId);
    List<NoteWithUsernameDto> listAllUserNotes1(Long userId);

    NoteDto add(NoteDto note);

    List<NoteDto> addAll(Collection<NoteDto> notes);

    void deleteById(UUID id, Long userId) throws NoteNotFoundException;

    void update(NoteDto note) throws NoteNotFoundException;

    NoteDto getById(UUID id) throws NoteNotFoundException;

    NoteDto getByTitle(String title) throws NoteNotFoundException;
}
