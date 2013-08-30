package ee.homies.gaffer;

import java.io.Serializable;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

public class UserTransactionImpl implements UserTransaction, Serializable {
	private static final long serialVersionUID = 1L;

	@Override
	public void begin() throws NotSupportedException, SystemException {
		TransactionManagerImpl transactionManager = ServiceRegistry.getInstance().getTransactionManager();
		transactionManager.begin();
	}

	@Override
	public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
		TransactionManagerImpl transactionManager = ServiceRegistry.getInstance().getTransactionManager();
		transactionManager.commit();
	}

	@Override
	public void rollback() throws IllegalStateException, SecurityException, SystemException {
		TransactionManagerImpl transactionManager = ServiceRegistry.getInstance().getTransactionManager();
		transactionManager.rollback();
	}

	@Override
	public void setRollbackOnly() throws IllegalStateException, SystemException {
		TransactionManagerImpl transactionManager = ServiceRegistry.getInstance().getTransactionManager();
		transactionManager.setRollbackOnly();
	}

	@Override
	public int getStatus() throws SystemException {
		TransactionManagerImpl transactionManager = ServiceRegistry.getInstance().getTransactionManager();
		return transactionManager.getStatus();
	}

	@Override
	public void setTransactionTimeout(int seconds) throws SystemException {
		TransactionManagerImpl transactionManager = ServiceRegistry.getInstance().getTransactionManager();
		transactionManager.setTransactionTimeout(seconds);
	}

}
