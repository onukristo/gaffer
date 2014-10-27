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
  private AutoCommitStrategy beforeReleaseAutoCommitStrategy = AutoCommitStrategy.NONE;

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

  public AutoCommitStrategy getBeforeReleaseAutoCommitStrategy() {
    return beforeReleaseAutoCommitStrategy;
  }

  public void setBeforeReleaseAutoCommitStrategy(AutoCommitStrategy beforeReleaseAutoCommitStrategy) {
    this.beforeReleaseAutoCommitStrategy = beforeReleaseAutoCommitStrategy;
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
    NonTransactionalConnectionImpl con = new NonTransactionalConnectionImpl(this, getConnectionFromDataSource(username, password));
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
    TransactionalConnectionImpl con = (TransactionalConnectionImpl) registry.getResource(connectionResourceKey);
    if (con == null) {
      con = new TransactionalConnectionImpl(this, getConnectionFromDataSource(username, password));
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
      throw e;
    }
  }

  private void setAutoCommit(Connection con, boolean autoCommit) throws SQLException {
    boolean currentAutoCommit = con.getAutoCommit();

    if (currentAutoCommit != autoCommit) {
      autoCommitSwitchingsCount.incrementAndGet();
      con.setAutoCommit(autoCommit);
    }
  }

  private void setAutoCommitBeforeRelease(Connection con, boolean autoCommitOnBorrow) throws SQLException {
    switch (getBeforeReleaseAutoCommitStrategy()) {
    case RESTORE:
      setAutoCommit(con, autoCommitOnBorrow);
      break;
    case TRUE:
      setAutoCommit(con, true);
      break;
    case FALSE:
      setAutoCommit(con, false);
      break;
    default:
    }
  }

  private static class TransactionalConnectionImpl extends ConnectionWrapper {
    private boolean autoCommitOnBorrow;
    private DataSourceImpl dataSourceImpl;

    public TransactionalConnectionImpl(DataSourceImpl dataSourceImpl, Connection con) throws SQLException {
      super(con);
      this.dataSourceImpl = dataSourceImpl;
      autoCommitOnBorrow = con.getAutoCommit();
      dataSourceImpl.setAutoCommit(con, false);
    }

    public void closeConnection() throws SQLException {
      Connection con = getConnection();
      dataSourceImpl.setAutoCommitBeforeRelease(con, autoCommitOnBorrow);
      con.close();
    }

    @Override
    public void close() {
    }
  }

  private static class NonTransactionalConnectionImpl extends ConnectionWrapper {
    private boolean autoCommitOnBorrow;
    private DataSourceImpl dataSourceImpl;

    public NonTransactionalConnectionImpl(DataSourceImpl dataSourceImpl, Connection con) throws SQLException {
      super(con);
      this.dataSourceImpl = dataSourceImpl;
      autoCommitOnBorrow = con.getAutoCommit();
      dataSourceImpl.setAutoCommit(con, true);
    }

    public void close() throws SQLException {
      dataSourceImpl.setAutoCommitBeforeRelease(getConnection(), autoCommitOnBorrow);
      super.close();
    }
  }

  private static class XAResourceImpl extends DummyXAResource {
    private final TransactionalConnectionImpl con;

    public XAResourceImpl(TransactionalConnectionImpl con) {
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
