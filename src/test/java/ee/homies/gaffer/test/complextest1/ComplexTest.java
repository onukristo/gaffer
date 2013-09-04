package ee.homies.gaffer.test.complextest1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import ee.homies.gaffer.test.complextest1.app.ClientsService;
import ee.homies.gaffer.test.complextest1.app.Config;
import ee.homies.gaffer.test.complextest1.app.DatabasesManager;
import ee.homies.gaffer.test.complextest1.jms.JMSNotifier;

public class ComplexTest {
  protected static final Logger log = LoggerFactory.getLogger(ComplexTest.class);

  protected static final String CONFIG_KEY_SECRETS_DATABASE_URL = "database.secrets.url";
  protected static final String CONFIG_KEY_MAX_POOL_SIZE = "maxPoolSize";
  protected static final String CLIENT_NAME = "OY Bemmimehed";
  protected static final String TABLE_ACCOUNTS = "accounts.accounts";
  protected static final String TABLE_PASSWORDS = "secrets.passwords";
  protected static final String TABLE_USERS = "users.users";
  protected static final String TABLE_CLIENTS = "clients.clients";

  protected ClassPathXmlApplicationContext appCtxt;

  boolean setUpJms;

  @Before
  public void setUpProperties() {
    System.setProperty(CONFIG_KEY_SECRETS_DATABASE_URL, "jdbc:hsqldb:mem:secrets");
    System.setProperty(CONFIG_KEY_MAX_POOL_SIZE, "1");
    setUpJms = false;
  }

  public void setUp() {
    if (setUpJms) {
      appCtxt = new ClassPathXmlApplicationContext("/ee/homies/gaffer/test/complextest1/app/applicationContext.xml",
          "/ee/homies/gaffer/test/complextest1/app/jms.xml");
    } else {
      appCtxt = new ClassPathXmlApplicationContext("/ee/homies/gaffer/test/complextest1/app/applicationContext.xml");
    }
  }

  @After
  public void tearDown() {
    appCtxt.close();
  }

  @Test
  public void testFullCommit() {
    setUp();
    Stat stat = new Stat("testFullCommit");
    getClientsService().createClient(CLIENT_NAME);
    log.info("{}", stat);

    Assert.assertEquals(1, getTableCount(TABLE_CLIENTS));
    Assert.assertEquals(1, getTableCount(TABLE_USERS));
    Assert.assertEquals(1, getTableCount(TABLE_PASSWORDS));
    Assert.assertEquals(1, getTableCount(TABLE_ACCOUNTS));
  }

  @Test
  public void testFullRollback() {
    setUp();
    try {
      getConfig().setFailPasswordCreation(true);
      Stat stat = new Stat("testFullRollback");
      try {
        getClientsService().createClient(CLIENT_NAME);
      } finally {
        log.info("{}", stat);
      }
      Assert.fail("Test has invalid logic, recheck it!");
    } catch (Exception e) {
      log.debug(e.getMessage(), e);
    }

    Assert.assertEquals(0, getTableCount(TABLE_CLIENTS));
    Assert.assertEquals(0, getTableCount(TABLE_USERS));
    Assert.assertEquals(0, getTableCount(TABLE_PASSWORDS));
    Assert.assertEquals(0, getTableCount(TABLE_ACCOUNTS));
  }

  /**
   * Use incorrect login for secrets database. When getConnection() is not
   * called, there would not be an exception and vice versa.
   */
  @Test(expected = CannotGetJdbcConnectionException.class)
  public void testEfficientResourceHandlingTestingMethod() {
    System.setProperty(CONFIG_KEY_SECRETS_DATABASE_URL, "Invalid URL.");
    setUp();
    Stat stat = new Stat("testEfficientResourceHandlingTestingMethod");
    try {
      getClientsService().createClient(CLIENT_NAME);
    } finally {
      log.info("{}", stat);
    }
  }

  /**
   * Secrets database is not used, so its getConnection() should never called.
   */
  @Test
  public void testEfficientResourceHandling() {
    System.setProperty(CONFIG_KEY_SECRETS_DATABASE_URL, "Invalid URL.");
    setUp();
    getConfig().setCreatePassword(false);
    Stat stat = new Stat("testEfficientResourceHandling");
    getClientsService().createClient(CLIENT_NAME);
    log.info("{}", stat);

    Assert.assertEquals(1, getTableCount(TABLE_CLIENTS));
    Assert.assertEquals(1, getTableCount(TABLE_USERS));
    Assert.assertEquals(0, getTableCount(TABLE_PASSWORDS));
    Assert.assertEquals(1, getTableCount(TABLE_ACCOUNTS));
  }

