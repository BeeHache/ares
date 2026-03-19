package net.blackhacker.ares.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDTO implements Serializable {
    private Long id;
    private String username;
    private String password;
    private String type;
    private String accountExpiresAt;
    private String passwordExpiresAt;
    private String accountLockedUntil;
    private String accountEnabledAt;
}
