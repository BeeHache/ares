package net.blackhacker.ares.repository;

import net.blackhacker.ares.model.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
