package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.repository.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public User registerUser(User user) {
        if(userRepository.existsByEmail(user.getEmail())) {
            return null;
        }
        return userRepository.save(user);
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email).orElse(null);
    }

    public User getUserByAccount(Account account){
        return userRepository.findByAccount(account).orElse(null);
    }

    public User saveUser(User user){
        return userRepository.save(user);
    }
}
