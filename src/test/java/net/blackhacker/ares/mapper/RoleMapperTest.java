package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.RoleDTO;
import net.blackhacker.ares.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = RoleMapper.class)
@ExtendWith(MockitoExtension.class)
class RoleMapperTest {

    @InjectMocks
    private RoleMapper roleMapper;

    @Test
    void toDTO_shouldMapRoleAndChildrenToRoleDTO() {
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
        assertNotNull(dto.getChildren());
        assertEquals(1, dto.getChildren().size());
        assertEquals("CHILD", dto.getChildren().get(0).getName());
        assertTrue(dto.getChildren().get(0).getChildren().isEmpty());
    }

    @Test
    void toModel_shouldMapRoleDTOAndChildrenToRole() {
        // Arrange
        RoleDTO parentDTO = new RoleDTO();
        parentDTO.setName("PARENT_DTO");

        RoleDTO childDTO = new RoleDTO();
        childDTO.setName("CHILD_DTO");

        List<RoleDTO> childrenDTO = new ArrayList<>();
        childrenDTO.add(childDTO);
        parentDTO.setChildren(childrenDTO);

        // Act
        Role model = roleMapper.toModel(parentDTO);

        // Assert
        assertNotNull(model);
        assertEquals("PARENT_DTO", model.getName());
        assertNull(model.getParentRole());
        assertNotNull(model.getSubRoles());
        assertEquals(1, model.getSubRoles().size());

        Role childModel = model.getSubRoles().get(0);
        assertEquals("CHILD_DTO", childModel.getName());
        assertNotNull(childModel.getParentRole());
        assertEquals("PARENT_DTO", childModel.getParentRole().getName());
    }
}
