package ee.homies.gaffer;

import javax.transaction.*;

import ee.homies.gaffer.util.FormatLogger;

public class TransactionManagerImpl implements TransactionManager {
  private final static FormatLogger log = new FormatLogger(TransactionManagerImpl.class);

  private final ThreadLocal<TransactionImpl> transactions = new ThreadLocal<>();
  private final ThreadLocal<Integer> transactionTimeoutsSeconds = new ThreadLocal<>();

  private final TransactionManagerStatistics statistics;

  public TransactionManagerImpl(TransactionManagerStatistics statistics) {
    this.statistics = statistics;
  }

  @Override
  public void begin() throws NotSupportedException, SystemException {
    Transaction transaction = getTransaction();
    if (transaction != null) {
      throw new NotSupportedException("Nested transactions are not supported.");
    }

    TransactionImpl transactionImpl = new TransactionImpl();
    transactionImpl.begin(transactionTimeoutsSeconds.get());
    transactions.set(transactionImpl);

    statistics.markBegin();
  }

  @Override
  public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
    TransactionImpl transaction = getTransactionImpl();
    if (transaction == null) {
      throw new IllegalStateException("Can not commit. Current thread is not associated with transaction.");
    }

    try {
      transaction.commit();
    } finally {
      transactions.remove();
    }
  }

  @Override
  public int getStatus() {
    TransactionImpl transaction = getTransactionImpl();
    return transaction == null ? Status.STATUS_NO_TRANSACTION : transaction.getStatus();
  }

  @Override
  public Transaction getTransaction() {
    TransactionImpl transaction = transactions.get();
    return transaction;
  }

  public TransactionImpl getTransactionImpl() {
    return (TransactionImpl) getTransaction();
  }

  @Override
  public void resume(Transaction transaction) throws InvalidTransactionException, IllegalStateException, SystemException {
    Transaction currentTransaction = getTransaction();
    if (currentTransaction != null) {
      throw new IllegalStateException("Can not resume. Current thread is already associated with transaction.");
    }
    if (!(transaction instanceof TransactionImpl)) {
      throw new IllegalStateException("Can not resume. Unsupported transaction object '" + transaction + "' provided.");
    }
    TransactionImpl transactionImpl = (TransactionImpl) transaction;
    if (log.isDebugEnabled()) {
      log.debug("Resuming transaction '%s'.", transactionImpl.getTransactionInfo());
    }
    transactions.set(transactionImpl);
    transactionImpl.setSuspended(false);

    statistics.markResume();
  }

  @Override
  public void rollback() throws IllegalStateException, SecurityException, SystemException {
    TransactionImpl transaction = getTransactionImpl();
    if (transaction == null) {
      throw new IllegalStateException("Can not rollback. Current thread is not associated with transaction.");
    }

    try {
      transaction.rollback();
    } finally {
      transactions.remove();
    }
  }

  @Override
  public void setRollbackOnly() throws IllegalStateException, SystemException {
    Transaction transaction = getTransaction();
    if (transaction == null) {
      throw new IllegalStateException("Can not mark to rollback. Current thread is not associated with transaction.");
    }
    transaction.setRollbackOnly();
  }

  @Override
  public void setTransactionTimeout(int seconds) throws SystemException {
    transactionTimeoutsSeconds.set(seconds);
  }

  @Override
  public Transaction suspend() throws SystemException {
    TransactionImpl transaction = getTransactionImpl();

    if (transaction != null) {
      if (log.isDebugEnabled()) {
        log.debug("Suspending transaction '" + transaction.getTransactionInfo() + "'.");
      }
      transaction.setSuspended(true);
      transactions.remove();
      statistics.markSuspend();
    } else {
      log.debug("Suspend called for non-existent transaction.");
    }
    return transaction;
  }

  public TransactionManagerStatistics getTransactionManagerStatistics() {
    return statistics;
  }
}
