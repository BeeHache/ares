package net.blackhacker.ares.repository;

import net.blackhacker.ares.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Custom query method: Find a user by their username
    Optional<User> findByEmail(String email);

    // Custom query method: Check if an email exists
    Boolean existsByEmail(String email);
}