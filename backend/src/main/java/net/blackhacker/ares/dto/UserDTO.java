package net.blackhacker.ares.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class UserDTO implements Serializable {
    private String email;
    private String password;
    private Set<FeedDTO> feeds;
}
