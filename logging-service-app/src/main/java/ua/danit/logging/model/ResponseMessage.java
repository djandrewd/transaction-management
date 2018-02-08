package ua.danit.logging.model;

import com.google.common.base.MoreObjects;

/**
 * Logging response message containing log message id.
 *
 * @author Andrey Minov.
 */
public class ResponseMessage {
  private String id;
  private Integer errorCode;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Integer getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(Integer errorCode) {
    this.errorCode = errorCode;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", id).add("errorCode", errorCode).toString();
  }
}
