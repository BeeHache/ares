package net.blackhacker.ares.repository.jpa;

import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.FeedSummaryDTO;
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

    @Query(Queries.FIND_MODIFIED_BEFORE)
    Page<Feed> findModifiedBefore(@Param("dt") ZonedDateTime zonedDateTime, Pageable pageable);

    Optional<Feed> findByUrl(@Param("url") URL url);

    Optional<Feed> findById(@Param("id") UUID id);

    @Query(value = Queries.FIND_FEED_TITLES_BY_USERID, nativeQuery = true)
    Collection<FeedTitleDTO> findFeedTitlesByUserId(@Param("userid") Long userId);

    @Query(value = Queries.FIND_FEED_SUMMARIES_BY_USERID, nativeQuery = true)
    Collection<FeedSummaryDTO> findFeedSummariesByUserId(@Param("userid") Long userId);

    @Query(value=Queries.GET_FEED_DTO_BY_ID)
    Optional<FeedDTO> getFeedDTOById(@Param("feed_id") UUID feedId);

    @Query(value = Queries.GET_FEED_IMAGE_BY_ID)
    Optional<FeedImage> getFeedImageById(@Param("feed_id") UUID feedId);
}
