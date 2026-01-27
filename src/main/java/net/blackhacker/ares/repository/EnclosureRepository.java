package net.blackhacker.ares.repository;

import net.blackhacker.ares.model.Enclosure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.net.URL;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnclosureRepository extends JpaRepository<Enclosure, UUID> {
    Optional<Enclosure> findByUrl(URL url);
}