  @Test
  public void testParallelWork() {
    final int THREADS_COUNT = 10;
    final int N = 10000;
    System.setProperty(CONFIG_KEY_MAX_POOL_SIZE, String.valueOf(Math.max(1, THREADS_COUNT / 2)));
    setUp();
    Stat stat = new Stat("testParallelWork");
    ExecutorService executor = Executors.newFixedThreadPool(THREADS_COUNT);

    for (int i = 0; i < N; i++) {
      executor.execute(new Runnable() {
        @Override
        public void run() {
          getClientsService().createClient(CLIENT_NAME);
        }
      });
    }
    executor.shutdown();
    try {
      executor.awaitTermination(1, TimeUnit.DAYS);
    } catch (InterruptedException e) {
      log.trace(e.getMessage(), e);
    }
    log.info("{}", stat);

    Assert.assertEquals(N, getTableCount(TABLE_CLIENTS));
    Assert.assertEquals(N, getTableCount(TABLE_USERS));
    Assert.assertEquals(N, getTableCount(TABLE_PASSWORDS));
    Assert.assertEquals(N, getTableCount(TABLE_ACCOUNTS));
  }

  /**
   * Test if the setup can work with non transactional code.
   */
  @Test
  public void testLocalTransactionsFullCommit() {
    setUp();
    Stat stat = new Stat("testLocalTransactionsFullCommit");
    getClientsService().createClientLegacy(CLIENT_NAME);
    log.info("{}", stat);

    Assert.assertEquals(1, getTableCount(TABLE_CLIENTS));
    Assert.assertEquals(1, getTableCount(TABLE_USERS));
    Assert.assertEquals(1, getTableCount(TABLE_PASSWORDS));
    Assert.assertEquals(1, getTableCount(TABLE_ACCOUNTS));
  }

  /**
   * Test if the setup can work with non transactional code.
   */
  @Test
  public void testLocalTransactionsFullRollback() {
    setUp();
    try {
      getConfig().setFailPasswordCreation(true);
      Stat stat = new Stat("testLocalTransactionsFullRollback");
      try {
        getClientsService().createClientLegacy(CLIENT_NAME);
      } finally {
        log.info("{}", stat);
      }
      Assert.fail("Programming error detected. Yell at developers immediately!");
    } catch (Exception e) {
      log.debug(e.getMessage(), e);
    }

    // Local transaction
    Assert.assertEquals(1, getTableCount(TABLE_CLIENTS));
    // XA transaction #1
    Assert.assertEquals(0, getTableCount(TABLE_USERS));
    // XA transaction #1
    Assert.assertEquals(0, getTableCount(TABLE_PASSWORDS));
    // XA transaction #2, committed before #1 rolls back
    Assert.assertEquals(1, getTableCount(TABLE_ACCOUNTS));
  }

  @Test
  public void testFullCommitWithHibernate() {
    setUp();
    getConfig().setUseHibernate(true);
    Stat stat = new Stat("testFullCommit");
    getClientsService().createClient(CLIENT_NAME);
    log.info("{}", stat);

    Assert.assertEquals(1, getTableCount(TABLE_CLIENTS));
    Assert.assertEquals(1, getTableCount(TABLE_USERS));
    Assert.assertEquals(1, getTableCount(TABLE_PASSWORDS));
    Assert.assertEquals(1, getTableCount(TABLE_ACCOUNTS));
  }

  @Test
  public void testJMS() {
    setUpJms = true;
    setUp();

    JMSNotifier jmsNotifier = (JMSNotifier) appCtxt.getBean("jmsNotifier");
    try {
      jmsNotifier.notifyClientCreation();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
    }

    log.info("Done.");
  }

  protected void createDatabase() {
    appCtxt.getBean(DatabasesManager.class).createDatabases();
  }

  protected int getTableCount(String tableName) {
    return appCtxt.getBean(DatabasesManager.class).getTableRowsCount(tableName);
  }

  protected Config getConfig() {
    return (Config) appCtxt.getBean("config");
  }

  protected ClientsService getClientsService() {
    return (ClientsService) appCtxt.getBean("clientsService");
  }

  protected class Stat {
    private final long start = System.currentTimeMillis();
    private final String action;

    protected Stat(String action) {
      this.action = action;
    }

    @Override
    public String toString() {
      return action + " - " + (System.currentTimeMillis() - start) + " ms.";
    }
  }
}
