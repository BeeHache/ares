package net.blackhacker.ares.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.blackhacker.ares.mapper.RoleMapper;
import net.blackhacker.ares.model.Role;
import net.blackhacker.ares.repository.jpa.RoleRepository;
import net.blackhacker.ares.dto.RoleDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public RoleController(RoleRepository roleRepository, RoleMapper mapper){
        this.roleRepository = roleRepository;
        this.roleMapper = mapper;
    }

    // Get the full hierarchy
    @GetMapping("/tree")
    public List<RoleDTO> getRoleTree() {
        List<Role> rootRoles = roleRepository.findByParentRoleIsNull();
        return rootRoles.stream()
                .map(roleMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Create a sub-role under a parent
    @PostMapping("/{parentId}/subrole")
    public ResponseEntity<String> createSubRole(@PathVariable("parentId") Long parentId, @RequestBody RoleDTO roleDTO) {

        Optional<Role> optionalParentRole = roleRepository.findById(parentId);
        if (optionalParentRole.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        Role parentRole = optionalParentRole.get();
        Role subRole = roleMapper.toRole(roleDTO, parentRole);
        roleRepository.save(parentRole);
        Role savedRole = roleRepository.save(subRole);
        try {
            String json = new ObjectMapper().writeValueAsString(roleMapper.toDTO(savedRole));
            return ResponseEntity.ok().body(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
