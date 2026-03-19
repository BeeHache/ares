package net.blackhacker.ares.projection;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.beans.factory.annotation.Value;

import java.io.Serializable;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface FeedTitleProjection extends Serializable {
    default String getId() { return null; }
    default String getTitle() { return null; }

    @Value("#target.podcast")
    default String getPodcastString() { return null; }
    default boolean getPodcast(){
        return getPodcastString() != null && getPodcastString().equalsIgnoreCase("Y");
    }
    default String getImageUrl() { return null; }
    default Instant getPubdate() { return null; }
}
