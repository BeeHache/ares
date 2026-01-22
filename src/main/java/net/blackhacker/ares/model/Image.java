package net.blackhacker.ares.model;


import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "image")
@Data
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "data", nullable = false, columnDefinition = "BYTEA")
    private byte[] data;
}
