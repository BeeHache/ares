package net.blackhacker.ares.model;


import jakarta.persistence.*;
import lombok.Data;
import net.blackhacker.ares.utils.URLConverter;

import java.net.URL;


@Entity
@Table(name = "enclosures")
@Data
public class Enclosure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

}
