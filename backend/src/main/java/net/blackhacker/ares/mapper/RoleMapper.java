package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.RoleDTO;
import net.blackhacker.ares.model.Role;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RoleMapper implements ModelDTOMapper<Role, RoleDTO> {

    @Override
    public RoleDTO toDTO(Role role) {
        if (role == null) return null;
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        if (role.getParentRole() != null) {
            dto.setParentId(role.getParentRole().getId());
            dto.setParentName(role.getParentRole().getName());
        }
        // Subroles are not recursively mapped here to avoid cycles in flat lists, 
        // they are handled by the service when building the hierarchy.
        return dto;
    }

    @Override
    public Role toModel(RoleDTO dto) {
        if (dto == null) return null;
        Role role = new Role();
        role.setId(dto.getId());
        role.setName(dto.getName());
        // Parent role is handled by the service
        return role;
    }
}
