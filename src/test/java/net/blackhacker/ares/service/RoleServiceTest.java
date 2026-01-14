package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Role;
import net.blackhacker.ares.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = RoleService.class)
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @MockitoBean
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void getSubRolesRecursive_shouldReturnRoleAndDescendants() {
        // Arrange
        Role root = new Role();
        root.setName("ROOT");

        Role child1 = new Role();
        child1.setName("CHILD1");
        
        Role child2 = new Role();
        child2.setName("CHILD2");

        Role grandChild = new Role();
        grandChild.setName("GRANDCHILD");

        // Build hierarchy
        root.setSubRoles(List.of(child1, child2));
        child1.setSubRoles(List.of(grandChild));
        // child2 has no sub-roles (default empty list or null depending on impl, let's assume empty list from constructor or setter)
        child2.setSubRoles(new ArrayList<>());
        grandChild.setSubRoles(new ArrayList<>());

        // Act
        List<Role> result = roleService.getSubRolesRecursive(root);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.contains(root));
        assertTrue(result.contains(child1));
        assertTrue(result.contains(child2));
        assertTrue(result.contains(grandChild));
    }

    @Test
    void getAllSubRoles_shouldReturnRoleNames_whenRoleExists() {
        // Arrange
        String roleName = "ROOT";
        Role root = new Role();
        root.setName(roleName);

        Role child = new Role();
        child.setName("CHILD");
        root.setSubRoles(List.of(child));
        child.setSubRoles(new ArrayList<>());

        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(root));

        // Act
        Collection<String> result = roleService.getAllSubRoles(roleName);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("ROOT"));
        assertTrue(result.contains("CHILD"));
    }

    @Test
    void getAllSubRoles_shouldReturnEmptyList_whenRoleDoesNotExist() {
        // Arrange
        String roleName = "NON_EXISTENT";
        when(roleRepository.findByName(roleName)).thenReturn(Optional.empty());

        // Act
        Collection<String> result = roleService.getAllSubRoles(roleName);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
