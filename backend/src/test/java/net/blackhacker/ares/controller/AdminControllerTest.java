package net.blackhacker.ares.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.blackhacker.ares.TestConfig;
import net.blackhacker.ares.dto.AccountDTO;
import net.blackhacker.ares.mapper.AccountMapper;
import net.blackhacker.ares.mapper.FeedMapper;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.Role;
import net.blackhacker.ares.repository.jpa.AccountRepository;
import net.blackhacker.ares.repository.jpa.RoleRepository;
import net.blackhacker.ares.security.CustomAccessDeniedHandler;
import net.blackhacker.ares.security.JwtAuthenticationEntryPoint;
import net.blackhacker.ares.security.JwtAuthenticationFilter;
import net.blackhacker.ares.security.OAuth2LoginSuccessHandler;
import net.blackhacker.ares.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({TestConfig.class, GlobalExceptionHandler.class})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private FeedService feedService;

    @MockitoBean
    private FeedMapper feedMapper;

    @MockitoBean
    private AccountMapper accountMapper;

    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private JWTService jwtService;

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
    void getStats_shouldReturnStats() throws Exception {
        when(userService.userCount()).thenReturn(10L);
        when(feedService.feedCount()).thenReturn(5L);
        when(feedService.feedItemsCount()).thenReturn(100L);

        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.totalFeeds").value(5))
                .andExpect(jsonPath("$.totalArticles").value(100));
    }

    @Test
    void getAccounts_shouldReturnPageOfAccounts() throws Exception {
        Account account = new Account();
        account.setId(1L);
        AccountDTO dto = new AccountDTO();
        dto.setId(1L);
        dto.setUsername("test@example.com");

        Page<Account> page = new PageImpl<>(Collections.singletonList(account));
        when(accountService.getAccountsFiltered(any(), any(), any(Pageable.class))).thenReturn(page);
        when(accountMapper.toDTO(account)).thenReturn(dto);

        mockMvc.perform(get("/api/admin/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("test@example.com"));
    }

    @Test
    void createAccount_shouldReturnCreatedAccount() throws Exception {
        AccountDTO inputDto = new AccountDTO();
        inputDto.setUsername("new@test.com");
        inputDto.setPassword("password123");
        inputDto.setType("USER");

        Account account = new Account();
        AccountDTO outputDto = new AccountDTO();
        outputDto.setId(1L);
        outputDto.setUsername("new@test.com");

        when(accountMapper.toModel(any(AccountDTO.class))).thenReturn(account);
        when(accountService.createAccount(any(Account.class), any())).thenReturn(account);
        when(accountMapper.toDTO(any(Account.class))).thenReturn(outputDto);

        mockMvc.perform(post("/api/admin/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("new@test.com"));
    }

    @Test
    void updateAccountRoles_shouldReturnUpdatedAccount() throws Exception {
        Long accountId = 1L;
        List<Long> roleIds = List.of(2L);
        
        Account account = new Account();
        account.setId(accountId);
        
        Role role = new Role();
        role.setId(2L);
        role.setName("MODERATOR");

        AccountDTO outputDto = new AccountDTO();
        outputDto.setId(accountId);

        when(accountService.getAccount(accountId)).thenReturn(Optional.of(account));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role));
        when(accountService.saveAccount(any(Account.class))).thenReturn(account);
        when(accountMapper.toDTO(account)).thenReturn(outputDto);

        mockMvc.perform(put("/api/admin/accounts/{id}/roles", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId));
    }

    @Test
    void deleteAccount_shouldReturnOk() throws Exception {
        Long accountId = 1L;
        when(accountRepository.existsById(accountId)).thenReturn(true);

        mockMvc.perform(delete("/api/admin/accounts/{id}", accountId))
                .andExpect(status().isOk());
    }

    @Test
    void lockUser_shouldReturnOk() throws Exception {
        Long id = 1L;
        Account account = new Account();
        when(accountService.getAccount(id)).thenReturn(Optional.of(account));

        mockMvc.perform(post("/api/admin/users/{id}/lock", id))
                .andExpect(status().isOk());
    }

    @Test
    void deleteFeed_shouldReturnOk() throws Exception {
        UUID feedId = UUID.randomUUID();
        when(feedService.feedExists(feedId)).thenReturn(true);

        mockMvc.perform(delete("/api/admin/feeds/{id}", feedId))
                .andExpect(status().isOk());
    }
}
