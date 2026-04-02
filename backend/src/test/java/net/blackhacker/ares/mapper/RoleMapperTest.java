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
        parent.setId(1L);
        parent.setName("PARENT");

        Role child = new Role();
        child.setId(2L);
        child.setName("CHILD");
        child.setParentRole(parent);

        List<Role> children = new ArrayList<>();
        children.add(child);
        parent.setSubRoles(children);

        // Act
        RoleDTO parentDto = roleMapper.toDTO(parent);
        RoleDTO childDto = roleMapper.toDTO(child);

        // Assert
        assertNotNull(parentDto);
        assertEquals("PARENT", parentDto.getName());
        assertNull(parentDto.getParentId());

        assertNotNull(childDto);
        assertEquals("CHILD", childDto.getName());
        assertEquals(1L, childDto.getParentId());
        assertEquals("PARENT", childDto.getParentName());
    }

    @Test
    void toModel_shouldMapRoleToRole() {
        // Arrange
        RoleDTO parentDTO = new RoleDTO();
        parentDTO.setName("PARENT_DTO");

        // Act
        Role model = roleMapper.toModel(parentDTO);

        // Assert
        assertNotNull(model);
        assertEquals("PARENT_DTO", model.getName());
        assertNull(model.getParentRole());
        assertTrue(model.getSubRoles().isEmpty());
    }
}
