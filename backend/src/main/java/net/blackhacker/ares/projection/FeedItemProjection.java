package net.blackhacker.ares.projection;

import java.time.Instant;

public interface FeedItemProjection {
    default String getTitle() { return null; }
    default String getDescription() { return null; }
    default String getLink() { return null; }
    default Instant getDate() { return null; }
}
