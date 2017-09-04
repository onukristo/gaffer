package ee.homies.gaffer.jms;

import ee.homies.gaffer.OrderedResource;
import ee.homies.gaffer.ServiceRegistryHolder;
import ee.homies.gaffer.util.DummyXAResource;
import ee.homies.gaffer.util.FormatLogger;
import ee.homies.gaffer.util.MBeanUtil;
import ee.homies.gaffer.util.XAExceptionImpl;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.transaction.Status;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ConnectionFactoryImpl implements ConnectionFactory, ConnectionFactoryMXBean {
	private final static FormatLogger log = new FormatLogger(ConnectionFactoryImpl.class);

	private ConnectionFactory connectionFactory;
	private static AtomicLong idSequence = new AtomicLong();
	private String id;
	private String sessionResourceKey;
	private String uniqueName;
	private boolean registerAsMBean = true;
	private int order = 0;

	private final AtomicLong allSessionGetsCount = new AtomicLong();
	private final AtomicLong bufferedSessionGetsCount = new AtomicLong();
	private final AtomicLong nonTransactionalSessionGetsCount = new AtomicLong();

	@PostConstruct
	public void init() {
		if (StringUtils.isEmpty(uniqueName)) {
			throw new IllegalStateException("Unique name is not set.");
		}
		id = ConnectionFactoryImpl.class.getName() + "." + String.valueOf(idSequence.incrementAndGet());
		sessionResourceKey = id + ".ses";

		if (registerAsMBean) {
			MBeanUtil.registerMBeanQuietly(this, "ee.homies.gaffer:type=JmsConnectionFactory,name=" + uniqueName);
		}
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public void setRegisterAsMBean(boolean registerAsMBean) {
		this.registerAsMBean = registerAsMBean;
	}

	@Override
	public Connection createConnection() throws JMSException {
		return new ConnectionImpl(connectionFactory.createConnection(), uniqueName);
	}

	@Override
	public Connection createConnection(String userName, String password) throws JMSException {
		return new ConnectionImpl(connectionFactory.createConnection(userName, password), uniqueName);
	}

	private class ConnectionImpl extends ConnectionWrapper {
		private AtomicInteger transactionalActiveSessionsCount = new AtomicInteger();
		private String resourceUniqueName;

		public ConnectionImpl(Connection connection, String resourceUniqueName) {
			super(connection);
			this.resourceUniqueName = resourceUniqueName;
		}

		@Override
		public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
			TransactionSynchronizationRegistry registry = ServiceRegistryHolder.getServiceRegistry().getTransactionSynchronizationRegistry();
			allSessionGetsCount.incrementAndGet();
			if (registry.getTransactionStatus() == Status.STATUS_NO_TRANSACTION) {
				return createNonTransactionalSession(transacted, acknowledgeMode);
			}
			return createTransactionalSession(registry);
		}

		@Override
		public void close() throws JMSException {
			if (transactionalActiveSessionsCount.get() == 0) {
				log.debug("Closing connection for resource '{}'.", resourceUniqueName);
				getConnection().close();
			}
		}

		private void reduceTransactionalActiveSessionsCount() {
			transactionalActiveSessionsCount.decrementAndGet();
		}

		private Session createTransactionalSession(TransactionSynchronizationRegistry registry) throws JMSException {
			SessionImpl session = (SessionImpl) registry.getResource(sessionResourceKey);
			if (session != null) {
				bufferedSessionGetsCount.incrementAndGet();
				return session;
			}
			Session jmsSession = getConnection().createSession(true, Session.SESSION_TRANSACTED);
			transactionalActiveSessionsCount.incrementAndGet();
			session = new SessionImpl(jmsSession, null);
			XAResourceImpl xaResource = new XAResourceImpl(session, order);
			registry.putResource(sessionResourceKey, session);

			ServiceRegistryHolder.getServiceRegistry().getTransactionManager().getTransactionImpl().enlistResource(xaResource);
			return session;
		}

		private Session createNonTransactionalSession(boolean transacted, int acknowledgeMode) throws JMSException {
			Session session = getConnection().createSession(transacted, acknowledgeMode);
			nonTransactionalSessionGetsCount.incrementAndGet();
			return session;
		}
	}

	private static class SessionImpl extends XASessionWrapper {
		public SessionImpl(Session session, XAResource xaResource) {
			super(session, xaResource);
		}

		@Override
		public void commit() throws JMSException {
			super.commit();
		}

		@Override
		public void close() throws JMSException {
		}

		public void closeSession() throws JMSException {
			getSession().close();
		}
	}

	public static class XAResourceImpl extends DummyXAResource implements OrderedResource {
		private final SessionImpl session;
		private int order;

		public XAResourceImpl(SessionImpl session, int order) {
			this.session = session;
			this.order = order;
		}

		@Override
		public void commit(Xid xid, boolean onePhase) throws XAException {
			try {
				try {
					session.commit();
				} catch (JMSException e) {
					throw new XAExceptionImpl(XAException.XAER_RMERR, e);
				}
			} finally {
				try {
					session.closeSession();
				} catch (JMSException e) {
					log.error(e.getMessage(), e);
				}
			}
		}

		@Override
		public void rollback(Xid xid) throws XAException {
			try {
				try {
					session.rollback();
				} catch (JMSException e) {
					log.error(e.getMessage(), e);
					throw new XAExceptionImpl(XAException.XAER_RMERR, e);
				}
			} finally {
				try {
					session.closeSession();
				} catch (JMSException e) {
					log.error(e.getMessage(), e);
				}
			}
		}

		@Override
		public int getOrder() {
			return order;
		}
	}

	@Override
	public long getAllSessionGetsCount() {
		return allSessionGetsCount.get();
	}

	@Override
	public long getBufferedSessionGetsCount() {
		return bufferedSessionGetsCount.get();
	}

	@Override
	public long getNonTransactionalSessionGetsCount() {
		return nonTransactionalSessionGetsCount.get();
	}
}
