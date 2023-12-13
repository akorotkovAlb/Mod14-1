package com.example.demo.controller.controller;

import com.example.demo.controller.request.V2.CreateNoteRequest;
import com.example.demo.controller.request.V2.UpdateNoteRequest;
import com.example.demo.controller.response.NoteResponse;
import com.example.demo.data.projection.NoteWithUserNameProj;
import com.example.demo.service.dto.NoteDto;
import com.example.demo.service.dto.NoteWithUsernameDto;
import com.example.demo.service.exception.NoteNotFoundException;
import com.example.demo.service.mapper.NoteMapper;
import com.example.demo.service.service.NoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Validated
@Controller
@RequestMapping("/V2/notes")
public class NoteControllerV2 {

    private static final String EXTENSION = "json";
    private static final String UPLOAD_FILE_EXCEPTION_TEXT =
            "Can not upload file with name %s. File must have .json extension.";

    @Autowired private NoteService noteService;
    @Autowired private NoteMapper noteMapper;
    @Autowired private ObjectMapper objectMapper;

    @GetMapping("/list")
    public ResponseEntity<List<NoteResponse>> noteList() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(noteMapper.toNoteResponses(noteService.listAll()));
    }

    @GetMapping("/user/list")
    public ResponseEntity<List<NoteWithUsernameDto>> getAllUserNotes(
            @CookieValue(value = "userId") Long userId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(noteService.listAllUserNotes(userId));
    }

    @GetMapping("/user/1/list")
    public ResponseEntity<List<NoteWithUsernameDto>> getAllUserNotes1(
            @CookieValue(value = "userId") Long userId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(noteService.listAllUserNotes1(userId));
    }

    @PostMapping("/create")
    public ResponseEntity<NoteResponse> createNote(
            @CookieValue(value = "userId") Long userId,
            @Valid @NotNull @RequestBody CreateNoteRequest request) {
        NoteDto noteDto = noteMapper.toNoteDto(request);
        noteDto.setUserId(userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(noteMapper.toNoteResponse(noteService.add(noteDto)));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateNote(
            @CookieValue(value = "userId") Long userId,
            @PathVariable("id") UUID id,
            @RequestBody @Valid @NotNull UpdateNoteRequest request) throws NoteNotFoundException {
        NoteDto noteDto = noteMapper.toNoteDto(id, request);
        noteDto.setUserId(userId);
        noteService.update(noteDto);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<NoteResponse> getNoteById(@PathVariable("id") UUID id) throws NoteNotFoundException {
        NoteDto noteDto = noteService.getById(id);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(noteMapper.toNoteResponse(noteDto));
    }

    @GetMapping("/title")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<NoteResponse> getNoteByTitle(@RequestParam("title") String title) throws NoteNotFoundException {
        NoteDto noteDto = noteService.getByTitle(title);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(noteMapper.toNoteResponse(noteDto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNoteById(@CookieValue(value = "userId") Long userId,
                               @PathVariable("id") UUID id) throws NoteNotFoundException {
        noteService.deleteById(id, userId);
    }

    @PostMapping("/upload")
    public ResponseEntity<List<NoteResponse>> uploadFromFile(@CookieValue(value = "userId") Long userId,
                                                             @RequestPart("file") MultipartFile file)
            throws IOException {
        String extension = Objects.requireNonNull(file.getOriginalFilename())
                .substring(file.getOriginalFilename().lastIndexOf('.') + 1);
        if (Objects.isNull(extension) || extension.isBlank() || !EXTENSION.equalsIgnoreCase(extension)) {
            throw new FileUploadException(String.format(UPLOAD_FILE_EXCEPTION_TEXT, file.getOriginalFilename()));
        }
        byte[] bytes = file.getBytes();
        List<NoteDto> notes = noteMapper
                .requestsToNoteDtos(Arrays.asList(objectMapper.readValue(bytes, CreateNoteRequest[].class)));
        notes.forEach(note -> note.setUserId(userId));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(noteMapper.toNoteResponses(noteService.addAll(notes)));
    }
}
