package net.blackhacker.ares.dto;

import lombok.Data;
import net.blackhacker.ares.model.Role;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class RoleDTO {
    private Integer id;
    private String name;
    private List<RoleDTO> children;

    public RoleDTO(Role role) {
        this.id = role.getId();
        this.name = role.getName();
        this.children = role.getSubRoles().stream()
                .map(RoleDTO::new)
                .collect(Collectors.toList());
    }
}
