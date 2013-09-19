package ee.homies.gaffer;

import javax.transaction.TransactionSynchronizationRegistry;

import ee.homies.gaffer.util.Clock;
import ee.homies.gaffer.util.MonotonicClock;

public class ServiceRegistry {
  private final TransactionManagerImpl transactionManager;
  private final TransactionSynchronizationRegistry transactionSynchronizationRegistry;
  private final UserTransactionImpl userTransaction;
  private final Configuration configuration;
  private final Clock clock;

  protected ServiceRegistry(Configuration configuration) {
    this.configuration = configuration;
    transactionManager = new TransactionManagerImpl();
    transactionSynchronizationRegistry = new TransactionSynchronizationRegistryImpl(transactionManager);
    userTransaction = new UserTransactionImpl(transactionManager);
    clock = new MonotonicClock();
  }

  public void destroy() {
  }

  public UserTransactionImpl getUserTransaction() {
    return userTransaction;
  }

  public TransactionManagerImpl getTransactionManager() {
    return transactionManager;
  }

  public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry() {
    return transactionSynchronizationRegistry;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public Clock getClock() {
    return clock;
  }

}
