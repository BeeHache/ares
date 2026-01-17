package net.blackhacker.ares.validation;

import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDTOValidator {

    private final EmailValidator emailValidator;
    private final PasswordValidator passwordValidator;
    private final UserService userService;

    public UserDTOValidator(EmailValidator emailValidator, PasswordValidator passwordValidator, UserService userService) {
        this.emailValidator = emailValidator;
        this.passwordValidator = passwordValidator;
        this.userService = userService;
    }

    public void validateUserForRegistration(UserDTO userDTO) {
        if (userDTO == null) {
            throw new ValidationException("User data must not be null.");
        }

        emailValidator.validateEmail(userDTO.getEmail());
        passwordValidator.validatePassword(userDTO.getPassword());

        if (userService.getUserByEmail(userDTO.getEmail()).isPresent()) {
            throw new ValidationException("User already exists");
        }
    }
}
