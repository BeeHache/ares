package net.blackhacker.ares.controller;

import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.mapper.UserMapper;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.service.UserService;
import net.blackhacker.ares.validation.UserDTOValidator;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Slf4j
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
        log.info("Registration attempt for email: {}", userDTO.getEmail());
        userDTOValidator.validateUserForRegistration(userDTO);
        Optional<User> registeredUser = userService.registerUser(userMapper.toModel(userDTO));
        if (registeredUser.isEmpty()){
            log.warn("Registration failed for email: {}", userDTO.getEmail());
            return ResponseEntity.badRequest().build();
        }

        log.info("User registered successfully: {}", userDTO.getEmail());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/")
                .build()
                .toUri();

        return ResponseEntity
                .created(location)
                .body(userMapper.toDTO(registeredUser.get()));
    }

    @GetMapping("/confirm/{code}")
    public ResponseEntity<String> confirmEmail(@PathVariable("code") String code) {
        log.debug("Email confirmation attempt with code: {}", code);
        if (!userService.confirm(code)){
            log.warn("Email confirmation failed for code: {}", code);
            return ResponseEntity.badRequest().body("Invalid confirmation code.");
        }
        log.info("Email confirmed successfully for code: {}", code);
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body("Email confirmed successfully.");
    }
}
