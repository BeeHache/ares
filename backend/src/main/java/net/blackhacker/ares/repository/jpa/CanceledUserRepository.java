package net.blackhacker.ares.repository.jpa;

import net.blackhacker.ares.model.CanceledUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CanceledUserRepository extends JpaRepository<CanceledUser, Long> {
}
