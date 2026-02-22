package net.blackhacker.ares.projection;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface FeedSummaryProjection extends Serializable {
    String getId();
    String getTitle();
    String getDescription();
    String getLink();
    String getImageUrl();
    Boolean getIsPodcast();
}
