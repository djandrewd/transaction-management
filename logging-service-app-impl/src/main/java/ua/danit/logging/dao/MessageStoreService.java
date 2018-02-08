package ua.danit.logging.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.danit.logging.model.ServiceMessage;

/**
 * Storage service for integration with data stores for storing service logs.
 *
 * @author Andrey Minov
 */
@Service
public class MessageStoreService {

  private final ServiceMessageDao messageDao;

  /**
   * Instantiates a new Message store service.
   *
   * @param messageDao the message data access object for accessing data store.
   */
  @Autowired
  public MessageStoreService(ServiceMessageDao messageDao) {
    this.messageDao = messageDao;
  }

  /**
   * Store new log service record into data storage.
   *
   * @param message the message to store.
   */
  public void store(ServiceMessage message) {
    messageDao.save(message);
  }

}
