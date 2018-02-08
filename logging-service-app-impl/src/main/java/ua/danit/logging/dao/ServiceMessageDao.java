package ua.danit.logging.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.danit.logging.model.ServiceMessage;

/**
 * Data access object for data store for {@link ServiceMessage}
 * log messages.
 *
 * @author Andrey Minov
 */
public interface ServiceMessageDao extends JpaRepository<ServiceMessage, Long> {
}
