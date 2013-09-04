package ee.homies.gaffer.util;

import javax.transaction.RollbackException;

public class RollbackExceptionImpl extends RollbackException {
  private static final long serialVersionUID = 1L;

  public RollbackExceptionImpl(String message) {
    super(message);
  }

  public RollbackExceptionImpl(String message, Exception cause) {
    super(message);
    initCause(cause);
  }
}
