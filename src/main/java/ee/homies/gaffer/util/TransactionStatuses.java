package ee.homies.gaffer.util;

import javax.transaction.Status;

public class TransactionStatuses {
  public static String toString(int status) {
    switch (status) {
    case Status.STATUS_ACTIVE:
      return "ACTIVE";
    case Status.STATUS_COMMITTED:
      return "COMMITED";
    case Status.STATUS_COMMITTING:
      return "COMMITTING";
    case Status.STATUS_MARKED_ROLLBACK:
      return "MARKED_ROLLBACK";
    case Status.STATUS_NO_TRANSACTION:
      return "NO_TRANSACTION";
    case Status.STATUS_PREPARED:
      return "PREPARED";
    case Status.STATUS_PREPARING:
      return "PREPARING";
    case Status.STATUS_ROLLEDBACK:
      return "ROLLEDBACK";
    case Status.STATUS_ROLLING_BACK:
      return "ROLLING_BACK";
    }
    return "UNKNOWN";
  }
}
