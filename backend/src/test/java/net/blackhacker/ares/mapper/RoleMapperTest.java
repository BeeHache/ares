package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.RoleDTO;
import net.blackhacker.ares.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RoleMapperTest {

    @InjectMocks
    private RoleMapper roleMapper;

    @Test
    void toDTO_shouldMapRoleToRoleDTO() {
        // Arrange
        Role parent = new Role();
        parent.setName("PARENT");

        Role child = new Role();
        child.setName("CHILD");
        child.setParentRole(parent);

        List<Role> children = new ArrayList<>();
        children.add(child);
        parent.setSubRoles(children);

        // Act
        RoleDTO dto = roleMapper.toDTO(parent);

        // Assert
        assertNotNull(dto);
        assertEquals("PARENT", dto.getName());
        assertNull(dto.getSubRoles());
    }

    @Test
    void toModel_shouldMapRoleToRole() {
        // Arrange
        RoleDTO parentDTO = new RoleDTO();
        parentDTO.setName("PARENT_DTO");

        RoleDTO childDTO = new RoleDTO();
        childDTO.setName("CHILD_DTO");

        List<RoleDTO> childrenDTO = new ArrayList<>();
        childrenDTO.add(childDTO);
        parentDTO.setSubRoles(childrenDTO);

        // Act
        Role model = roleMapper.toModel(parentDTO);

        // Assert
        assertNotNull(model);
        assertEquals("PARENT_DTO", model.getName());
        assertNull(model.getParentRole());
        assertTrue(model.getSubRoles().isEmpty());
    }
}
