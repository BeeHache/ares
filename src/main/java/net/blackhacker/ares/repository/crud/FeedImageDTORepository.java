package net.blackhacker.ares.repository.crud;

import net.blackhacker.ares.dto.FeedImageDTO;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FeedImageDTORepository extends CrudRepository<FeedImageDTO, UUID> {
}
