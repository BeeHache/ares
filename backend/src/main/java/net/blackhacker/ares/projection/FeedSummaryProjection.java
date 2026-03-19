package net.blackhacker.ares.projection;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface FeedSummaryProjection extends Serializable {
    default String getId() { return null; }
    default String getTitle() { return null; }
    default String getDescription() { return null; }
    default String getLink() { return null; }
    default String getImageUrl() { return null; }
    default Boolean getIsPodcast() { return null; }
}
