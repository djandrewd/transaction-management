package ua.danit.logging.resources;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.jms.JMSContext;
import javax.jms.Queue;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.danit.logging.messages.MessageRequestsCache;
import ua.danit.logging.model.ResponseMessage;
import ua.danit.logging.model.ServiceMessage;

/**
 * This is REST resource which uses asynchronous logging
 * queues and asynchronous client response.
 *
 * @author Andrey Minov
 */
@RestController
@RequestMapping("/log")
public class LoggingResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingResource.class);
  private static final String MDC_KEY = "message";

  private final Queue logQueue;
  private final ActiveMQConnectionFactory connectionFactory;
  private final MessageRequestsCache messageRequestsCache;
  private final ObjectMapper objectMapper;


  /**
   * Instantiates a new Logging resource.
   *
   * @param logQueue             the log queue holding the logs.
   * @param connectionFactory    the connection factory for connection to JMS broker.
   * @param messageRequestsCache the message requests cache.
   * @param objectMapper         the object mapper for serializing objects.
   */
  @Autowired
  public LoggingResource(@Qualifier("logs_queue") Queue logQueue,
                         ActiveMQConnectionFactory connectionFactory,
                         MessageRequestsCache messageRequestsCache, ObjectMapper objectMapper) {
    this.logQueue = logQueue;
    this.connectionFactory = connectionFactory;
    this.messageRequestsCache = messageRequestsCache;
    this.objectMapper = objectMapper;
  }

  /**
   * Save log message into logging queue and return completable future instance
   * completed when response came after queue processing.
   *
   * @param payload     the message logging payload.
   * @param hostname    the hostname of the logging machine.
   * @param level       the level of logging message.
   * @param application the application who wore the log.
   * @param timestamp   the timestamp of the log entry.
   * @return the completable future completed when response came after queue processing.
   */
  @Async
  @PostMapping(value = "/message", produces = APPLICATION_JSON_VALUE,
      consumes = APPLICATION_FORM_URLENCODED_VALUE)
  public CompletableFuture<ResponseMessage> saveLogMessage(String payload, String hostname,
                                                           int level, String application,
                                                           long timestamp) throws Exception {
    MDC.put(MDC_KEY, UUID.randomUUID().toString());
    try {
      LOGGER.info("Receive message with parameters: '{}','{}','{}' at {}. Level {}", hostname,
          application, payload, timestamp, level);
      ServiceMessage serviceMessage = new ServiceMessage();
      serviceMessage.setId(UUID.randomUUID().toString());
      serviceMessage.setHostname(hostname);
      serviceMessage.setApplication(application);
      serviceMessage.setPayload(payload);
      serviceMessage.setLevel(level);
      serviceMessage.setTimestamp(timestamp);

      CompletableFuture<ResponseMessage> response = new CompletableFuture<>();
      String text = objectMapper.writer().writeValueAsString(serviceMessage);

      LOGGER.info("Create new message log with id {}", serviceMessage.getId());
      messageRequestsCache.store(serviceMessage.getId(), response);

      try (JMSContext context = connectionFactory.createContext()) {
        context.createProducer().send(logQueue, text);
      }
      return response;
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  /**
   * Handle {@link IllegalArgumentException} and {@link IllegalStateException} and send status code
   * 400 as response.
   *
   * @param ex the exception to accept
   * @return the response entity with status code 400.
   */
  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<ResponseMessage> handleException(RuntimeException ex) {
    LOGGER.warn("Unable to handle log messages due to illegal argument: " + ex.getMessage(), ex);
    return ResponseEntity.badRequest().build();
  }

  /**
   * Handle common exception and return message status 500.
   *
   * @param ex the exception to accept
   * @return the response entity with status code 500.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ResponseMessage> handleException(Exception ex) {
    LOGGER.warn("Unable to handle log messages: " + ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }

}
