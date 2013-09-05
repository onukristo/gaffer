package ee.homies.gaffer.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.transaction.Status;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import ee.homies.gaffer.ServiceRegistry;
import ee.homies.gaffer.util.DummyXAResource;
import ee.homies.gaffer.util.FormatLogger;

public class DataSourceImpl extends DataSourceWrapper {
  private static final FormatLogger log = new FormatLogger(DataSourceImpl.class);

  private static AtomicLong idSequence = new AtomicLong();
  private String id;
  private String connectionResourceKey;
  private String uniqueName;

  public DataSourceImpl() {
  }

  @PostConstruct
  public void init() {
    id = DataSourceImpl.class + "." + String.valueOf(idSequence.incrementAndGet());
    connectionResourceKey = id + ".con";
  }

  public String getUniqueName() {
    return uniqueName;
  }

  public void setUniqueName(String uniqueName) {
    this.uniqueName = uniqueName;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return getTransactionalConnection(null, null);
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return getTransactionalConnection(username, password);
  }

  private Connection getTransactionalConnection(String username, String password) throws SQLException {
    TransactionSynchronizationRegistry registry = ServiceRegistry.getInstance().getTransactionSynchronizationRegistry();
    if (registry.getTransactionStatus() == Status.STATUS_NO_TRANSACTION) {
      log.debug("Connection requested outside of transaction.");
      Connection con = getConnectionFromDataSource(username, password);
      if (!con.getAutoCommit()) {
        con.setAutoCommit(true);
      }
      return con;
    }
    ConnectionImpl con = (ConnectionImpl) registry.getResource(connectionResourceKey);
    if (con == null) {
      con = new ConnectionImpl(getConnectionFromDataSource(username, password));

      if (con.getAutoCommit()) {
        con.setAutoCommit(false);
      }
      registry.putResource(connectionResourceKey, con);
      XAResource xaResource = new XAResourceImpl(con);
      ServiceRegistry.getInstance().getTransactionManager().getTransactionImpl().enlistResource(xaResource);
    }
    return con;
  }

  private Connection getConnectionFromDataSource(String username, String password) throws SQLException {
    try {
      if (username == null) {
        return getDataSource().getConnection();
      }
      return getDataSource().getConnection(username, password);
    } catch (SQLException e) {
      e.printStackTrace();
      throw e;
    }
  }

  public static class ConnectionImpl extends ConnectionWrapper {
    public ConnectionImpl(Connection con) {
      super(con);
    }

    public void closeConnection() throws SQLException {
      getConnection().close();
    }

    @Override
    public void close() {
    }
  }

  public static class XAResourceImpl extends DummyXAResource {
    private final ConnectionImpl con;

    public XAResourceImpl(ConnectionImpl con) {
      this.con = con;
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
      try {
        try {
          con.commit();
        } catch (SQLException e) {
          log.error(e.getMessage(), e);
          throw new XAException(XAException.XAER_RMERR);
        }
      } finally {
        try {
          con.closeConnection();
        } catch (SQLException e) {
          log.error(e.getMessage(), e);
        }
      }
    }

    @Override
    public void rollback(Xid xid) throws XAException {
      try {
        try {
          con.rollback();
        } catch (SQLException e) {
          log.error(e.getMessage(), e);
          throw new XAException(XAException.XAER_RMERR);
        }
      } finally {
        try {
          con.closeConnection();
        } catch (SQLException e) {
          log.error(e.getMessage(), e);
        }
      }
    }
  }

}
