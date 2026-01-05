package net.blackhacker.ares.service;

import net.blackhacker.ares.model.User;
import net.blackhacker.ares.repository.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return user.toUserDetails();
      /*
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword()) // Must be already encoded (BCrypt)
                .roles(user.getRole())        // Spring adds "ROLE_" prefix automatically
                .disabled(!user.isEnabled())
                .accountExpired(false)
                .build();
                
       */
    }

    public User registerUser(User user) {
        if(userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already taken!");
        }
        return userRepository.save(user);
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email).orElse(null);
    }

    public User getUserByUserDetails(UserDetails userDetails){
        return userRepository.findByEmail(userDetails.getUsername()).orElse(null);
    }

    public void saveUser(User user){
        userRepository.save(user);
    }
}
