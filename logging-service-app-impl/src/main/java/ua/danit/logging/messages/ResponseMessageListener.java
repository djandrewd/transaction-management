package ua.danit.logging.messages;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
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
import ua.danit.logging.model.ResponseMessage;

/**
 * Listener for JMS message responses queue.
 * <p/>
 * Listener will get the message and notify waiting process of
 * processing response.
 *
 * @author Andrey Minov
 */
@Component
public class ResponseMessageListener implements MessageListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMessageLoggingListener.class);
  private static final String MDC_KEY = "message";

  private final Queue responsesQueue;
  private final ActiveMQConnectionFactory connectionFactory;
  private final ObjectMapper objectMapper;
  private final MessageRequestsCache messageRequestsCache;

  private JMSContext context;
  private JMSConsumer consumer;

  /**
   * Instantiates a new Response message listener.
   *
   * @param responsesQueue       the responses queue for handling responses.
   * @param connectionFactory    the connection factory for connection to JMS broker.
   * @param objectMapper         the object mapper for serialization of the requests.
   * @param messageRequestsCache the message requests cache for sending client responses.
   */
  @Autowired
  public ResponseMessageListener(@Qualifier("responses_queue") Queue responsesQueue,
                                 ActiveMQConnectionFactory connectionFactory,
                                 ObjectMapper objectMapper,
                                 MessageRequestsCache messageRequestsCache) {
    this.responsesQueue = responsesQueue;
    this.connectionFactory = connectionFactory;
    this.objectMapper = objectMapper;
    this.messageRequestsCache = messageRequestsCache;
  }

  /**
   * Subscribe to response queue.
   *
   */
  @PostConstruct
  public void subscribe() {
    context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE);
    consumer = context.createConsumer(responsesQueue);
    consumer.setMessageListener(this);
    context.start();
  }

  @Override
  public void onMessage(Message message) {
    MDC.put(MDC_KEY, UUID.randomUUID().toString());
    try {
      TextMessage textMessage = (TextMessage) message;
      String text = textMessage.getText();
      LOGGER.debug("Received response message {}", text);
      ResponseMessage serviceMessage =
          objectMapper.reader().forType(ResponseMessage.class).readValue(text);
      messageRequestsCache.get(serviceMessage.getId()).ifPresent(v -> v.complete(serviceMessage));
      LOGGER.debug("Finish handling response message.", text);
    } catch (Exception e) {
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
