package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Role;
import net.blackhacker.ares.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> getSubRolesRecursive(Role role) {
        List<Role> allRoles = new ArrayList<>();
        if (role == null) {
            return allRoles;
        }
        
        // Add the current role
        allRoles.add(role);
        
        // Recursively add sub-roles
        if (role.getSubRoles() != null) {
            for (Role subRole : role.getSubRoles()) {
                allRoles.addAll(getSubRolesRecursive(subRole));
            }
        }
        
        return allRoles;
    }

    public Collection<String> getAllSubRoles(String roleString) {
        Optional<Role> optionalRole = roleRepository.findByName(roleString);
        if(optionalRole.isEmpty()){
            return new ArrayList<>();
        }
        
        List<Role> roleTree = getSubRolesRecursive(optionalRole.get());
        List<String> roleNames = new ArrayList<>();
        for(Role role : roleTree) {
            roleNames.add(role.getName());
        }
        return roleNames;
    }
}
