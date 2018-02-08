package ua.danit.logging.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Spring REST application configuration module.
 *
 * @author Andrey Minov
 */
@Configuration
@EnableWebMvc
@ComponentScan("ua.danit.logging.resources")
public class RestConfiguration extends WebMvcConfigurerAdapter {
  private final ObjectMapper objectMapper;

  @Autowired
  public RestConfiguration(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
  }
}
