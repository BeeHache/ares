package net.blackhacker.ares.projection;

import java.util.UUID;

public interface FeedProjection {

    UUID getId();
    String getTitle();
    String getDescription();
    String getLink();
    boolean isPodcast();
    String getImageUrl();
}
