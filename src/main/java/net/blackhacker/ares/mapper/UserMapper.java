package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.stream.Collectors;

public class UserMapper implements ModelDTOMapper<User, UserDTO> {

    @Autowired
    private FeedMapper feedMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDTO toDTO(User user) {
        if (user == null) return null;

        UserDTO dto = new UserDTO();
        dto.setEmail(user.getEmail());
        // Password should generally not be mapped back to DTO for security
        if (user.getFeeds() != null) {
            dto.setFeeds(user.getFeeds().stream()
                    .map(feedMapper::toDTO)
                    .collect(Collectors.toSet()));
        }
        return dto;
    }

    @Override
    public User toModel(UserDTO dto) {
        if (dto == null) return null;

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); //encode password
        // Feeds and Roles usually require looking up entities from repositories
        return user;
    }
}
