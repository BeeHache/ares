package net.blackhacker.ares.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TokenDTO implements Serializable {
    private String token;

    public static TokenDTO token(String token){
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(token);
        return tokenDTO;
    }
}
