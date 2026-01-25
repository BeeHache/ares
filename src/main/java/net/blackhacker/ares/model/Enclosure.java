package net.blackhacker.ares.model;


import jakarta.persistence.*;
import lombok.Data;
import net.blackhacker.ares.utils.URLConverter;

import java.net.URL;
import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "enclosures")
@Data
public class Enclosure {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    @Convert(converter = URLConverter.class)
    private URL url;

    @Column
    private Long length;

    @Column
    private String type;

    @ManyToOne
    @JoinColumn(name = "feed_item_id")
    private FeedItem feedItem;


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Enclosure enclosure = (Enclosure) o;
        return Objects.equals(getUrl().toString(), enclosure.getUrl().toString());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUrl().toString());
    }
}
