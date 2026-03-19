package net.blackhacker.ares.projection;

import java.util.UUID;

public interface FeedProjection {

    UUID getId();
    String getTitle();
    String getDescription();
    String getUrl();
    String getLink();
    Boolean getIsPodcast();
    String getImageUrl();
    Long getSubscribers();
    String getPubdate();
}
