package net.blackhacker.ares.service;

import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.dto.RoleDTO;
import net.blackhacker.ares.mapper.RoleMapper;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.Role;
import net.blackhacker.ares.repository.jpa.AccountRepository;
import net.blackhacker.ares.repository.jpa.RoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final AccountRepository accountRepository; // To check if roles are in use

    public RoleService(RoleRepository roleRepository, RoleMapper roleMapper, AccountRepository accountRepository) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
        this.accountRepository = accountRepository;
    }

    // --- CRUD Operations ---

    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<RoleDTO> getRoleById(Long id) {
        return roleRepository.findById(id).map(roleMapper::toDTO);
    }

    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) {
        if (roleRepository.findByName(roleDTO.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Role with name " + roleDTO.getName() + " already exists.");
        }
        Role role = roleMapper.toModel(roleDTO);
        if (roleDTO.getParentId() != null) {
            roleRepository.findById(roleDTO.getParentId())
                    .ifPresent(role::setParentRole);
        }
        return roleMapper.toDTO(roleRepository.save(role));
    }

    @Transactional
    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        return roleRepository.findById(id).map(existingRole -> {
            if (!existingRole.getName().equals(roleDTO.getName()) && roleRepository.findByName(roleDTO.getName()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Role with name " + roleDTO.getName() + " already exists.");
            }
            existingRole.setName(roleDTO.getName());

            // Handle re-parenting
            if (roleDTO.getParentId() == null) {
                existingRole.setParentRole(null);
            } else {
                if (id.equals(roleDTO.getParentId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A role cannot be its own parent.");
                }
                roleRepository.findById(roleDTO.getParentId())
                        .ifPresentOrElse(
                                existingRole::setParentRole,
                                () -> { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent role not found."); }
                        );
            }
            return roleMapper.toDTO(roleRepository.save(existingRole));
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

        // Check if the role is assigned to any account
        List<Account> accountsWithRole = accountRepository.findByRolesContaining(role);
        if (!accountsWithRole.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Role is assigned to " + accountsWithRole.size() + " accounts and cannot be deleted.");
        }

        // Check if the role has any children
        if (!role.getSubRoles().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Role has sub-roles and cannot be deleted. Re-parent or delete sub-roles first.");
        }

        roleRepository.delete(role);
    }

    // --- Hierarchy Operations ---

    public List<RoleDTO> getRoleHierarchy() {
        // Get all roles
        List<Role> allRoles = roleRepository.findAll();

        // Build a map for quick lookup by ID
        java.util.Map<Long, RoleDTO> roleMap = allRoles.stream()
                .map(roleMapper::toDTO)
                .collect(Collectors.toMap(RoleDTO::getId, dto -> dto));

        // Build the hierarchy
        List<RoleDTO> topLevelRoles = new ArrayList<>();
        for (Role role : allRoles) {
            RoleDTO roleDTO = roleMap.get(role.getId());
            if (role.getParentRole() == null) {
                topLevelRoles.add(roleDTO);
            } else {
                RoleDTO parentDTO = roleMap.get(role.getParentRole().getId());
                if (parentDTO != null) {
                    if (parentDTO.getSubRoles() == null) {
                        parentDTO.setSubRoles(new ArrayList<>());
                    }
                    if (!parentDTO.getSubRoles().contains(roleDTO)) {
                        parentDTO.getSubRoles().add(roleDTO);
                    }
                }
            }
        }
        return topLevelRoles;
    }

    // --- Existing methods (adjusted for DTOs if needed) ---

    public List<Role> getSubRolesRecursive(Role role) {
        List<Role> allRoles = new ArrayList<>();
        if (role == null) {
            return allRoles;
        }
        allRoles.add(role);
        if (role.getSubRoles() != null) {
            for (Role subRole : role.getSubRoles()) {
                allRoles.addAll(getSubRolesRecursive(subRole));
            }
        }
        return allRoles;
    }

    public Collection<String> getAllSubRoles(String roleString) {
        Optional<Role> optionalRole = roleRepository.findByName(roleString);
        if(optionalRole.isEmpty()){
            return new ArrayList<>();
        }
        List<Role> roleTree = getSubRolesRecursive(optionalRole.get());
        List<String> roleNames = new ArrayList<>();
        for(Role role : roleTree) {
            roleNames.add(role.getName());
        }
        return roleNames;
    }
}
