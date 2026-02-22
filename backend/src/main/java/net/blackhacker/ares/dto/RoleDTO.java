package net.blackhacker.ares.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RoleDTO implements Serializable {
    private String name;
    private List<RoleDTO> children;
}
