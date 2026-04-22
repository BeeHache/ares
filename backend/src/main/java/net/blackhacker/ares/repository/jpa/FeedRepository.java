package net.blackhacker.ares.repository.jpa;

import lombok.NonNull;
import net.blackhacker.ares.projection.FeedItemProjection;
import net.blackhacker.ares.projection.FeedProjection;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.service.CacheService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
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

    String FIND_MODIFIED_BEFORE = "SELECT f.id FROM Feed f WHERE f.lastModified < :dt OR f.lastModified IS NULL OR f.pubdate IS NULL";

    String  FIND_FEED_USERID =
            "SELECT f.id, f.title, f.description, f.url, f.link, f.podcast, f.subscribers" +
                    "(select img.image_url from feed_image img where img.feed_id=f.id) as imageUrl, " +
                    "f.pubdate " +
                    "FROM subscriptions s INNER JOIN feeds f ON s.feed_id = f.id " +
                    "WHERE s.user_id = :userid";

    String SEARCH_ITEMS =
            "SELECT title, description, link, date " +
            "FROM feed_items " +
            "WHERE search_vector @@ plainto_tsquery('english', :query)";

    String SUBSCRIPTION_COUNT =
            "SELECT count(0) FROM subscriptions WHERE feed_id = :feedId";

    @Query(FIND_MODIFIED_BEFORE)
    Page<UUID> findFeedIdsModifiedBefore(@Param("dt") ZonedDateTime zonedDateTime, Pageable pageable);

    @EntityGraph(attributePaths = {"feedItems"})
    @NonNull Optional<Feed> findById(@NonNull @Param("id") UUID id);

    @EntityGraph(attributePaths = {"feedItems"})
    Optional<Feed> findByUrl(@Param("url") URL url);

    @Query(value = FIND_FEED_USERID, nativeQuery = true)
    Collection<FeedProjection> findFeedProjectionByUserId(@Param("userid") Long userId);

    @Query(value = SEARCH_ITEMS, nativeQuery = true)
    Collection<FeedItemProjection> searchItems(@Param("query") String query);

    @Cacheable(value = CacheService.SUBSCRIPTION_COUNT_CACHE)
    @Query(value = SUBSCRIPTION_COUNT, nativeQuery = true)
    Long findSubscriptionCountByFeedId(@Param("feedId") UUID feedId);

}
