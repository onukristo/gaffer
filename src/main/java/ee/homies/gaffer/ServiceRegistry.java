package ee.homies.gaffer;

import javax.transaction.TransactionSynchronizationRegistry;

public class ServiceRegistry {
  private static volatile ServiceRegistry serviceRegistry;
  private final TransactionManagerImpl transactionManager;
  private final TransactionSynchronizationRegistry transactionSynchronizationRegistry;
  private final UserTransactionImpl userTransaction;

  public static ServiceRegistry getInstance() {
    if (serviceRegistry == null) {
      synchronized (ServiceRegistry.class) {
        if (serviceRegistry == null) {
          serviceRegistry = new ServiceRegistry();
        }
      }
    }
    return serviceRegistry;
  }

  public ServiceRegistry() {
    transactionManager = new TransactionManagerImpl();
    transactionSynchronizationRegistry = new TransactionSynchronizationRegistryImpl();
    userTransaction = new UserTransactionImpl();
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
}
