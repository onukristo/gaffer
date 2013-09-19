package ee.homies.gaffer.jms;

import java.util.concurrent.atomic.AtomicLong;

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

import ee.homies.gaffer.ServiceRegistryHolder;
import ee.homies.gaffer.util.DummyXAResource;
import ee.homies.gaffer.util.FormatLogger;

public class ConnectionFactoryImpl implements ConnectionFactory {
  private final static FormatLogger log = new FormatLogger(ConnectionFactoryImpl.class);

  private ConnectionFactory connectionFactory;
  private static AtomicLong idSequence = new AtomicLong();
  private String id;
  private String sessionResourceKey;

  @PostConstruct
  public void init() {
    id = ConnectionFactoryImpl.class.getName() + "." + String.valueOf(idSequence.incrementAndGet());
    sessionResourceKey = id + ".ses";
  }

  public ConnectionFactory getConnectionFactory() {
    return connectionFactory;
  }

  public void setConnectionFactory(ConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  @Override
  public Connection createConnection() throws JMSException {
    return new ConnectionImpl(connectionFactory.createConnection());
  }

  @Override
  public Connection createConnection(String userName, String password) throws JMSException {
    return new ConnectionImpl(connectionFactory.createConnection(userName, password));
  }

  private class ConnectionImpl extends ConnectionWrapper {
    public ConnectionImpl(Connection connection) {
      super(connection);
    }

    @Override
    public void close() throws JMSException {
    }

    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
      TransactionSynchronizationRegistry registry = ServiceRegistryHolder.getServiceRegistry().getTransactionSynchronizationRegistry();
      if (registry.getTransactionStatus() == Status.STATUS_NO_TRANSACTION) {
        Session session = getConnection().createSession(transacted, acknowledgeMode);
        return session;
      }

      SessionImpl session = (SessionImpl) registry.getResource(sessionResourceKey);
      if (session != null) {
        return session;
      }
      Session jmsSession = getConnection().createSession(true, Session.SESSION_TRANSACTED);
      session = new SessionImpl(jmsSession, null);
      XAResourceImpl xaResource = new XAResourceImpl(session);
      registry.putResource(sessionResourceKey, session);

      ServiceRegistryHolder.getServiceRegistry().getTransactionManager().getTransactionImpl().enlistResource(xaResource);
      return session;
    }
  }

  private class SessionImpl extends XASessionWrapper {
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

  public static class XAResourceImpl extends DummyXAResource {
    private final SessionImpl session;

    public XAResourceImpl(SessionImpl session) {
      this.session = session;
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
      try {
        try {
          session.commit();
        } catch (JMSException e) {
          log.error(e.getMessage(), e);
          throw new XAException(XAException.XAER_RMERR);
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
          throw new XAException(XAException.XAER_RMERR);
        }
      } finally {
        try {
          session.closeSession();
        } catch (JMSException e) {
          log.error(e.getMessage(), e);
        }
      }
    }
  }
}
