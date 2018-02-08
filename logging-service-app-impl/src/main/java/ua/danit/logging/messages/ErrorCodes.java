package ua.danit.logging.messages;

/**
 * Application error codes enumeration.
 *
 * @author Andrey Minov
 */
public enum ErrorCodes {
  OK(0), STORE_ERROR(1), BROKER_ERROR(2), GENERAL_ERROR(3);
  int status;

  ErrorCodes(int status) {
    this.status = status;
  }

  public int getStatus() {
    return status;
  }
}
