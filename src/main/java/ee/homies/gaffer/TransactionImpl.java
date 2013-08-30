package ee.homies.gaffer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionImpl implements Transaction {
	private final static Logger log = LoggerFactory.getLogger(TransactionImpl.class);

	private static AtomicLong idSequence = new AtomicLong();

	private int status;
	private String id;
	private final List<XAResource> xaResources = new CopyOnWriteArrayList<>();
	private final List<Synchronization> synchronizations = new CopyOnWriteArrayList<>();
	private final Map<Object, Object> resources = new ConcurrentHashMap<>();

	public void putResource(Object key, Object val) {
		resources.put(key, val);
	}

	public Object getResource(Object key) {
		return resources.get(key);
	}

	public void begin() {
		status = Status.STATUS_ACTIVE;
		id = String.valueOf(idSequence.incrementAndGet());
	}

	@Override
	public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
		int status = getStatus();
		if (status == Status.STATUS_ROLLEDBACK) {
			throw new RollbackException("Can not commit. Transaction is already rolled back.");
		}
		if (status == Status.STATUS_MARKED_ROLLBACK) {
			rollback();
			throw new RollbackException("Transaction was marked as to be rolled back and so it was rolled back.");
		}

		setStatus(Status.STATUS_COMMITTING);

		boolean failed = false;
		for (XAResource xaResource : xaResources) {
			try {
				xaResource.commit(null, true);
			} catch (XAException e) {
				failed = true;
				log.error("Commit failed.", e);
			}
		}
		xaResources.clear();
		resources.clear();

		setStatus(Status.STATUS_COMMITTED);

		if (failed) {
			throw new HeuristicMixedException("Complete commit failed.");
		}
	}

	@Override
	public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException, SystemException {
		return xaResources.remove(xaRes);
	}

	@Override
	public boolean enlistResource(XAResource xaRes) {
		xaResources.add(xaRes);
		return true;
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public void registerSynchronization(Synchronization sync) throws RollbackException, IllegalStateException {
		if (getStatus() == Status.STATUS_MARKED_ROLLBACK) {
			throw new RollbackException("Transaction is marked as rollback-only.");
		}
		synchronizations.add(sync);
	}

	@Override
	public void rollback() throws IllegalStateException, SystemException {
		setStatus(Status.STATUS_ROLLING_BACK);
		for (XAResource xaResource : xaResources) {
			try {
				xaResource.rollback(null);
			} catch (XAException e) {
				log.error("Rollback failed.", e);
			}
		}
		xaResources.clear();
		resources.clear();
		setStatus(Status.STATUS_ROLLEDBACK);
	}

	@Override
	public void setRollbackOnly() {
		setStatus(Status.STATUS_MARKED_ROLLBACK);
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Object getId() {
		return id;
	}
}
