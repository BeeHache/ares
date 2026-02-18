package net.blackhacker.ares.repository.crud;

import net.blackhacker.ares.model.FeedPageCache;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface FeedPageRepository extends CrudRepository<FeedPageCache, UUID> {
}
