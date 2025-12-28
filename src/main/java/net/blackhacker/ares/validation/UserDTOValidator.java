package net.blackhacker.ares.validation;

import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDTOValidator {

    @Autowired
    private EmailValidator emailValidator;

    @Autowired
    private PasswordValidator passwordValidator;

    @Autowired
    private UserService userService;

    public void validateUserForLogin(UserDTO userDTO) {
        if (userDTO == null) {
            throw new ValidationException("User data must not be null.");
        }

        emailValidator.validateEmail(userDTO.getEmail());
        passwordValidator.validatePassword(userDTO.getPassword());
    }

    public void validateUserForRegistration(UserDTO userDTO) {
        validateUserForLogin(userDTO);

        if (userService.getUserByEmail(userDTO.getEmail()) != null) {
            throw new ValidationException("User already exists");
        }
    }
}
