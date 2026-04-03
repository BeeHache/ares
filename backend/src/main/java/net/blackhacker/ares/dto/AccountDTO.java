package net.blackhacker.ares.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDTO implements Serializable {
    private Long id;
    private String username; // This will be the email for both USER and ADMIN accounts
    private String password;
    private String name; // For Admin accounts, this is the display name
    private String type;
    private String accountExpiresAt;
    private String passwordExpiresAt;
    private String accountLockedUntil;
    private String accountEnabledAt;
    private List<RoleDTO> roles;
}
