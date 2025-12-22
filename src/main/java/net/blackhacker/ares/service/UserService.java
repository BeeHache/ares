package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Role;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.repository.RoleRepository;
import net.blackhacker.ares.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        if(userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already taken!");
        }
        return userRepository.save(user);
    }

    public User registerUser(String email, String rawPassword) {
        String hashed = passwordEncoder.encode(rawPassword);
        User user = new User();
        user.setEmail(email);
        user.setPassword(hashed);
        return registerUser(user);
    }

    public User loginUser(User user) {
        return user;
    }

    public boolean addRole(User user, Role role) {
        //TODO implement
        return false;
    }
}
