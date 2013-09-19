package ee.homies.gaffer;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;

public class TransactionSynchronizationRegistryImpl implements TransactionSynchronizationRegistry {
  private final TransactionManagerImpl transactionManager;

  public TransactionSynchronizationRegistryImpl(TransactionManagerImpl transactionManager) {
    this.transactionManager = transactionManager;
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
      throw new IllegalStateException("Current thread is not associated with transaction.");
    }
    transaction.putResource(key, value);
  }

  @Override
  public Object getResource(Object key) {
    TransactionImpl transaction = getTransaction();
    if (transaction == null) {
      throw new IllegalStateException("Current thread is not associated with transaction.");
    }
    return transaction.getResource(key);
  }

  @Override
  public void registerInterposedSynchronization(Synchronization sync) {
    TransactionImpl transaction = getTransaction();
    if (transaction == null) {
      throw new IllegalStateException("Current thread is not associated with transaction.");
    }
    try {
      transaction.registerSynchronization(sync);
    } catch (RollbackException e) {
      throw new RuntimeException(e);
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
      throw new IllegalStateException("Current thread is not associated with transaction.");
    }
    transaction.setRollbackOnly();
  }

  @Override
  public boolean getRollbackOnly() {
    TransactionImpl transaction = getTransaction();
    if (transaction == null) {
      throw new IllegalStateException("Current thread is not associated with transaction.");
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
