package net.blackhacker.ares.projection;

import java.time.Instant;

public interface FeedItemProjection {
    String getTitle();
    String getDescription();
    String getLink();
    Instant getDate();
}
