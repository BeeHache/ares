package net.blackhacker.ares.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.repository.jpa.AdminsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JWTServiceTest {

    @Mock
    ObjectProvider<RoleHierarchy> roleHierarchyProvider;

    @Mock
    RoleHierarchy roleHierarchy;

    @Mock
    AdminsRepository adminsRepository;

    @InjectMocks
    private JWTService jwtService;

    private final String SECRET_KEY = "aS1hbS1hLXNlY3JldC1rZXktZm9yLWFyZXMtdGhhdC1pcy1sb25nLWVub3VnaA==";
    private final Long EXPIRATION_TIME = 3600000L; // 1 hour

    private Collection<GrantedAuthority> authorities;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "EXPIRATION_TIME", EXPIRATION_TIME);

        GrantedAuthority userRole = new GrantedAuthority(){
            @Override
            public String getAuthority() {
                return "ROLE_USER";
            }
        };

        GrantedAuthority adminRole = new GrantedAuthority(){
            @Override
            public String getAuthority() {
                return "ROLE_ADMIN";
            }
        };

        authorities = List.of(userRole, adminRole);
    }

    private Account createTestAccount() {
        Account account = new Account();
        account.setUsername("testuser");
        account.setPassword("password");
        account.setType(Account.AccountType.USER);
        return account;
    }

    @Test
    void generateToken_shouldReturnToken() {
        Account account = createTestAccount();
        when(roleHierarchyProvider.getObject()).thenReturn(roleHierarchy);
        // Use doReturn to avoid generic wildcard capture issues
        doReturn(authorities).when(roleHierarchy).getReachableGrantedAuthorities(any());
        String token = jwtService.generateToken(account);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername_shouldReturnUsername() {
        Account account = createTestAccount();
        when(roleHierarchyProvider.getObject()).thenReturn(roleHierarchy);
        // Use doReturn to avoid generic wildcard capture issues
        doReturn(authorities).when(roleHierarchy).getReachableGrantedAuthorities(any());
        String token = jwtService.generateToken(account);
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void isTokenValid_shouldReturnTrue_whenTokenIsValid() {
        Account account = createTestAccount();
        when(roleHierarchyProvider.getObject()).thenReturn(roleHierarchy);
        // Use doReturn to avoid generic wildcard capture issues
        doReturn(authorities).when(roleHierarchy).getReachableGrantedAuthorities(any());
        String token = jwtService.generateToken(account);
        assertTrue(jwtService.isTokenValid(token, account));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenUsernameDoesNotMatch() {
        Account account = createTestAccount();
        when(roleHierarchyProvider.getObject()).thenReturn(roleHierarchy);
        // Use doReturn to avoid generic wildcard capture issues
        doReturn(authorities).when(roleHierarchy).getReachableGrantedAuthorities(any());
        String token = jwtService.generateToken(account);
        UserDetails otherUser = new User("otheruser", "password", Collections.emptyList());
        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsExpired() {
        Account account = createTestAccount();
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        String expiredToken = Jwts.builder()
                .subject(account.getUsername())
                .issuedAt(new Date(System.currentTimeMillis() - 20000)) // 20 seconds ago
                .expiration(new Date(System.currentTimeMillis() - 10000)) // 10 seconds ago
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();

        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> jwtService.isTokenValid(expiredToken, account));
    }
}
