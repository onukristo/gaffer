package ee.homies.gaffer.util;

import javax.transaction.RollbackException;

public class HeuristicMixedExceptionImpl extends RollbackException {
  private static final long serialVersionUID = 1L;

  public HeuristicMixedExceptionImpl(String message) {
    super(message);
  }

  public HeuristicMixedExceptionImpl(String message, Exception cause) {
    super(message);
    initCause(cause);
  }
}
