package net.blackhacker.ares.repository.jpa;

import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Custom query method: Find a user by their username
    Optional<User> findByEmail(String email);

    // Custom query method: Check if an email exists
    boolean existsByEmail(String email);

    Optional<User> findByAccount(Account account);

    @Modifying
    @Query(value = "insert into canceled_users (user_id) values (:userId) on conflict do nothing", nativeQuery = true)
    void cancelUser(@Param("userId") Long userId);


    @Query(value = "select user_id from canceled_users", nativeQuery = true)
    List<Long> getCanceledUserIds();
}
