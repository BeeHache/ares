package net.blackhacker.ares.model;


import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.blackhacker.ares.utils.URLConverter;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;


@Entity
@Table(name = "feeds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Feed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Convert(converter = URLConverter.class)
    private URL url;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column
    @Convert(converter = URLConverter.class)
    private URL link;

    @Column
    private boolean isPodcast = false;

    @Column(nullable = false)
    private LocalDateTime lastModified;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id")
    private Image image;

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedItem> items = new ArrayList<>();

    @ManyToMany(mappedBy = "feeds")
    private List<User> users = new ArrayList<>();

    public void setLinkString(String link) {
        if (link == null) {
            this.link = null;
            return;
        }
        try {
            this.link = URI.create(link).toURL();
        }catch (MalformedURLException | IllegalArgumentException e) {
            this.link = null;
        }
    }
}
