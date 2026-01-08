package net.blackhacker.ares.repository;

import net.blackhacker.ares.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepositry extends JpaRepository<Image, Long> {
}
