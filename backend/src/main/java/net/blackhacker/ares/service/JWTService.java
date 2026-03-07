package net.blackhacker.ares.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.Admins;
import net.blackhacker.ares.repository.jpa.AdminsRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JWTService {

    @Value("${security.jwt.secret}")
    private String SECRET_KEY;

    @Value("${security.jwt.access_expiration_ms:3600000}") // default is 1 hour
    private Long EXPIRATION_TIME;

    private final ObjectProvider<RoleHierarchy> roleHierarchyProvider;
    private final AdminsRepository adminsRepository;

    public JWTService(ObjectProvider<RoleHierarchy> roleHierarchyProvider, AdminsRepository adminsRepository) {
        this.roleHierarchyProvider = roleHierarchyProvider;
        this.adminsRepository = adminsRepository;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }

    // 1. Generate Token
    public String generateToken(Account account) {
        RoleHierarchy roleHierarchy = roleHierarchyProvider.getObject(); // Get lazily

        // Expand roles based on hierarchy
        Collection<? extends GrantedAuthority> authorities = account.getAuthorities();
        Collection<? extends GrantedAuthority> reachableAuthorities = roleHierarchy.getReachableGrantedAuthorities(authorities);

        List<String> roles = reachableAuthorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String name = null;
        if (roles.contains("ROLE_ADMIN")) {
            Optional<Admins> admin = adminsRepository.findByAccount(account);
            if (admin.isPresent()) {
                name = admin.get().getName();
            }
        }

        var builder = Jwts.builder()
                .subject(account.getUsername())
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey());
        
        if (name != null) {
            builder.claim("name", name);
        }

        return builder.compact();
    }

    // 2. Extract Username
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 3. Validate Token
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }
}
