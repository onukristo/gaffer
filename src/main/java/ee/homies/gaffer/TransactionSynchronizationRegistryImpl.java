package ee.homies.gaffer;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;

import ee.homies.gaffer.util.ExceptionThrower;

public class TransactionSynchronizationRegistryImpl implements TransactionSynchronizationRegistry {
  private final TransactionManagerImpl transactionManager;

  private final ExceptionThrower exceptionThrower;

  public TransactionSynchronizationRegistryImpl(TransactionManagerImpl transactionManager, Configuration configuration) {
    this.transactionManager = transactionManager;
    exceptionThrower = new ExceptionThrower(configuration.isLogExceptions());
  }

  @Override
  public Object getTransactionKey() {
    TransactionImpl transaction = getTransaction();
    return transaction == null ? null : transaction.getGlobalTransactionId();
  }

  @Override
  public void putResource(Object key, Object value) {
    TransactionImpl transaction = getTransaction();
    if (transaction == null) {
      exceptionThrower.throwException(new IllegalStateException("Current thread is not associated with transaction."));
    } else {
      transaction.putResource(key, value);
    }
  }

  @Override
  public Object getResource(Object key) {
    TransactionImpl transaction = getTransaction();
    if (transaction == null) {
      exceptionThrower.throwException(new IllegalStateException("Current thread is not associated with transaction."));
      return null;
    }
    return transaction.getResource(key);
  }

  @Override
  public void registerInterposedSynchronization(Synchronization sync) {
    TransactionImpl transaction = getTransaction();
    if (transaction == null) {
      exceptionThrower.throwException(new IllegalStateException("Current thread is not associated with transaction."));
    } else {
      try {
        transaction.registerSynchronization(sync);
      } catch (RollbackException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public int getTransactionStatus() {
    return getTransactionManager().getStatus();
  }

  @Override
  public void setRollbackOnly() {
    TransactionImpl transaction = getTransaction();
    if (transaction == null) {
      exceptionThrower.throwException(new IllegalStateException("Current thread is not associated with transaction."));
    } else {
      transaction.setRollbackOnly();
    }
  }

  @Override
  public boolean getRollbackOnly() {
    TransactionImpl transaction = getTransaction();
    if (transaction == null) {
      exceptionThrower.throwException(new IllegalStateException("Current thread is not associated with transaction."));
      return false;
    }
    return transaction.getStatus() == Status.STATUS_MARKED_ROLLBACK;
  }

  private TransactionImpl getTransaction() {
    return getTransactionManager().getTransactionImpl();
  }

  private TransactionManagerImpl getTransactionManager() {
    return transactionManager;
  }
}
