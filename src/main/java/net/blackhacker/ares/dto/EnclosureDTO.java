package net.blackhacker.ares.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class EnclosureDTO implements Serializable {
    private UUID id;
    private String url;
    private Long length;
    private String type;
}
