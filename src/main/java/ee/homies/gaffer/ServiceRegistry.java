package ee.homies.gaffer;

import javax.transaction.TransactionSynchronizationRegistry;

import ee.homies.gaffer.util.Clock;
import ee.homies.gaffer.util.MonotonicClock;

public class ServiceRegistry {
  private static volatile ServiceRegistry instance;
  private final TransactionManagerImpl transactionManager;
  private final TransactionSynchronizationRegistry transactionSynchronizationRegistry;
  private final UserTransactionImpl userTransaction;
  private final Configuration configuration;
  private final Clock clock;

  public static ServiceRegistry getInstance() {
    if (instance == null) {
      throw new IllegalStateException("ServiceRegistry is not initialized.");
    }
    return instance;
  }

  public static ServiceRegistry createInstance() {
    return createInstance(new Configuration());
  }

  public static ServiceRegistry createInstance(Configuration configuration) {
    synchronized (ServiceRegistry.class) {
      if (instance != null) {
        throw new IllegalStateException("Service registry already created.");
      }
    }
    ServiceRegistry registry = new ServiceRegistry(configuration);
    instance = registry;

    return instance;
  }

  public static void destroyInstance() {
    getInstance().destroy();
    instance = null;
  }

  private ServiceRegistry(Configuration configuration) {
    this.configuration = configuration;
    transactionManager = new TransactionManagerImpl();
    transactionSynchronizationRegistry = new TransactionSynchronizationRegistryImpl();
    userTransaction = new UserTransactionImpl();
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
