package net.blackhacker.ares.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name ="accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account implements UserDetails {

    public enum AccountType {
        ADMIN, USER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType type;

    @Column(nullable = false, unique = true)
    private String token;

    @Column
    private LocalDateTime accountExpiresAt;

    @Column
    private LocalDateTime passwordExpiresAt;

    @Column
    private LocalDateTime accountLockedUntil;

    @Column
    private LocalDateTime accountEnabledAt;

    @Column
    private LocalDateTime tokenExpiresAt;

    @Override
    public boolean isAccountNonExpired() {
        if (accountExpiresAt == null) {
            return true;
        }
        return accountExpiresAt.isAfter(LocalDateTime.now());
    }

    @Override
    public boolean isAccountNonLocked() {
        if (accountLockedUntil == null) {
            return true;
        }
        return accountLockedUntil.isAfter(LocalDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        if (passwordExpiresAt == null) {
            return true;
        }
        return passwordExpiresAt.isAfter(LocalDateTime.now());
    }

    @Override
    public boolean isEnabled() {
        if (accountEnabledAt == null) {
            return false;
        }
        return accountEnabledAt.isBefore(LocalDateTime.now());
    }

    public boolean isTokenExpired(){
        if (tokenExpiresAt == null){
            return false;
        }

        return tokenExpiresAt.isBefore(LocalDateTime.now());
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "account_roles",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Collection<Role> roles = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // This maps your Role entities to Spring Security authorities
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }
}
