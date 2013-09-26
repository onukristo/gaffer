package ee.homies.gaffer;

import javax.management.MXBean;

@MXBean
public interface TransactionManagerStatisticsMXBean {
  public long getStartedTransactionsCount();

  public long getCommittedTransactionsCount();

  public long getRolledBackTransactionsCount();

  public long getActiveTransactionsCount();

  public long getSuspendedTransactionsCount();

  public long getAbandonedTransactionsCount();

  public long getFailedRollbacksCount();

  public long getHeuristicCommitsCount();
}
