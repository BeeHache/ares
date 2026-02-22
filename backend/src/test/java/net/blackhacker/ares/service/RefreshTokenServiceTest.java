package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.RefreshToken;
import net.blackhacker.ares.repository.crud.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private Account account;
    private RefreshToken refreshToken;
    private String tokenString = "test-token";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 3600000L);

        account = new Account();
        account.setUsername("testuser");

        refreshToken = new RefreshToken();
        refreshToken.setToken(tokenString);
        refreshToken.setUsername("testuser");
    }

    @Test
    void saveRefreshToken_shouldCallRepositorySave() {
        refreshTokenService.saveRefreshToken(refreshToken);
        verify(refreshTokenRepository, times(1)).save(refreshToken);
    }

    @Test
    void findByToken_shouldReturnToken_whenExists() {
        when(refreshTokenRepository.findById(tokenString)).thenReturn(Optional.of(refreshToken));

        Optional<RefreshToken> result = refreshTokenService.findByToken(tokenString);

        assertTrue(result.isPresent());
        assertEquals(tokenString, result.get().getToken());
    }

    @Test
    void findByToken_shouldReturnEmpty_whenNotExists() {
        when(refreshTokenRepository.findById("non-existent")).thenReturn(Optional.empty());

        Optional<RefreshToken> result = refreshTokenService.findByToken("non-existent");

        assertFalse(result.isPresent());
    }

    @Test
    void deleteRefreshToken_shouldCallRepositoryDelete() {
        refreshTokenService.deleteRefreshToken(tokenString);
        verify(refreshTokenRepository, times(1)).deleteById(tokenString);
    }

    @Test
    void generateToken_shouldCreateAndSaveToken() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result = refreshTokenService.generateToken(account);

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals(account.getUsername(), result.getUsername());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }
}
