package net.blackhacker.ares.repository.jpa;

import lombok.NonNull;
import net.blackhacker.ares.model.FeedItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.net.URL;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedItemRepository extends JpaRepository<FeedItem, UUID> {

    @Query(value = "SELECT * from feed_items where feed_id=:feedId",
            countQuery = "SELECT count(*) from feed_items where feed_id=:feedId",
            nativeQuery = true)
    Slice<FeedItem> findByFeedId(@Param("feedId")UUID feedId, Pageable pageable);

    Optional<FeedItem> findByLink(URL link);

    Optional<FeedItem> findByGuid(String guid);

    @Query(value="SELECT * from feed_items f where f.feed_id=:feed_id and f.title=:title", nativeQuery = true)
    Optional<FeedItem> findByFeedAndTitle(@Param("feed_id")UUID feedId, @Param("title")String title);
}
