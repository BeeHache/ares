package net.blackhacker.ares.projection;

import java.time.ZonedDateTime;

public interface AccountProjection {
    enum AccountType {
        ADMIN, USER
    }

    default Long getId() { return null;}
    default String getUsername() { return null; };
    default AccountType getType() { return null; };
    default ZonedDateTime getAccountEnabledAt() { return null; };
    default ZonedDateTime getAccountLockedUntil() { return null; };
    default ZonedDateTime getAccountExpiresAt() { return null; };

}
