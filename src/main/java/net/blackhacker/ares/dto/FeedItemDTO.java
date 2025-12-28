package net.blackhacker.ares.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class FeedItemDTO implements Serializable {
    private String title;
    private String description;
    private String link;
    private String image;
    private LocalDateTime date;
}
