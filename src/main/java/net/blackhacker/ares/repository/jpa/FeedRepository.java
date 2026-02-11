package net.blackhacker.ares.repository.jpa;

import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.FeedTitleDTO;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedImage;
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

    @Query("SELECT f FROM Feed f WHERE f.lastModified < :dt OR f.lastModified IS NULL")
    Page<Feed> findModifiedBefore(@Param("dt") ZonedDateTime zonedDateTime, Pageable pageable);

    Optional<Feed> findByUrl(@Param("url") URL url);

    Optional<Feed> findById(@Param("id") UUID id);

    @Query(value = "SELECT f.id, f.dto ->> 'title' AS title, f.dto ->> 'imageUrl' AS imageUrl, f.dto ->> 'isPodcast' as isPodcast FROM subscriptions s INNER JOIN feeds f ON s.feed_id = f.id WHERE s.user_id = :userid", nativeQuery = true)
    Collection<FeedTitleDTO> findFeedTitlesByUserId(@Param("userid") Long userId);

    @Query(value="SELECT f.dto FROM Feed f WHERE f.id=:feed_id")
    Optional<FeedDTO> getFeedDTOById(@Param("feed_id") UUID feedId);

    @Query(value = "SELECT f.feedImage from Feed f where f.id=:feed_id")
    Optional<FeedImage> getFeedImageById(@Param("feed_id") UUID feedId);
}
