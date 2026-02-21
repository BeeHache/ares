package net.blackhacker.ares.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EnclosureDTO implements Serializable {
    private String url;
    private Long length;
    private String type;
}
