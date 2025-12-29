package net.blackhacker.ares.repository;

import net.blackhacker.ares.model.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

    @Query("SELECT f FROM Feed f WHERE f.lastModified > ?1 ORDER BY f.lastModified ASC LIMIT ?2")
    List<Feed> findByLastModifiedAfter(LocalDateTime localDateTime, int limit);

    @Query("SELECT f FROM Feed f WHERE f.link = ?1")
    Feed findByLink(String link);
}
