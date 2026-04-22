package net.blackhacker.ares.repository.jpa;

import net.blackhacker.ares.model.FeedItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedItemRepository extends JpaRepository<FeedItem, UUID> {

    @Query(value = "SELECT f from FeedItem f where f.feed.id=:feedId",
            countQuery = "SELECT count(f) from f.FeedItem where f.feed.id=:feedId")
    Slice<FeedItem> findByFeedId(@Param("feedId")UUID feedId, Pageable pageable);

    @EntityGraph(attributePaths = {"enclosures"})
    Optional<FeedItem> findByGuid(String guid);

    @EntityGraph(attributePaths = {"enclosures"})
    @Query(value="SELECT f from FeedItem f where f.feed.id=:feedId and f.title=:title")
    Optional<FeedItem> findByFeedAndTitle(@Param("feedId")UUID feedId, @Param("title")String title);

    @Query(value = "SELECT max(f.date) from FeedItem f where f.feed.id=:feedId")
    Optional<ZonedDateTime> findLatestDateByFeedId(@Param("feedId")UUID feedId);
}
