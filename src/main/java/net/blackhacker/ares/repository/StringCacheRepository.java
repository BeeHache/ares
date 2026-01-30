package net.blackhacker.ares.repository;

import net.blackhacker.ares.dto.StringCacheDTO;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StringCacheRepository extends CrudRepository<StringCacheDTO, UUID> {
}
