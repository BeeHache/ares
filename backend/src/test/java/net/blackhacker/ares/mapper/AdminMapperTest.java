package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.AdminDTO;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.Admins;
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
class AdminMapperTest {


    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminMapper adminMapper;

    private Account account;
    private Admins admin;
    private AdminDTO adminDTO;


    @BeforeEach
    void setUp() {
        account = new Account();
        account.setUsername("admin@example.com");
        account.setPassword("encodedPassword");
        account.setType(Account.AccountType.ADMIN);

        admin = new Admins();
        admin.setName("Admin Name");
        admin.setEmail("admin@example.com");
        admin.setAccount(account);

        adminDTO = new AdminDTO();
        adminDTO.setName("Admin Name");
        adminDTO.setEmail("admin@example.com");
        adminDTO.setPassword("password");
    }

    @Test
    void toDTO_shouldReturnNull_whenAdminIsNull() {
        assertNull(adminMapper.toDTO(null));
    }

    @Test
    void toDTO_shouldMapAdminToAdminDTO() {
        // Arrange


        // Act
        AdminDTO result = adminMapper.toDTO(admin);

        // Assert
        assertNotNull(result);
        assertEquals(admin.getName(), result.getName());
        assertEquals(admin.getEmail(), result.getEmail());
        assertNull(result.getPassword()); // Password should not be mapped to DTO
    }

    @Test
    void toDTO_shouldHandleNullRoles() {
        // Act
        AdminDTO result = adminMapper.toDTO(admin);

        // Assert
        assertNotNull(result);
        assertEquals(admin.getName(), result.getName());
        assertEquals(admin.getEmail(), result.getEmail());
        assertNull(result.getPassword()); // Password should not be mapped to DTO
    }

    @Test
    void toModel_shouldReturnNull_whenAdminDTOIsNull() {
        assertNull(adminMapper.toModel(null));
    }

    @Test
    void toModel_shouldMapAdminDTOToAdmin() {

        String encodedPassword = "encodedPassword";
        when(passwordEncoder.encode("password")).thenReturn(encodedPassword);

        // Act
        Admins result = adminMapper.toModel(adminDTO);

        // Assert
        assertNotNull(result);
        assertEquals(adminDTO.getName(), result.getName());
        assertEquals(adminDTO.getEmail(), result.getEmail());
        assertEquals(encodedPassword, result.getAccount().getPassword());
        assertNotNull(result.getAccount());
        assertEquals(Account.AccountType.ADMIN, result.getAccount().getType());
        }
}
