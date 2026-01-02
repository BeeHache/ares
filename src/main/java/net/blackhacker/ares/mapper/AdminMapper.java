package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.AdminDTO;
import net.blackhacker.ares.model.Admins;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AdminMapper implements ModelDTOMapper<Admins, AdminDTO>{

    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminMapper(RoleMapper roleMapper, PasswordEncoder passwordEncoder){
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AdminDTO toDTO(Admins admin) {
        if (admin == null) return null;

        AdminDTO dto = new AdminDTO();
        dto.setName(admin.getName());
        dto.setEmail(admin.getEmail());
        // Password should generally not be mapped back to DTO for security
        if (admin.getRoles() != null) {
            dto.setRoles(admin.getRoles().stream()
                    .map(roleMapper::toDTO)
                    .collect(Collectors.toSet()));
        }
        return dto;
    }

    @Override
    public Admins toModel(AdminDTO dto) {
        if (dto == null) return null;

        Admins admin = new Admins();
        admin.setName(dto.getName());
        admin.setEmail(dto.getEmail());
        // Password should generally not be mapped back to DTO for security
        admin.setPassword(passwordEncoder.encode(dto.getPassword())); //encode password;
        // Roles usually require looking up entities from repositories
        return admin;
    }
}
