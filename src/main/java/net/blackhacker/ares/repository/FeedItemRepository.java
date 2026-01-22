package net.blackhacker.ares.repository;

import net.blackhacker.ares.model.FeedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FeedItemRepository extends JpaRepository<FeedItem, UUID> {
}
