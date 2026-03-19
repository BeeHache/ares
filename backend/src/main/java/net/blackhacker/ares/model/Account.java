package net.blackhacker.ares.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NonNull;
import net.blackhacker.ares.projection.AccountProjection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name ="accounts")
@Data
public class Account implements AccountProjection, UserDetails {

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

    @Column(name = "account_expires_at")
    private ZonedDateTime accountExpiresAt;

    @Column(name = "password_expires_at")
    private ZonedDateTime passwordExpiresAt;

    @Column(name = "account_locked_until")
    private ZonedDateTime accountLockedUntil;

    @Column(name = "account_enabled_at")
    private ZonedDateTime accountEnabledAt;

    @Column(name = "last_login")
    private ZonedDateTime lastLogin;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts;

    @Column(name = "last_modified")
    private ZonedDateTime lastModified;


    public Account() {
    }

    public Account(String username, String password, AccountType type, Collection<Role> rolls) {
        this.username = username;
        this.password = password;
        this.type = type;
        this.roles = rolls;
    }

    @Override
    public boolean isAccountNonExpired() {
        if (accountExpiresAt == null) {
            return true;
        }
        return accountExpiresAt.isAfter(ZonedDateTime.now());
    }

    @Override
    public boolean isAccountNonLocked() {
        if (accountLockedUntil == null) {
            return true;
        }
        return accountLockedUntil.isAfter(ZonedDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        if (passwordExpiresAt == null) {
            return true;
        }
        return passwordExpiresAt.isAfter(ZonedDateTime.now());
    }

    @Override
    public boolean isEnabled() {
        if (accountEnabledAt == null) {
            return false;
        }

        return accountEnabledAt.isBefore(ZonedDateTime.now());
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "account_roles",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Collection<Role> roles = new ArrayList<>();

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // This maps your Role entities to Spring Security authorities
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }

    public void lockAccount(){
        accountLockedUntil = ZonedDateTime.now().plusMinutes(15);
    }

    public void enableAccount(){
        ZonedDateTime now = ZonedDateTime.now();
        accountEnabledAt = now;
        accountLockedUntil = now;
    }
}
