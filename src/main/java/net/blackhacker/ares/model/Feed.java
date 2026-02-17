package net.blackhacker.ares.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.utils.BooleanConverter;
import net.blackhacker.ares.utils.FeedDtoType;
import net.blackhacker.ares.utils.URLConverter;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.*;


@Slf4j
@Entity
@Table(name = "feeds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Feed implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String title;

    @Column
    private String description;

    @Column(nullable = false, unique = true)
    @Convert(converter = URLConverter.class)
    private URL url;

    @Column
    @Convert(converter = URLConverter.class)
    private URL link;

    @Column
    @Convert(converter = BooleanConverter.class)
    private boolean podcast = false;

    @Column(nullable = false)
    private ZonedDateTime lastModified = ZonedDateTime.now();

    @OneToOne(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private FeedImage feedImage;

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL)
    private Set<FeedItem> feedItems = new TreeSet<>(new Comparator<FeedItem>() {
        @Override
        public int compare(@NonNull FeedItem o1, @NonNull FeedItem o2) {
            if (o1.getDate() == null) {
                return o2.getDate() == null ? 0 : -1;
            } else if (o2.getDate() == null) {
                return 1;
            }
            return - o1.getDate().compareTo(o2.getDate());
        }
    });

    @Type(FeedDtoType.class)
    @Column(name = "dto",columnDefinition = "JSONB")
    private FeedDTO dto;

    public void setUrlFromString(String urlString) {
        try {
            this.url = new URI(urlString).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            log.error(e.getMessage());
        }
    }


    public void touch() {
        this.lastModified = ZonedDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feed feed = (Feed) o;
        if (id != null && feed.getId() != null) return Objects.equals(id, feed.getId());
        if (url != null && feed.getUrl() != null) return Objects.equals(url, feed.getUrl());
        return false;
    }
    @Override
    public int hashCode() {
        if (id != null) return id.hashCode();
        if (url != null) return url.toString().hashCode();
        return super.hashCode();
    }

    public ZonedDateTime getPubdate() {
        if (!getFeedItems().iterator().hasNext()) {
            return ZonedDateTime.now();
        }
        return getFeedItems().iterator().next().getDate();
    }
}
