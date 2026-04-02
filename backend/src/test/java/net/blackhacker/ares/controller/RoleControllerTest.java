package net.blackhacker.ares.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.blackhacker.ares.TestConfig;
import net.blackhacker.ares.dto.RoleDTO;
import net.blackhacker.ares.security.CustomAccessDeniedHandler;
import net.blackhacker.ares.security.JwtAuthenticationEntryPoint;
import net.blackhacker.ares.security.JwtAuthenticationFilter;
import net.blackhacker.ares.security.OAuth2LoginSuccessHandler;
import net.blackhacker.ares.service.AccountService;
import net.blackhacker.ares.service.JWTService;
import net.blackhacker.ares.service.RoleHierarchyService;
import net.blackhacker.ares.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({TestConfig.class, GlobalExceptionHandler.class})
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService roleService;

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

    @MockitoBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @MockitoBean
    private RoleHierarchyService roleHierarchyService;

    @MockitoBean
    private HttpSecurity httpSecurity;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getAllRoles_shouldReturnList() throws Exception {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(1L);
        roleDTO.setName("USER");

        when(roleService.getAllRoles()).thenReturn(Collections.singletonList(roleDTO));

        mockMvc.perform(get("/api/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("USER"));
    }

    @Test
    void getRoleHierarchy_shouldReturnTree() throws Exception {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(1L);
        roleDTO.setName("ADMIN");
        roleDTO.setSubRoles(Collections.emptyList());

        when(roleService.getRoleHierarchy()).thenReturn(Collections.singletonList(roleDTO));

        mockMvc.perform(get("/api/admin/roles/hierarchy")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ADMIN"));
    }

    @Test
    void createRole_shouldReturnCreatedRole() throws Exception {
        RoleDTO inputDTO = new RoleDTO();
        inputDTO.setName("NEW_ROLE");

        RoleDTO outputDTO = new RoleDTO();
        outputDTO.setId(1L);
        outputDTO.setName("NEW_ROLE");

        when(roleService.createRole(any(RoleDTO.class))).thenReturn(outputDTO);

        mockMvc.perform(post("/api/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("NEW_ROLE"));
    }

    @Test
    void updateRole_shouldReturnUpdatedRole() throws Exception {
        RoleDTO inputDTO = new RoleDTO();
        inputDTO.setName("UPDATED_NAME");

        RoleDTO outputDTO = new RoleDTO();
        outputDTO.setId(1L);
        outputDTO.setName("UPDATED_NAME");

        when(roleService.updateRole(eq(1L), any(RoleDTO.class))).thenReturn(outputDTO);

        mockMvc.perform(put("/api/admin/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UPDATED_NAME"));
    }

    @Test
    void deleteRole_shouldReturnOk() throws Exception {
        doNothing().when(roleService).deleteRole(1L);

        mockMvc.perform(delete("/api/admin/roles/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
