package net.blackhacker.ares.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.blackhacker.ares.dto.RoleDTO;
import net.blackhacker.ares.mapper.RoleMapper;
import net.blackhacker.ares.model.Role;
import net.blackhacker.ares.repository.RoleRepository;
import net.blackhacker.ares.security.CustomAccessDeniedHandler;
import net.blackhacker.ares.security.JwtAuthenticationEntryPoint;
import net.blackhacker.ares.security.JwtAuthenticationFilter;
import net.blackhacker.ares.service.AccountService;
import net.blackhacker.ares.service.JWTService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private RoleMapper roleMapper;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @MockitoBean
    private CustomAccessDeniedHandler forbiddenHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getRoleTree_shouldReturnListOfRoles() throws Exception {
        // Arrange
        Role role = new Role();
        role.setName("ADMIN");
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setName("ADMIN");

        when(roleRepository.findByParentRoleIsNull()).thenReturn(Collections.singletonList(role));
        when(roleMapper.toDTO(role)).thenReturn(roleDTO);

        // Act & Assert
        mockMvc.perform(get("/api/roles/tree")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ADMIN"));
    }

    @Test
    void createSubRole_shouldReturnCreatedRole_whenParentExists() throws Exception {
        // Arrange

        Role parentRole = new Role();
        parentRole.setId(1L);
        parentRole.setName("PARENT");

        RoleDTO newRole = new RoleDTO();
        newRole.setName("CHILD");

        Role savedRole = new Role();
        savedRole.setId(2L);
        savedRole.setName("CHILD");
        savedRole.setParentRole(parentRole);


        when(roleRepository.findById(any())).thenReturn(Optional.of(parentRole));
        when(roleMapper.toRole(any(), any())).thenReturn(savedRole);
        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);
        when(roleMapper.toDTO(any(Role.class))).thenReturn(newRole);


        // Act & Assert
        mockMvc.perform(post("/api/roles/{parentId}/subrole", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRole)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("CHILD"));
    }
}
