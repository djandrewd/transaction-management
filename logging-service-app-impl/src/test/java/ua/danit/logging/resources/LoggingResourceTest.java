package ua.danit.logging.resources;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ua.danit.logging.configuration.JmsConfiguration;
import ua.danit.logging.configuration.RestConfiguration;
import ua.danit.logging.dao.MessageStoreService;
import ua.danit.logging.model.ServiceMessage;

/**
 * Logging resource correctness test.
 *
 * @author Andrey Minov
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(
    classes = {JmsConfiguration.class, LoggingResourceTest.DataTestConfiguration.class,
        RestConfiguration.class})
@WebAppConfiguration
//@Ignore
public class LoggingResourceTest {

  @Autowired
  private WebApplicationContext applicationContext;

  @Autowired
  private MessageStoreService messageStoreService;

  private MockMvc mockMvc;
  private ServiceMessage logRecord;

  @Before
  public void setUp() throws Exception {
    logRecord = new ServiceMessage();
    logRecord.setApplication("application-test");
    logRecord.setPayload("Message");
    logRecord.setHostname("Hostname");
    logRecord.setTimestamp(System.currentTimeMillis());
    logRecord.setLevel(1);
    mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
  }

  @Test
  public void testCorrectMessageStore() throws Exception {
    AtomicReference<String> ref = new AtomicReference<>();
    doAnswer(v -> {
      ServiceMessage message = v.getArgument(0);
      ref.set(message.getId());
      return null;
    }).when(messageStoreService).store(any(ServiceMessage.class));

    MvcResult mvcResult = mockMvc.perform(
        post("/log/message").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("payload", logRecord.getPayload())
                            .param("hostname", logRecord.getHostname())
                            .param("application", logRecord.getApplication())
                            .param("level", String.valueOf(logRecord.getLevel()))
                            .param("timestamp", String.valueOf(logRecord.getTimestamp())))
                                 .andReturn();

    mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk())
           .andExpect(content().json(String.format("{\"id\":\"%s\", \"errorCode\":0}", ref.get())));
  }

  @Test
  public void testMissingParameters() throws Exception {
    mockMvc.perform(post("/log/message").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                        .param("payload", logRecord.getPayload()))
           .andExpect(status().isBadRequest());
  }

  @Configuration
  public static class DataTestConfiguration {
    @Bean
    public MessageStoreService messageStoreService() {
      return mock(MessageStoreService.class);
    }
  }
}