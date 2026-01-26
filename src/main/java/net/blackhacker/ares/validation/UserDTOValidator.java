package net.blackhacker.ares.validation;

import net.blackhacker.ares.dto.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class UserDTOValidator {

    private final EmailValidator emailValidator;
    private final PasswordValidator passwordValidator;

    public UserDTOValidator(EmailValidator emailValidator, PasswordValidator passwordValidator) {
        this.emailValidator = emailValidator;
        this.passwordValidator = passwordValidator;
    }

    public void validateUserForRegistration(UserDTO userDTO) {
        if (userDTO == null) {
            throw new ValidationException("User data must not be null.");
        }

        emailValidator.validateEmail(userDTO.getEmail());
        passwordValidator.validatePassword(userDTO.getPassword());
    }
}
