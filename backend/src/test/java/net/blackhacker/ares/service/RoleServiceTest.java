package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Role;
import net.blackhacker.ares.repository.jpa.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private Role parentRole;
    private Role childRole;
    private Role grandChildRole;

    @BeforeEach
    void setUp() {
        grandChildRole = new Role();
        grandChildRole.setName("GRAND_CHILD");

        childRole = new Role();
        childRole.setName("CHILD");
        childRole.getSubRoles().add(grandChildRole);

        parentRole = new Role();
        parentRole.setName("PARENT");
        parentRole.getSubRoles().add(childRole);
    }

    @Test
    void getSubRolesRecursive_shouldReturnAllRolesInTree() {
        List<Role> result = roleService.getSubRolesRecursive(parentRole);

        assertEquals(3, result.size());
        assertTrue(result.contains(parentRole));
        assertTrue(result.contains(childRole));
        assertTrue(result.contains(grandChildRole));
    }

    @Test
    void getSubRolesRecursive_shouldReturnEmptyList_whenRoleIsNull() {
        List<Role> result = roleService.getSubRolesRecursive(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllSubRoles_shouldReturnRoleNames_whenRoleExists() {
        when(roleRepository.findByName("PARENT")).thenReturn(Optional.of(parentRole));

        Collection<String> result = roleService.getAllSubRoles("PARENT");

        assertEquals(3, result.size());
        assertTrue(result.contains("PARENT"));
        assertTrue(result.contains("CHILD"));
        assertTrue(result.contains("GRAND_CHILD"));
    }

    @Test
    void getAllSubRoles_shouldReturnEmptyList_whenRoleDoesNotExist() {
        when(roleRepository.findByName("NON_EXISTENT")).thenReturn(Optional.empty());

        Collection<String> result = roleService.getAllSubRoles("NON_EXISTENT");

        assertTrue(result.isEmpty());
    }
}
