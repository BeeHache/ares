package net.blackhacker.ares.service;

import net.blackhacker.ares.dto.RoleDTO;
import net.blackhacker.ares.mapper.RoleMapper;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.Role;
import net.blackhacker.ares.repository.jpa.AccountRepository;
import net.blackhacker.ares.repository.jpa.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private RoleService roleService;

    private Role parentRole;
    private Role childRole;
    private Role grandChildRole;
    private RoleDTO parentRoleDTO;
    private RoleDTO childRoleDTO;
    private RoleDTO grandChildDTO;


    @BeforeEach
    void setUp() {
        parentRole = new Role();
        parentRole.setId(1L);
        parentRole.setName("PARENT");

        childRole = new Role();
        childRole.setId(2L);
        childRole.setName("CHILD");

        grandChildRole = new Role();
        grandChildRole.setId(3L);
        grandChildRole.setName("GRAND_CHILD");

        parentRole.setSubRoles(Collections.singletonList(childRole));
        childRole.setParentRole(parentRole);
        childRole.setSubRoles(Collections.singletonList(grandChildRole));
        grandChildRole.setParentRole(childRole);

        // DTOs
        parentRoleDTO = new RoleDTO();
        parentRoleDTO.setId(1L);
        parentRoleDTO.setName("PARENT");

        childRoleDTO = new RoleDTO();
        childRoleDTO.setId(2L);
        childRoleDTO.setName("CHILD");
        childRoleDTO.setParentId(parentRoleDTO.getId());
        childRoleDTO.setParentName(parentRoleDTO.getName());

        // Mock mapper for each role
        grandChildDTO = new RoleDTO();
        grandChildDTO.setId(3L);
        grandChildDTO.setName("GRAND_CHILD");
        grandChildDTO.setParentId(childRoleDTO.getId());
        grandChildDTO.setParentName(childRoleDTO.getName());

        // Ensure parent-child relationships are set up in the DTOs
        parentRoleDTO.setSubRoles(new ArrayList<>(Collections.singletonList(childRoleDTO)));
        childRoleDTO.setSubRoles(new ArrayList<>(Collections.singletonList(grandChildDTO)));
    }

    // --- Existing tests (updated) ---

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

    // --- New CRUD tests ---

    @Test
    void getAllRoles_shouldReturnListOfRoleDTOs() {
        when(roleRepository.findAll()).thenReturn(Arrays.asList(parentRole, childRole));
        when(roleMapper.toDTO(parentRole)).thenReturn(parentRoleDTO);
        when(roleMapper.toDTO(childRole)).thenReturn(childRoleDTO);

        List<RoleDTO> result = roleService.getAllRoles();

        assertEquals(2, result.size());
        assertEquals("PARENT", result.get(0).getName());
        assertEquals("CHILD", result.get(1).getName());
    }

    @Test
    void getRoleById_shouldReturnRoleDTO_whenFound() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(parentRole));
        when(roleMapper.toDTO(parentRole)).thenReturn(parentRoleDTO);

        Optional<RoleDTO> result = roleService.getRoleById(1L);

        assertTrue(result.isPresent());
        assertEquals("PARENT", result.get().getName());
    }

    @Test
    void createRole_shouldCreateNewRole() {
        RoleDTO newRoleDTO = new RoleDTO();
        newRoleDTO.setName("NEW_ROLE");
        newRoleDTO.setParentId(1L);

        Role newRole = new Role();
        newRole.setName("NEW_ROLE");

        when(roleRepository.findByName("NEW_ROLE")).thenReturn(Optional.empty());
        when(roleMapper.toModel(any(RoleDTO.class))).thenReturn(newRole);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(parentRole));
        when(roleRepository.save(any(Role.class))).thenReturn(newRole);
        when(roleMapper.toDTO(any(Role.class))).thenReturn(newRoleDTO);

        RoleDTO result = roleService.createRole(newRoleDTO);

        assertNotNull(result);
        assertEquals("NEW_ROLE", result.getName());
        verify(roleRepository).save(newRole);
    }

    @Test
    void createRole_shouldThrowConflict_whenRoleExists() {
        RoleDTO newRoleDTO = new RoleDTO();
        newRoleDTO.setName("PARENT");
        when(roleRepository.findByName("PARENT")).thenReturn(Optional.of(parentRole));

        assertThrows(ResponseStatusException.class, () -> roleService.createRole(newRoleDTO));
    }

    @Test
    void updateRole_shouldUpdateExistingRole() {
        RoleDTO updateDTO = new RoleDTO();
        updateDTO.setName("UPDATED_PARENT");
        updateDTO.setParentId(2L); // Change parent

        Role existingRole = new Role();
        existingRole.setId(1L);
        existingRole.setName("PARENT");

        Role newParentRole = new Role();
        newParentRole.setId(2L);
        newParentRole.setName("CHILD");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(existingRole));
        when(roleRepository.findByName("UPDATED_PARENT")).thenReturn(Optional.empty()); // No name conflict
        when(roleRepository.findById(2L)).thenReturn(Optional.of(newParentRole));
        when(roleRepository.save(any(Role.class))).thenReturn(existingRole);
        when(roleMapper.toDTO(any(Role.class))).thenReturn(updateDTO);

        RoleDTO result = roleService.updateRole(1L, updateDTO);

        assertNotNull(result);
        assertEquals("UPDATED_PARENT", result.getName());
        assertEquals(2L, existingRole.getParentRole().getId());
        verify(roleRepository).save(existingRole);
    }

    @Test
    void updateRole_shouldThrowNotFound_whenRoleDoesNotExist() {
        RoleDTO updateDTO = new RoleDTO();
        updateDTO.setName("NON_EXISTENT");
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> roleService.updateRole(99L, updateDTO));
    }

    @Test
    void deleteRole_shouldDeleteRole_whenNotUsedAndNoSubRoles() {
        Role roleToDelete = new Role();
        roleToDelete.setId(1L);
        roleToDelete.setName("UNUSED_ROLE");
        roleToDelete.setSubRoles(Collections.emptyList());

        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleToDelete));
        when(accountRepository.findByRolesContaining(roleToDelete)).thenReturn(Collections.emptyList());

        roleService.deleteRole(1L);

        verify(roleRepository).delete(roleToDelete);
    }

    @Test
    void deleteRole_shouldThrowConflict_whenRoleHasSubRoles() {
        Role roleToDelete = new Role();
        roleToDelete.setId(1L);
        roleToDelete.setName("PARENT");
        roleToDelete.setSubRoles(Collections.singletonList(childRole)); // Has sub-roles

        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleToDelete));

        assertThrows(ResponseStatusException.class, () -> roleService.deleteRole(1L));
    }

    @Test
    void deleteRole_shouldThrowConflict_whenRoleIsAssignedToAccounts() {
        Role roleToDelete = new Role();
        roleToDelete.setId(1L);
        roleToDelete.setName("ASSIGNED_ROLE");
        roleToDelete.setSubRoles(Collections.emptyList());

        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleToDelete));
        when(accountRepository.findByRolesContaining(roleToDelete)).thenReturn(Collections.singletonList(new Account()));

        assertThrows(ResponseStatusException.class, () -> roleService.deleteRole(1L));
    }

    @Test
    void getRoleHierarchy_shouldReturnTreeStructure() {
        // Mock flat list of roles
        List<Role> allRoles = Arrays.asList(parentRole, childRole, grandChildRole);
        when(roleRepository.findAll()).thenReturn(allRoles);
        when(roleMapper.toDTO(parentRole)).thenReturn(parentRoleDTO);
        when(roleMapper.toDTO(childRole)).thenReturn(childRoleDTO);
        when(roleMapper.toDTO(grandChildRole)).thenReturn(grandChildDTO);

        List<RoleDTO> hierarchy = roleService.getRoleHierarchy();

        assertNotNull(hierarchy);
        assertEquals(1, hierarchy.size()); // Only PARENT is a top-level role
        assertEquals("PARENT", hierarchy.get(0).getName());
        assertEquals(1, hierarchy.get(0).getSubRoles().size());
        assertEquals("CHILD", hierarchy.get(0).getSubRoles().get(0).getName());
        assertEquals(1, hierarchy.get(0).getSubRoles().get(0).getSubRoles().size());
        assertEquals("GRAND_CHILD", hierarchy.get(0).getSubRoles().get(0).getSubRoles().get(0).getName());
    }
}
