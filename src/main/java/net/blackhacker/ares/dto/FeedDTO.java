package net.blackhacker.ares.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FeedDTO implements Serializable {
    private UUID id;
    private String title;
    private String description;
    private String link;
    private Boolean isPodcast = Boolean.FALSE;
    private String imageUrl;
    private String publishedDate = "1970-01-01T00:00:00Z";
    private List<FeedItemDTO> items = new ArrayList<>();
}
