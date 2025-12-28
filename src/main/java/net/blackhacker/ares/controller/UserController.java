package net.blackhacker.ares.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.mapper.UserMapper;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.service.FeedService;
import net.blackhacker.ares.service.UserService;
import net.blackhacker.ares.validation.UserDTOValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

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


    @GetMapping("/feeds")
    Collection<Feed> getFeeds() {
        return null;

    }

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

}
