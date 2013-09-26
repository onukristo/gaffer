package ee.homies.gaffer.util;

public class ExceptionThrower {
  private static final FormatLogger log = new FormatLogger(ExceptionThrower.class);

  private final boolean logExceptions;

  public ExceptionThrower(boolean logExceptions) {
    this.logExceptions = logExceptions;
  }

  public <T extends Exception> void throwException(T e) throws T {
    if (logExceptions) {
      log.error(e.getMessage(), e);
    }
    throw e;
  }
}
