package net.blackhacker.ares.repository.jpa;

import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.Admins;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminsRepository extends JpaRepository<Admins, Long> {
    Optional<Admins> findByAccount(Account account);
}
