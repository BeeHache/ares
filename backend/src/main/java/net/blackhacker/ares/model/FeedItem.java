package net.blackhacker.ares.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.utils.URLConverter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.io.Serializable;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.*;

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

    @Column(length = 255)
    private String guid;

    @Column(length = 255)
    private String title;

    @Column
    private String description;

    @Column(length = 512)
    @Convert(converter = URLConverter.class)
    private URL link;

    @Column
    private ZonedDateTime date;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name="feed_item_id")
    @Fetch(FetchMode.SUBSELECT)
    private Set<Enclosure> enclosures = new HashSet<>();

    @ManyToOne
    @JoinColumn(name="feed_id")
    private Feed feed;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FeedItem that)) return false;

        if (guid != null && that.guid != null) {
            return Objects.equals(guid, that.guid);
        }

        UUID thisFeedId = (feed != null) ? feed.getId() : null;
        UUID thatFeedId = (that.feed != null) ? that.feed.getId() : null;

        return Objects.equals(title, that.title) &&
               Objects.equals(thisFeedId, thatFeedId);
    }

    @Override
    public int hashCode() {
        if (guid != null) {
            return Objects.hash(guid);
        }
        UUID feedId = (feed != null) ? feed.getId() : null;
        return Objects.hash(title, feedId);
    }
}
