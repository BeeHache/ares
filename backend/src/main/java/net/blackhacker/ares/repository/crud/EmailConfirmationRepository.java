package net.blackhacker.ares.repository.crud;

import net.blackhacker.ares.model.EmailConfirmationCode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailConfirmationRepository extends CrudRepository<EmailConfirmationCode, String> {
}
