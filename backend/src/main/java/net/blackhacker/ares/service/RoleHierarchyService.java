package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Role;
import net.blackhacker.ares.repository.jpa.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleHierarchyService {

    private final RoleRepository roleRepository;

    public RoleHierarchyService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public String getRoleHierarchyString() {
        List<Role> roles = roleRepository.findAll();
        
        return roles.stream()
                .filter(role -> role.getParentRole() != null)
                .map(role -> "ROLE_" + role.getParentRole().getName() + " > ROLE_" + role.getName())
                .collect(Collectors.joining("\n"));
    }
}
