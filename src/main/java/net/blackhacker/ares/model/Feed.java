package net.blackhacker.ares.model;


import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;


@Entity
@Table(name = "FEEDS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Feed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(nullable = false)
    private String link;

    @Column
    private String image;

    @Column(nullable = false)
    private boolean isPodcast = false;

    @Column(nullable = false)
    private LocalDateTime lastModified;

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedItem> items = new ArrayList<>();

    @ManyToMany(mappedBy = "feeds")
    private Set<User> users = new HashSet<>();

}
