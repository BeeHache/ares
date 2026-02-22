package net.blackhacker.ares.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.utils.URLConverter;

import java.net.URL;
import java.util.UUID;

@Slf4j
@Entity
@Table(name = "enclosures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Enclosure {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    @Convert(converter = URLConverter.class)
    private URL url;

    @Column
    private Long length;

    @Column(nullable = false)
    private String type;
}
