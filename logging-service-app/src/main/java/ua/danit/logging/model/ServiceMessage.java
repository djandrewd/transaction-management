package ua.danit.logging.model;

import com.google.common.base.MoreObjects;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This message holding some logging information about
 * application, platform, hostname, log level and text payload.
 *
 * @author Andrey Minov
 */
@Entity
@Table(name = "SERVICE_MESSAGE")
public class ServiceMessage {
  @Id
  private String id;
  private Long timestamp;
  private String application;
  private String hostname;
  private Integer level;
  private String payload;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public Integer getLevel() {
    return level;
  }

  public void setLevel(Integer level) {
    this.level = level;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", id).add("timestamp", timestamp)
                      .add("application", application).add("hostname", hostname).add("level", level)
                      .add("payload", payload).toString();
  }
}
