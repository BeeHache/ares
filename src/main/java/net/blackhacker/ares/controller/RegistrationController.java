package net.blackhacker.ares.controller;

import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.mapper.UserMapper;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.service.UserService;
import net.blackhacker.ares.validation.UserDTOValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/register")
public class RegistrationController {
    private final UserService userService;
    private final UserDTOValidator userDTOValidator;
    private final UserMapper userMapper;

    public RegistrationController(UserService userService, UserDTOValidator userDTOValidator, UserMapper userMapper){
        this.userService = userService;
        this.userDTOValidator = userDTOValidator;
        this.userMapper = userMapper;
    }

    @PostMapping
    ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO userDTO) {
        userDTOValidator.validateUserForRegistration(userDTO);
        User registeredUser = userService.registerUser(userMapper.toModel(userDTO));
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/")
                .build()
                .toUri();

        return ResponseEntity
                .created(location)
                .body(userMapper.toDTO(registeredUser));
    }
}
