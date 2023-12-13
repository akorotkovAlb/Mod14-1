package com.example.demo.service.service.impl;

import com.example.demo.data.entity.NoteEntity;
import com.example.demo.data.entity.UserEntity;
import com.example.demo.data.repository.NoteRepository;
import com.example.demo.service.dto.NoteDto;
import com.example.demo.service.dto.NoteWithUsernameDto;
import com.example.demo.service.exception.NoteNotFoundException;
import com.example.demo.service.mapper.NoteMapper;
import com.example.demo.service.service.NoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class NoteServiceImpl implements NoteService {

    @Autowired private NoteRepository noteRepository;
    @Autowired private NoteMapper noteMapper;

    @Override
    public List<NoteDto> listAll() {
        return noteMapper.toNoteDtos(noteRepository.findAll());
    }

    @Override
    public List<NoteWithUsernameDto> listAllUserNotes(Long userId) {
        return noteMapper.toNoteWithUsernameDtoList(noteRepository.findWithUsername(userId));
    }

    @Override
    public List<NoteWithUsernameDto> listAllUserNotes1(Long userId) {
        List<NoteEntity> list = noteRepository.findWithUser(userId);
        return noteMapper.toNoteWithUsernameDtoList1(list);
    }

    @Override
    @Transactional
    public NoteDto add(NoteDto note) {
        NoteEntity entity = noteMapper.toNoteEntity(note);
        entity.setId(null);
        entity.setUser(new UserEntity(note.getUserId()));
        entity.setCreatedDate(LocalDate.now());
        entity.setLastUpdatedDate(LocalDate.now());
        return noteMapper.toNoteDto(noteRepository.save(entity));
    }

    @Override
    @Transactional
    public List<NoteDto> addAll(Collection<NoteDto> notes) {
        Collection<NoteEntity> notesForSave = noteMapper.toNoteEntities(notes);
        notesForSave.forEach(note -> {
            note.setCreatedDate(LocalDate.now());
            note.setLastUpdatedDate(LocalDate.now());
        });
        return noteMapper.toNoteDtos(noteRepository.saveAll(notesForSave));
    }

    @Override
    @Transactional
    public void deleteById(UUID id, Long userId) throws NoteNotFoundException {
        NoteDto note = getById(id);
        if (!note.getUserId().equals(userId)) {
            throw new NoteNotFoundException(id);
        }
        noteRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void update(NoteDto note) throws NoteNotFoundException {
        if (Objects.isNull(note.getId())) {
            throw new NoteNotFoundException();
        }
        getById(note.getId());
        note.setLastUpdatedDate(LocalDate.now());
        noteRepository.save(noteMapper.toNoteEntity(note));
    }

    @Override
    public NoteDto getById(UUID id) throws NoteNotFoundException {
        Optional<NoteEntity> optionalNote = noteRepository.findById(id);
        if (optionalNote.isPresent()) {
            return noteMapper.toNoteDto(optionalNote.get());
        } else {
            throw new NoteNotFoundException(id);
        }
    }

    @Override
    public NoteDto getByTitle(String title) throws NoteNotFoundException {
        NoteEntity note = noteRepository.findByTitle(title)
                .orElseThrow(NoteNotFoundException::new);
        return noteMapper.toNoteDto(note);
    }
}
