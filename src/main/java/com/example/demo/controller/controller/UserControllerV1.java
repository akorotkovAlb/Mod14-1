package com.example.demo.controller.controller;

import com.example.demo.controller.request.V2.CreateUserRequest;
import com.example.demo.controller.request.V2.UpdateUserRequest;
import com.example.demo.controller.response.UserResponse;
import com.example.demo.service.dto.UserDto;
import com.example.demo.service.exception.UserAlreadyExistException;
import com.example.demo.service.exception.UserIncorrectPasswordException;
import com.example.demo.service.exception.UserNotFoundException;
import com.example.demo.service.mapper.NoteMapper;
import com.example.demo.service.mapper.UserMapper;
import com.example.demo.service.service.NoteService;
import com.example.demo.service.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Validated
@Controller
@RequestMapping("/V1/users")
public class UserControllerV1 {

    @Autowired private UserService userService;
    @Autowired private NoteService noteService;
    @Autowired private UserMapper userMapper;
    @Autowired private NoteMapper noteMapper;

    @GetMapping("/index")
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView getIndexPage() {
        return new ModelAndView("notes/index");
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView login(
            @RequestParam(value="username") @NotBlank String username,
            @RequestParam(value="password") @NotBlank String password,
            HttpServletResponse response) throws UserNotFoundException, UserIncorrectPasswordException {
        UserDto user = userService.login(username, password);
        Cookie cookie = new Cookie("userId", user.getId().toString());
        cookie.setPath("/V1/");
        response.addCookie(cookie);

        ModelAndView result = new ModelAndView("notes/allNotes");
        result.addObject("notes", noteMapper.toNoteResponses(noteService.listAll()));
        return result;
    }

    @GetMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        Cookie cookie = new Cookie("userId", "");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        request.logout();
        return new ModelAndView("notes/index");
    }
}
