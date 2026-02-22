package net.blackhacker.ares.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AdminDTO implements Serializable {
    private String name;
    private String email;
    private String password;
}
