package net.blackhacker.ares.controller;

import jakarta.servlet.http.HttpSession;
import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.mapper.UserMapper;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.service.UserService;
import net.blackhacker.ares.validation.UserDTOValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private HttpSession httpSession;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper mapper;

    @Autowired
    private UserDTOValidator userDTOValidator;

    @PostMapping("/register")
    void registerUser(@RequestBody UserDTO userDTO) {
        userDTOValidator.validateUserForRegistration(userDTO);
        userService.registerUser(mapper.toModel(userDTO));
    }

    @PostMapping("/login")
    void loginUser(@RequestBody UserDTO userDTO) {
        userDTOValidator.validateUserForLogin(userDTO);
        User user = userService.loginUser(mapper.toModel(userDTO));
        httpSession.setAttribute("user", mapper.toDTO(user));
    }

    @GetMapping("/logout")
    void logoutUser() {
        httpSession.invalidate();
    }

    @GetMapping("/")
    UserDTO getUser() {
        return mapper.toDTO((User) httpSession.getAttribute("user"));
    }

}
