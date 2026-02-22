package net.blackhacker.ares.projection;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.beans.factory.annotation.Value;

import java.io.Serializable;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface FeedTitleProjection extends Serializable {
    String getId();
    String getTitle();

    @Value("#target.podcast")
    String getPodcastString();
    default boolean getPodcast(){
        return getPodcastString() != null && getPodcastString().equalsIgnoreCase("Y");
    }

    String getImageUrl();

    Instant getPubdate();

}
