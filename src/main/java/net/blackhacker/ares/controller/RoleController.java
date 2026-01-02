package net.blackhacker.ares.controller;

import net.blackhacker.ares.mapper.RoleMapper;
import net.blackhacker.ares.model.Role;
import net.blackhacker.ares.repository.RoleRepository;
import net.blackhacker.ares.dto.RoleDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleRepository roleRepository;
    private final RoleMapper mapper;

    public RoleController(RoleRepository roleRepository, RoleMapper mapper){
        this.roleRepository = roleRepository;
        this.mapper = mapper;
    }

    // Get the full hierarchy
    @GetMapping("/tree")
    public List<RoleDTO> getRoleTree() {
        List<Role> rootRoles = roleRepository.findByParentRoleIsNull();
        return rootRoles.stream()
                .map(mapper::toDTO)
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