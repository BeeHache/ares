package net.blackhacker.ares.controller;

import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.mapper.UserMapper;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.service.AccountService;
import net.blackhacker.ares.service.UserService;
import net.blackhacker.ares.validation.UserDTOValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

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
        Optional<User> registeredUser = userService.registerUser(userMapper.toModel(userDTO));
        if (registeredUser.isEmpty()){
            return ResponseEntity.badRequest().build();
        }

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
        if (!userService.confirm(code)){
            return ResponseEntity.badRequest().body("Invalid confirmation code.");
        }
        return ResponseEntity.ok("Email confirmed successfully.");
    }
}
