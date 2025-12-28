package net.blackhacker.ares.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FeedDTO implements Serializable {
    private String title;
    private String description;
    private String link;
    private String image;
    private boolean isPodcast;
    private LocalDateTime lastModified;
    private List<FeedItemDTO> items;
}
