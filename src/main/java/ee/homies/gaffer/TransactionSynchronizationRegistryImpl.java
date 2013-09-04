package ee.homies.gaffer;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;

public class TransactionSynchronizationRegistryImpl implements TransactionSynchronizationRegistry {
  @Override
  public Object getTransactionKey() {
    TransactionImpl transaction = ServiceRegistry.getInstance().getTransactionManager().getTransactionImpl();
    return transaction == null ? null : transaction.getGlobalTransactionId();
  }

  @Override
  public void putResource(Object key, Object value) {
    TransactionImpl transaction = ServiceRegistry.getInstance().getTransactionManager().getTransactionImpl();
    if (transaction == null) {
      throw new IllegalStateException("Current thread is not associated with transaction.");
    }
    transaction.putResource(key, value);
  }

  @Override
  public Object getResource(Object key) {
    TransactionImpl transaction = ServiceRegistry.getInstance().getTransactionManager().getTransactionImpl();
    if (transaction == null) {
      throw new IllegalStateException("Current thread is not associated with transaction.");
    }
    return transaction.getResource(key);
  }

  @Override
  public void registerInterposedSynchronization(Synchronization sync) {
    TransactionImpl transaction = ServiceRegistry.getInstance().getTransactionManager().getTransactionImpl();
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
    return ServiceRegistry.getInstance().getTransactionManager().getStatus();
  }

  @Override
  public void setRollbackOnly() {
    TransactionImpl transaction = ServiceRegistry.getInstance().getTransactionManager().getTransactionImpl();
    if (transaction == null) {
      throw new IllegalStateException("Current thread is not associated with transaction.");
    }
    transaction.setRollbackOnly();
  }

  @Override
  public boolean getRollbackOnly() {
    TransactionImpl transaction = ServiceRegistry.getInstance().getTransactionManager().getTransactionImpl();
    if (transaction == null) {
      throw new IllegalStateException("Current thread is not associated with transaction.");
    }
    return transaction.getStatus() == Status.STATUS_MARKED_ROLLBACK;
  }

}
