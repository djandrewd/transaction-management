package ua.danit.logging.configuration;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_WITH_ZONE_ID;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Set;
import javax.jms.Queue;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Spring configuration for JMS service. This is no spring-jms configuration, but
 * configuration for list JMS 2.0 implementation.
 *
 * @author Andrey Minov
 */
@Configuration
@ComponentScan("ua.danit.logging.messages")
public class JmsConfiguration {

  @Value("${log_queue.name:logs_queue}")
  private String logsQueueName;
  @Value("${responses_queue.name:responses_queue}")
  private String responsesQueueName;
  @Value("${connection_url:vm://0}")
  private String connectionUrl;

  /**
   * Create and return new embedded JMS server.
   *
   * @return the embedded JMS server for managing queues and topics.
   */
  @Bean(destroyMethod = "stop", initMethod = "start")
  public EmbeddedJMS activeMqServer() {
    ConfigurationImpl configuration = new ConfigurationImpl();
    Set<TransportConfiguration> transports = new HashSet<>();
    transports.add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));

    configuration.setAcceptorConfigurations(transports);
    configuration.setSecurityEnabled(false);

    JMSConfigurationImpl jmsConfiguration = new JMSConfigurationImpl();
    EmbeddedJMS server = new EmbeddedJMS();
    server.setConfiguration(configuration);
    server.setJmsConfiguration(jmsConfiguration);
    return server;
  }

  /**
   * Create and return new JMS connection factory used to provide connections for JMS brokers.
   *
   * @param jms the jms
   * @return new JMS connection factory used to provide connections for JMS brokers.
   */
  @Bean
  public ActiveMQConnectionFactory connectionFactory(EmbeddedJMS jms) {
    return new ActiveMQConnectionFactory(connectionUrl);
  }

  /**
   * Create queue for service logs.
   *
   * @return the queue holding message logs.
   */
  @Qualifier("logs_queue")
  @Bean
  public Queue logsQueue() {
    return new ActiveMQQueue(logsQueueName);
  }

  /**
   * Create queue for message process responses.
   *
   * @return the queue for message process responses.
   */
  @Qualifier("responses_queue")
  @Bean
  public Queue responsesQueue() {
    return new ActiveMQQueue(responsesQueueName);
  }

  /**
   * Jackson ObjectMapper for JSON serialization and deserialization.
   *
   * @return the object mapper for JSON serialization.
   */
  @Bean
  public ObjectMapper objectMapper() {
    return new Jackson2ObjectMapperBuilder().indentOutput(true)
                                            .featuresToDisable(WRITE_DATES_AS_TIMESTAMPS)
                                            .featuresToEnable(WRITE_DATES_WITH_ZONE_ID).build();
  }
}
