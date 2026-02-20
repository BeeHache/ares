package net.blackhacker.ares.model;


import jakarta.persistence.*;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.utils.BooleanConverter;
import net.blackhacker.ares.utils.URLConverter;

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
    @Convert(converter = URLConverter.class)
    private URL imageUrl;

    @Column
    @Convert(converter = BooleanConverter.class)
    private boolean podcast = false;

    @Column
    private ZonedDateTime pubdate;

    @Column(nullable = false)
    private ZonedDateTime lastModified = ZonedDateTime.now();

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL)
    private Set<FeedItem> feedItems = new HashSet<>();

    public void setUrlFromString(String urlString) {
        try {
            this.url = new URI(urlString).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            log.error("Could not create URL from {}", urlString,e);
        }
    }

    public void setLinkFromString(String urlString) {
        try {
            this.link = new URI(urlString).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            log.error(" {}", urlString,e);
        }
    }

    public void setImageUrlFromString(String urlString) {
        try {
            if (urlString==null) {
                return;
            }
            this.imageUrl = new URI(urlString).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            log.error("Could not create URL from {}", urlString,e);
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
}
