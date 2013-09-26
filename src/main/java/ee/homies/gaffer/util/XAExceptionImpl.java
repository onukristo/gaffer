package ee.homies.gaffer.util;

import javax.transaction.xa.XAException;

public class XAExceptionImpl extends XAException {
  private static final long serialVersionUID = 1L;

  public XAExceptionImpl(int errorCode, Throwable cause) {
    super(errorCode);
    initCause(cause);
  }
}
