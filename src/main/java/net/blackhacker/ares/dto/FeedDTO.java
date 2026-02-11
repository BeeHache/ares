package net.blackhacker.ares.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class FeedDTO implements Serializable {
    private UUID id;
    private String title;
    private String description;
    private String link;
    private Boolean isPodcast = Boolean.FALSE;
    private String imageUrl;
    private List<FeedItemDTO> items = new ArrayList<>();
}
