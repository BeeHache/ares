package net.blackhacker.ares.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.utils.URLConverter;

import java.net.URI;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

@Slf4j
@Entity
@Table(name = "feed_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    @Convert(converter = URLConverter.class)
    private URL link;

    @Column
    private ZonedDateTime date;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id")
    private Image image;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @OneToMany(mappedBy = "feedItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Enclosure> enclosures = new HashSet<>();

    public void setLinkFromString(String linkString) {
        try {
            link = new URI(linkString).toURL();
        }catch (Exception e) {
            log.error("Error parsing link from string", e);
            link = null;
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeedItem feedItem = (FeedItem) o;
        if (link != null && feedItem.link != null) return link.equals(feedItem.link);
        if (id != null && feedItem.id != null) return id.equals(feedItem.id);
        return super.equals(o);
    }

    public int hashCode() {
        if (link != null) return link.hashCode();
        if (id != null) return id.hashCode();
        return super.hashCode();
    }
}
