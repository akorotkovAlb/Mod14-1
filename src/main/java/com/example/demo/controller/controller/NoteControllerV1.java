package com.example.demo.controller.controller;

import com.example.demo.service.dto.NoteDto;
import com.example.demo.service.exception.NoteNotFoundException;
import com.example.demo.service.mapper.NoteMapper;
import com.example.demo.service.service.NoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Objects;
import java.util.UUID;

@Validated
@Controller
@RequestMapping("/V1/notes")
public class NoteControllerV1 {

    @Autowired private NoteService noteService;
    @Autowired private NoteMapper noteMapper;

    @RequestMapping(value = "/list", method = {RequestMethod.GET})
    public ModelAndView noteList(@CookieValue(value = "userId", required = false) Long userId) {
        if (Objects.isNull(userId)) {
            return new ModelAndView("notes/index");
        }
        ModelAndView result = new ModelAndView("notes/allNotes");
        result.addObject("notes", noteMapper.toNoteResponses(noteService.listAll()));
        return result;
    }

    @RequestMapping(value = "/create", method = {RequestMethod.POST})
    public ModelAndView createNote(
            @CookieValue(value = "userId") Long userId,
            @RequestParam(value="title") @Size(min = 1, max = 20) String title,
            @RequestParam(value="content") @NotBlank String content) {
        NoteDto dto = new NoteDto();
        dto.setTitle(title);
        dto.setContent(content);
        dto.setUserId(userId);
        noteService.add(dto);
        return noteList(userId);
    }

    @RequestMapping(value = "/update", method = {RequestMethod.POST})
    public ModelAndView updateNote(
            @CookieValue(value = "userId") Long userId,
            @NotNull @RequestParam(value="id") String id,
            @Size(min = 1, max = 250) @RequestParam(value="title") String title,
            @NotEmpty @RequestParam(value="content") String content)
                throws NoteNotFoundException {
        NoteDto dto = new NoteDto();
        dto.setId(UUID.fromString(id));
        dto.setTitle(title);
        dto.setContent(content);
        dto.setUserId(userId);
        noteService.update(dto);
        return noteList(userId);
    }

    @DeleteMapping("/delete")
    @RequestMapping(value = "/delete", method = {RequestMethod.POST})
    public ModelAndView deleteNoteById(@CookieValue(value = "userId") Long userId,
                                       @Valid @NotNull @RequestParam(value="id") String id)
            throws NoteNotFoundException {
        noteService.deleteById(UUID.fromString(id), userId);
        return noteList(userId);
    }
}
