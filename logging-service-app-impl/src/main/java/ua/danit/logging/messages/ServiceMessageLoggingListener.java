package ua.danit.logging.messages;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ua.danit.logging.dao.MessageStoreService;
import ua.danit.logging.model.ResponseMessage;
import ua.danit.logging.model.ServiceMessage;

/**
 * JMS message listener for messages from service.
 * </p>
 * This listener receives message from logging queue, store log record into
 * logging store and push response into responses queue.
 * </p>
 * Message is receiving and saving in single transaction. Exception
 * occurrence during storing of the message will cause rollback of JMS message processing.
 *
 * @author Andrey Minov
 */
@Component
public class ServiceMessageLoggingListener implements MessageListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMessageLoggingListener.class);
  private static final String MDC_KEY = "message";

  private final Queue logsQueue;
  private final Queue responsesQueue;
  private final ActiveMQConnectionFactory connectionFactory;
  private final MessageStoreService messageStoreService;
  private final ObjectMapper objectMapper;

  private JMSContext context;
  private JMSConsumer consumer;

  /**
   * Instantiates a new Service message logging listener.
   *
   * @param logsQueue           the logs queue for getting the logs.
   * @param responsesQueue      the responses queue for sending response of processing
   * @param connectionFactory   the connection factory for connection to JMS broker
   * @param messageStoreService the service message dao for storing message results.
   * @param objectMapper        the object mapper for serialization of entries.
   */
  @Autowired
  public ServiceMessageLoggingListener(@Qualifier("logs_queue") Queue logsQueue,
                                       @Qualifier("responses_queue") Queue responsesQueue,
                                       ActiveMQConnectionFactory connectionFactory,
                                       MessageStoreService messageStoreService,
                                       ObjectMapper objectMapper) {
    this.logsQueue = logsQueue;
    this.responsesQueue = responsesQueue;
    this.connectionFactory = connectionFactory;
    this.messageStoreService = messageStoreService;
    this.objectMapper = objectMapper;
  }

  /**
   * Subscribe to logging queue.
   */
  @PostConstruct
  public void subscribe() throws JMSException {
    context = connectionFactory.createContext(Session.SESSION_TRANSACTED);
    consumer = context.createConsumer(logsQueue);
    consumer.setMessageListener(this);
    context.start();
  }

  @Override
  public void onMessage(Message message) {
    MDC.put(MDC_KEY, UUID.randomUUID().toString());
    try {
      TextMessage textMessage = (TextMessage) message;
      String text = textMessage.getText();
      LOGGER.debug("Received logging message {}", text);

      ServiceMessage serviceMessage =
          objectMapper.reader().forType(ServiceMessage.class).readValue(text);
      LOGGER.debug("Store message into data store.");
      messageStoreService.store(serviceMessage);

      LOGGER.debug("Send message into responses queue.");
      ResponseMessage responseMessage = new ResponseMessage();
      responseMessage.setId(serviceMessage.getId());
      responseMessage.setErrorCode(ErrorCodes.OK.getStatus());

      String responseText = objectMapper.writer().writeValueAsString(responseMessage);
      context.createProducer().send(responsesQueue, responseText);
      context.commit();
      LOGGER.debug("Finish handling logging message.", text);
    } catch (JMSException e) {
      LOGGER.warn(String.format("Exception occurred during message processing %s", e.getMessage()),
          e);
      context.rollback();
    } catch (Exception e) {
      context.rollback();
      LOGGER.warn(String.format("Exception occurred during message processing %s", e.getMessage()),
          e);
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  /**
   * Close.
   */
  @PreDestroy
  public void close() {
    if (consumer != null) {
      try {
        consumer.close();
      } catch (Exception e) {
        LOGGER.warn(String.format("Unable to close JMS consumer due to %s", e.getMessage()), e);
      }
    }
    if (context != null) {
      try {
        context.close();
      } catch (Exception e) {
        LOGGER.warn(String.format("Unable to close JMS context due to %s", e.getMessage()), e);
      }
    }
  }


}
