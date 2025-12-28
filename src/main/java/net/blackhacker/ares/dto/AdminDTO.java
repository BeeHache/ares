package net.blackhacker.ares.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class AdminDTO implements Serializable {
    private String name;
    private String email;
    private String password;
    private Set<RoleDTO> roles;
}
