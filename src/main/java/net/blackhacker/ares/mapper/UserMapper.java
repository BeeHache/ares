package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper implements ModelDTOMapper<User, UserDTO> {

    private final PasswordEncoder passwordEncoder;

    public UserMapper(PasswordEncoder passwordEncoder){
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO toDTO(User user) {
        if (user == null) return null;

        UserDTO dto = new UserDTO();
        dto.setEmail(user.getEmail());
        // Password should not be mapped back to DTO for security
        return dto;
    }

    @Override
    public User toModel(UserDTO dto) {
        if (dto == null) return null;

        User user = new User();
        user.setEmail(dto.getEmail());

        Account account = new Account();
        account.setUsername(dto.getEmail());
        account.setPassword(passwordEncoder.encode(dto.getPassword())); //encode password
        account.setType(Account.AccountType.USER);
        user.setAccount(account);
        return user;
    }
}
