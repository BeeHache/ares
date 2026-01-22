package net.blackhacker.ares.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ImageDTO {
    private UUID id;
    private String contentType;
    private byte[] data;
}
