package net.blackhacker.ares.repository;

import net.blackhacker.ares.dto.FeedTitleDTO;
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

    @Query("SELECT f FROM Feed f WHERE f.lastModified < :dt OR f.lastModified IS NULL")
    Page<Feed> findModifiedBefore(@Param("dt") ZonedDateTime zonedDateTime, Pageable pageable);

    Optional<Feed> findByUrl(@Param("url") URL url);

    @Query(value = "SELECT f.id, f.title, f.image_url FROM subscriptions s,  feeds f WHERE s.feed_id=f.id and s.user_id = :userid", nativeQuery = true)
    Collection<FeedTitleDTO> findFeedTitlesByUserId(@Param("userid") Long userId);

    @Query(value="SELECT f.jsonData FROM feeds f WHERE f.id=:feed_id")
    Optional<String> getJsonDataById(@Param("feed_id") UUID feedId);
}
