package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.AdminDTO;
import net.blackhacker.ares.dto.RoleDTO;
import net.blackhacker.ares.model.Admins;
import net.blackhacker.ares.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminMapperTest {

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminMapper adminMapper;

    @BeforeEach
    void setUp() {
        adminMapper = new AdminMapper(roleMapper, passwordEncoder);
    }

    @Test
    void toDTO_shouldReturnNull_whenAdminIsNull() {
        assertNull(adminMapper.toDTO(null));
    }

    @Test
    void toDTO_shouldMapAdminToAdminDTO() {
        // Arrange
        Admins admin = new Admins();
        admin.setName("Admin Name");
        admin.setEmail("admin@example.com");
        admin.setPassword("encodedPassword");

        Role role = new Role();
        role.setName("ADMIN");
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        admin.setRoles(roles);

        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setName("ADMIN");

        when(roleMapper.toDTO(any(Role.class))).thenReturn(roleDTO);

        // Act
        AdminDTO result = adminMapper.toDTO(admin);

        // Assert
        assertNotNull(result);
        assertEquals(admin.getName(), result.getName());
        assertEquals(admin.getEmail(), result.getEmail());
        assertNull(result.getPassword()); // Password should not be mapped to DTO
        assertNotNull(result.getRoles());
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains(roleDTO));
    }

    @Test
    void toDTO_shouldHandleNullRoles() {
        // Arrange
        Admins admin = new Admins();
        admin.setName("Admin Name");
        admin.setEmail("admin@example.com");
        admin.setRoles(null);

        // Act
        AdminDTO result = adminMapper.toDTO(admin);

        // Assert
        assertNotNull(result);
        assertEquals(admin.getName(), result.getName());
        assertEquals(admin.getEmail(), result.getEmail());
        assertNull(result.getRoles());
    }

    @Test
    void toModel_shouldReturnNull_whenAdminDTOIsNull() {
        assertNull(adminMapper.toModel(null));
    }

    @Test
    void toModel_shouldMapAdminDTOToAdmin() {
        // Arrange
        AdminDTO adminDTO = new AdminDTO();
        adminDTO.setName("Admin Name");
        adminDTO.setEmail("admin@example.com");
        adminDTO.setPassword("password");

        String encodedPassword = "encodedPassword";
        when(passwordEncoder.encode("password")).thenReturn(encodedPassword);

        // Act
        Admins result = adminMapper.toModel(adminDTO);

        // Assert
        assertNotNull(result);
        assertEquals(adminDTO.getName(), result.getName());
        assertEquals(adminDTO.getEmail(), result.getEmail());
        assertEquals(encodedPassword, result.getPassword());
        // Roles are not mapped in toModel as per implementation
        assertNotNull(result.getRoles());
        assertTrue(result.getRoles().isEmpty());
    }
}
