package net.blackhacker.ares.repository;

import net.blackhacker.ares.model.Feed;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

    @Query("SELECT f FROM Feed f WHERE f.lastTouched < :dt OR f.lastTouched IS NULL")
    Page<Feed> findTouchedBefore(@Param("dt") ZonedDateTime zonedDateTime, Pageable pageable);

    Feed findByLink(@Param("link")  String link);
}
