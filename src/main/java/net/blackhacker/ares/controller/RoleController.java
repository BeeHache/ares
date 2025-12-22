package net.blackhacker.ares.controller;

import net.blackhacker.ares.model.Role;
import net.blackhacker.ares.repository.RoleRepository;
import net.blackhacker.ares.dto.RoleDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleRepository roleRepository;

    public RoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    // Get the full hierarchy
    @GetMapping("/tree")
    public List<RoleDTO> getRoleTree() {
        List<Role> rootRoles = roleRepository.findByParentRoleIsNull();
        return rootRoles.stream()
                .map(RoleDTO::new)
                .collect(Collectors.toList());
    }

    // Create a sub-role under a parent
    @PostMapping("/{parentId}/subrole")
    public Role createSubRole(@PathVariable Integer parentId, @RequestBody Role newRole) {
        return roleRepository.findById(parentId).map(parent -> {
            newRole.setParentRole(parent);
            return roleRepository.save(newRole);
        }).orElseThrow(() -> new RuntimeException("Parent role not found"));
    }
}