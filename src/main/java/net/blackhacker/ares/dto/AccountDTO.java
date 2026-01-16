package net.blackhacker.ares.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AccountDTO implements Serializable {
    private String username;
    private String password;
    private String type;
}
