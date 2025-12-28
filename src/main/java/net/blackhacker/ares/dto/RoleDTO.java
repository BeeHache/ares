package net.blackhacker.ares.dto;

import lombok.Data;
import net.blackhacker.ares.model.Role;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class RoleDTO implements Serializable {
    private String name;
    private List<RoleDTO> children;
}
