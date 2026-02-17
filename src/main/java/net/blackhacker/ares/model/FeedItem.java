package net.blackhacker.ares.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.utils.URLConverter;

import java.io.Serializable;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Entity
@Table(name = "feed_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedItem implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String title;

    @Column
    private String description;

    @Column(unique = true)
    @Convert(converter = URLConverter.class)
    private URL link;

    @Column
    private ZonedDateTime date;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name="feed_item_id")
    private Collection<Enclosure> enclosures = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name="feed_id")
    private Feed feed;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FeedItem feedItem = (FeedItem) o;
        return Objects.equals(getId(), feedItem.getId()) && Objects.equals(getLink(), feedItem.getLink());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getLink());
    }
}
