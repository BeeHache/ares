package net.blackhacker.ares.repository;

import net.blackhacker.ares.model.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

    @Query("SELECT f FROM Feed f WHERE f.lastModified > :localDateTime ORDER BY f.lastModified ASC LIMIT :limit")
    List<Feed> findByLastModifiedAfter(@Param("localDateTime") LocalDateTime localDateTime, @Param("limit") int limit);

    @Query("SELECT f FROM Feed f WHERE f.link = :link")
    Feed findByLink(@Param("link")  String link);
}
