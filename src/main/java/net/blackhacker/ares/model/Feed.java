package net.blackhacker.ares.model;


import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    @Column(nullable = false, unique = true)
    @Convert(converter = URLConverter.class)
    private URL url;

    @Column
    @Convert(converter = BooleanConverter.class)
    private boolean isPodcast = false;

    @Column(nullable = false)
    private ZonedDateTime lastModified = ZonedDateTime.now();

    @OneToOne(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private FeedImage feedImage;

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
        return Objects.equals(url, feed.url);
    }
    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : super.hashCode();
    }
}
