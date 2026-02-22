package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.AccountDTO;
import net.blackhacker.ares.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AccountMapperTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountMapper accountMapper;

    @BeforeEach
    void setUp() {
        accountMapper = new AccountMapper(passwordEncoder);
    }

    @Test
    void toDTO_shouldMapAccountToAccountDTO() {
        // Arrange
        Account account = new Account();
        account.setUsername("testuser");
        account.setType(Account.AccountType.USER);
        // Password is not mapped to DTO for security usually, but let's check the implementation
        // AccountMapper.toDTO only maps username and type.

        // Act
        AccountDTO dto = accountMapper.toDTO(account);

        // Assert
        assertNotNull(dto);
        assertEquals("testuser", dto.getUsername());
        assertEquals("USER", dto.getType());
        assertNull(dto.getPassword()); // Password should not be exposed in DTO
    }

    @Test
    void toModel_shouldMapAccountDTOToAccount() {
        // Arrange
        AccountDTO dto = new AccountDTO();
        dto.setUsername("newuser");
        dto.setPassword("plainPassword");
        dto.setType("ADMIN");

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

        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> accountMapper.toModel(dto));
    }
}
