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

import org.apache.commons.lang3.StringUtils;

import ee.homies.gaffer.ServiceRegistry;
import ee.homies.gaffer.ServiceRegistryHolder;
import ee.homies.gaffer.util.DummyXAResource;
import ee.homies.gaffer.util.FormatLogger;
import ee.homies.gaffer.util.MBeanUtil;
import ee.homies.gaffer.util.XAExceptionImpl;

public class DataSourceImpl extends DataSourceWrapper implements DataSourceMXBean {
  private static final FormatLogger log = new FormatLogger(DataSourceImpl.class);

  private static AtomicLong idSequence = new AtomicLong();
  private String id;
  private String connectionResourceKey;
  private String uniqueName;

  private final AtomicLong allConnectionGetsCount = new AtomicLong();
  private final AtomicLong bufferedConnectionGetsCount = new AtomicLong();
  private final AtomicLong nonTransactionalConnectionGetsCount = new AtomicLong();
  private final AtomicLong autoCommitSwitchingsCount = new AtomicLong();

  @PostConstruct
  public void init() {
    if (StringUtils.isEmpty(uniqueName)) {
      throw new IllegalStateException("Unique name is not set.");
    }
    id = DataSourceImpl.class + "." + String.valueOf(idSequence.incrementAndGet());
    connectionResourceKey = id + ".con";

    MBeanUtil.registerMBeanQuietly(this, "ee.homies.gaffer:type=JdbcDataSource,name=" + uniqueName);
  }

  public String getUniqueName() {
    return uniqueName;
  }

  public void setUniqueName(String uniqueName) {
    this.uniqueName = uniqueName;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return getConnection0(null, null);
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return getConnection0(username, password);
  }

  private Connection getNonTransactionalConnection(String username, String password) throws SQLException {
    log.debug("Connection requested outside of transaction.");
    Connection con = getConnectionFromDataSource(username, password);
    if (!con.getAutoCommit()) {
      autoCommitSwitchingsCount.incrementAndGet();
      con.setAutoCommit(true);
    }
    nonTransactionalConnectionGetsCount.incrementAndGet();
    return con;
  }

  private Connection getConnection0(String username, String password) throws SQLException {
    ServiceRegistry serviceRegistry = ServiceRegistryHolder.getServiceRegistry();
    TransactionSynchronizationRegistry registry = serviceRegistry.getTransactionSynchronizationRegistry();
    allConnectionGetsCount.incrementAndGet();
    if (registry.getTransactionStatus() == Status.STATUS_NO_TRANSACTION) {
      return getNonTransactionalConnection(username, password);
    }
    return getTransactionalConnection(serviceRegistry, registry, username, password);
  }

  private Connection getTransactionalConnection(ServiceRegistry serviceRegistry, TransactionSynchronizationRegistry registry, String username, String password)
      throws SQLException {
    ConnectionImpl con = (ConnectionImpl) registry.getResource(connectionResourceKey);
    if (con == null) {
      con = new ConnectionImpl(getConnectionFromDataSource(username, password));
      if (con.getAutoCommit()) {
        autoCommitSwitchingsCount.incrementAndGet();
        con.setAutoCommit(false);
      }
      registry.putResource(connectionResourceKey, con);
      XAResource xaResource = new XAResourceImpl(con);
      serviceRegistry.getTransactionManager().getTransactionImpl().enlistResource(xaResource);
    } else {
      bufferedConnectionGetsCount.incrementAndGet();
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
          throw new XAExceptionImpl(XAException.XAER_RMERR, e);
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
          throw new XAExceptionImpl(XAException.XAER_RMERR, e);
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

  @Override
  public long getAllConnectionGetsCount() {
    return allConnectionGetsCount.get();
  }

  @Override
  public long getBufferedConnectionGetsCount() {
    return bufferedConnectionGetsCount.get();
  }

  @Override
  public long getNonTransactionalConnectionGetsCount() {
    return nonTransactionalConnectionGetsCount.get();
  }

  @Override
  public long getAutoCommitSwitchingCount() {
    return autoCommitSwitchingsCount.get();
  }

}
