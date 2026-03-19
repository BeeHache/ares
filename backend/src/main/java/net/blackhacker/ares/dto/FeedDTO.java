package net.blackhacker.ares.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import net.blackhacker.ares.projection.FeedProjection;

import java.io.Serializable;
import java.util.*;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FeedDTO implements FeedProjection, Serializable {
    private UUID id;
    private String title;
    private String description;
    private String url;
    private String link;
    private Boolean isPodcast = Boolean.FALSE;
    private String imageUrl;
    private Long subscribers = null;
    private String pubdate = null;

    private Set<FeedItemDTO> items = new TreeSet<>();
}
