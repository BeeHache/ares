package net.blackhacker.ares.model;

import jakarta.persistence.*;
import lombok.Data;
import net.blackhacker.ares.utils.MediaTypeConverter;
import net.blackhacker.ares.utils.URLConverter;
import org.springframework.http.MediaType;

import java.net.URL;
import java.util.UUID;

@Entity
@Table(name ="feed_image")
@Data
public class FeedImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    @Convert(converter = URLConverter.class)
    private URL imageUrl;

    @Column
    @Convert(converter = MediaTypeConverter.class)
    private MediaType contentType;

    @Column(name = "data") // Map to 'data' column in DB
    private byte[] content;

    @OneToOne
    @JoinColumn(name="feed_id", nullable = false, unique = true)
    private Feed feed;
}
