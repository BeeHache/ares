package net.blackhacker.ares.repository.jpa;

import net.blackhacker.ares.projection.FeedItemProjection;
import net.blackhacker.ares.projection.FeedSummaryProjection;
import net.blackhacker.ares.projection.FeedTitleProjection;
import net.blackhacker.ares.model.Feed;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedRepository extends JpaRepository<Feed, UUID> {

    String FIND_MODIFIED_BEFORE = "SELECT f FROM Feed f WHERE f.lastModified < :dt OR f.lastModified IS NULL";
    String  FIND_FEED_TITLES_BY_USERID =
            "SELECT f.id, f.title, f.podcast, " +
                    "(select img.image_url from feed_image img where img.feed_id=f.id) as imageUrl, " +
                    "(select max(i.date) from feed_items i where i.feed_id = f.id) as pubdate " +
                    "FROM subscriptions s INNER JOIN feeds f ON s.feed_id = f.id " +
                    "WHERE s.user_id = :userid";

    String  FIND_FEED_SUMMARIES_BY_USERID =
            "SELECT f.id, f.title, f.podcast, f.description, f.link, " +
                    "(select img.image_url from feed_image img where img.feed_id=f.id) as imageUrl, " +
                    "(select max(i.date) from feed_items i where i.feed_id = f.id) as pubdate " +
                    "FROM subscriptions s INNER JOIN feeds f ON s.feed_id = f.id " +
                    "WHERE s.user_id = :userid";

    String SEARCH_ITEMS =
            "SELECT title, description, link, date " +
            "FROM feed_items " +
            "WHERE search_vector @@ plainto_tsquery('english', :query)";

    @Query(FIND_MODIFIED_BEFORE)
    Page<Feed> findModifiedBefore(@Param("dt") ZonedDateTime zonedDateTime, Pageable pageable);

    Optional<Feed> findByUrl(@Param("url") URL url);

    @Query(value = FIND_FEED_TITLES_BY_USERID, nativeQuery = true)
    Collection<FeedTitleProjection> findFeedTitlesByUserId(@Param("userid") Long userId);

    @Query(value = FIND_FEED_SUMMARIES_BY_USERID, nativeQuery = true)
    Collection<FeedSummaryProjection> findFeedSummariesByUserId(@Param("userid") Long userId);

    @Query(value = SEARCH_ITEMS, nativeQuery = true)
    Collection<FeedItemProjection> searchItems(@Param("query") String query);
}
