package net.blackhacker.ares.repository.jpa;

import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsername(String username);

    @Query(value = "SELECT a FROM Account a WHERE " +
                   "(:type IS NULL OR a.type = :type) AND " +
                   "(:locked IS NULL OR (:locked = true AND a.accountLockedUntil > CURRENT_TIMESTAMP) OR (:locked = false AND (a.accountLockedUntil IS NULL OR a.accountLockedUntil <= CURRENT_TIMESTAMP)))",
           countQuery = "SELECT count(a) FROM Account a WHERE " +
                        "(:type IS NULL OR a.type = :type) AND " +
                        "(:locked IS NULL OR (:locked = true AND a.accountLockedUntil > CURRENT_TIMESTAMP) OR (:locked = false AND (a.accountLockedUntil IS NULL OR a.accountLockedUntil <= CURRENT_TIMESTAMP)))")
    Page<Account> findAllWithFilters(@Param("type") Account.AccountType type, 
                                     @Param("locked") Boolean locked, 
                                     Pageable pageable);

    @Query(value = "SELECT a FROM Account a",
           countQuery = "SELECT count(a) FROM Account a")
    Page<Account> findAllAccountProjection(Pageable pageable);

    List<Account> findByRolesContaining(Role role);
}
