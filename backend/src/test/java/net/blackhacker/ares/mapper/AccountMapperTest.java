package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.AccountDTO;
import net.blackhacker.ares.dto.RoleDTO;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AccountMapperTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ObjectProvider<PasswordEncoder> objectProvider;

    @Mock
    private RoleMapper roleMapper;

    private AccountMapper accountMapper;

    @BeforeEach
    void setUp() {
        accountMapper = new AccountMapper(objectProvider, roleMapper);
    }

    @Test
    void toDTO_shouldMapAccountToAccountDTO() {
        // Arrange
        Account account = new Account();
        account.setUsername("testuser");
        account.setType(Account.AccountType.USER);
        
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");
        account.setRoles(Set.of(role));

        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(1L);
        roleDTO.setName("USER");

        when(roleMapper.toDTO(any(Role.class))).thenReturn(roleDTO);

        // Act
        AccountDTO dto = accountMapper.toDTO(account);

        // Assert
        assertNotNull(dto);
        assertEquals("testuser", dto.getUsername());
        assertEquals("USER", dto.getType());
        assertNull(dto.getPassword());
        assertNotNull(dto.getRoles());
        assertEquals(1, dto.getRoles().size());
        assertEquals("USER", dto.getRoles().get(0).getName());
    }

    @Test
    void toModel_shouldMapAccountDTOToAccount() {
        // Arrange
        AccountDTO dto = new AccountDTO();
        dto.setUsername("newuser");
        dto.setPassword("plainPassword");
        dto.setType("ADMIN");

        when(objectProvider.getObject()).thenReturn(passwordEncoder);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");

        // Act
        Account account = accountMapper.toModel(dto);

        // Assert
        assertNotNull(account);
        assertEquals("newuser", account.getUsername());
        assertEquals("encodedPassword", account.getPassword());
        assertEquals(Account.AccountType.ADMIN, account.getType());
    }

    @Test
    void toModel_shouldThrowException_whenTypeIsInvalid() {
        // Arrange
        AccountDTO dto = new AccountDTO();
        dto.setUsername("user");
        dto.setPassword("pass");
        dto.setType("INVALID_TYPE");

        // In the current implementation, toModel creates the account before validation
        // The exception happens at Account.AccountType.valueOf
        when(objectProvider.getObject()).thenReturn(passwordEncoder);
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> accountMapper.toModel(dto));
    }
}
