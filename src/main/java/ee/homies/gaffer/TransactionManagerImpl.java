package ee.homies.gaffer;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionManagerImpl implements TransactionManager {
	private final static Logger log = LoggerFactory.getLogger(TransactionManagerImpl.class);
	private final ThreadLocal<TransactionImpl> transactions = new ThreadLocal<TransactionImpl>();

	@Override
	public void begin() throws NotSupportedException, SystemException {
		Transaction transaction = getTransaction();
		if (transaction != null) {
			throw new NotSupportedException("Nested transactions are not supported.");
		}

		transactions.set(new TransactionImpl());
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
			transactions.set((TransactionImpl) transaction);
		}
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
		// TODO need to implement.
	}

	@Override
	public Transaction suspend() throws SystemException {
		log.debug("Suspending transaction.");
		Transaction transaction = getTransaction();
		if (transaction != null) {
			transactions.remove();
		}
		return transaction;
	}
}
