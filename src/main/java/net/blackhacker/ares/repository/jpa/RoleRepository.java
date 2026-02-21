package net.blackhacker.ares.repository.jpa;

import net.blackhacker.ares.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Finds all top-level roles (e.g., "Super Admin")
    List<Role> findByParentRoleIsNull();

    // Finds children of a specific role
    List<Role> findByParentRoleId(Integer parentId);

    Optional<Role> findByName(String name);
}
