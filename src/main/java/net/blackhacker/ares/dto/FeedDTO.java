package net.blackhacker.ares.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

@Data
public class FeedDTO implements Serializable {
    private Long id;
    private String title;
    private String description;
    private String link;
    private boolean isPodcast;
    private ZonedDateTime lastModified;
    private ImageDTO image;
    private List<FeedItemDTO> items;
}
