package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.RoleDTO;
import net.blackhacker.ares.model.Role;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RoleMapper implements ModelDTOMapper<Role, RoleDTO>{
    @Override
    public RoleDTO toDTO(Role role) {
        if (role == null) return null;

        RoleDTO dto = new RoleDTO();
        dto.setName(role.getName());
        dto.setChildren(role.getSubRoles().stream()
                .map(this::toDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    public Role toRole(RoleDTO dto,Role parent) {
        if (dto == null) return null;

        Role role = new Role();
        role.setName(dto.getName());
        role.setParentRole(parent);
        if (dto.getChildren() != null){
            role.setSubRoles(dto.getChildren().stream()
                    .map(child -> toRole(child,role))
                    .collect(Collectors.toList()));
        }

        return role;
    }

    @Override
    public Role toModel(RoleDTO dto) {
        if (dto == null) return null;

        return toRole(dto,null);
    }
}
