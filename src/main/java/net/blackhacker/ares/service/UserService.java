package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Role;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.repository.RoleRepository;
import net.blackhacker.ares.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public User registerUser(User user) {
        if(userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already taken!");
        }
        return userRepository.save(user);
    }

    public User loginUser(User user) {
        return user;
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email).orElse(null);
    }

}
