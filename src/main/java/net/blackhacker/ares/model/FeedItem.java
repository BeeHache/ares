package net.blackhacker.ares.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.Collection;

@Entity
@Table(name = "feed_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    private String link;

    @Column
    private ZonedDateTime date;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id")
    private Image image;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @OneToMany(mappedBy = "feedItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Enclosure> enclosures;

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
